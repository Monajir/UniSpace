[CmdletBinding()]
param(
    [int]$FrontendPort = 18080,
    [int]$BackendPort = 18082,
    [switch]$SkipBuild,
    [switch]$KeepStack
)

$ErrorActionPreference = "Stop"
$projectName = "unispace-release-check"
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $repositoryRoot "compose.yaml"
$frontendRoot = Join-Path $repositoryRoot "Frontend/classflo-booking"
$frontendBaseUrl = "http://localhost:$FrontendPort"
$backendBaseUrl = "http://localhost:$BackendPort"
$classroomId = "11111111-1111-1111-1111-111111111111"
$adminEmail = "admin@iut-dhaka.edu"
$adminPassword = "admin12345"
$crEmail = "release.cr@iut-dhaka.edu"
$studentEmail = "release.student@iut-dhaka.edu"
$testPassword = "release12345"
$script:checksPassed = 0

function Invoke-Compose {
    param([string[]]$ComposeArguments)

    & docker compose --project-name $projectName --file $composeFile @ComposeArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose command failed: $($ComposeArguments -join ' ')"
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$BaseUrl = $frontendBaseUrl,
        [string]$Path,
        [object]$Body,
        [hashtable]$Headers = @{},
        [int[]]$ExpectedStatus = @(200)
    )

    $request = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $Headers
        SkipHttpErrorCheck = $true
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $request.ContentType = "application/json"
        $request.Body = $Body | ConvertTo-Json -Depth 10 -Compress
    }

    $response = Invoke-WebRequest @request
    if ($response.StatusCode -notin $ExpectedStatus) {
        throw "$Method $Path returned $($response.StatusCode); expected $($ExpectedStatus -join ', '). Response: $($response.Content)"
    }

    $content = if ($response.Content -is [byte[]]) {
        [System.Text.Encoding]::UTF8.GetString($response.Content)
    } else {
        [string]$response.Content
    }
    $json = $null
    if ($content -and ($response.Headers.'Content-Type' -match 'json' -or $content.TrimStart().StartsWith('{') -or $content.TrimStart().StartsWith('['))) {
        try { $json = $content | ConvertFrom-Json } catch { $json = $null }
    }
    return [pscustomobject]@{
        Status = [int]$response.StatusCode
        Content = $content
        Json = $json
    }
}

function Confirm-Check {
    param([bool]$Condition, [string]$Message)

    if (-not $Condition) {
        throw "Release check failed: $Message"
    }
    $script:checksPassed++
    Write-Host "[PASS] $Message"
}

function Get-AuthHeaders {
    param([string]$Token)
    return @{ Authorization = "Bearer $Token" }
}

function Login {
    param([string]$Email, [string]$Password)

    $response = Invoke-Api -Method POST -Path "/public/login" -Body @{
        email = $Email
        password = $Password
    }
    Confirm-Check ($response.Json.token.Length -gt 20) "Login succeeds for $Email"
    return $response.Json.token
}

function New-TestUser {
    param(
        [hashtable]$AdminHeaders,
        [string]$Name,
        [string]$Email,
        [string[]]$Roles
    )

    $response = Invoke-Api -Method POST -Path "/roles/create/user" -Headers $AdminHeaders -ExpectedStatus 201 -Body @{
        name = $Name
        email = $Email
        password = $testPassword
        roles = $Roles
        program = "CSE"
        semester = 5
        section = 1
    }
    Confirm-Check ($response.Json.email -eq $Email) "Administrator can create $Email"
    return $response.Json
}

function New-BookingBody {
    param([datetime]$Date, [string]$StartTime, [string]$EndTime, [string]$Reason)

    return @{
        classroom_id = $classroomId
        faculty_email = "faculty@iut-dhaka.edu"
        reason = $Reason
        course_code = "CSE 499"
        day = $Date.DayOfWeek.ToString()
        booking_date = $Date.ToString("yyyy-MM-dd")
        start_time = $StartTime
        end_time = $EndTime
    }
}

$managedEnvironment = @(
    "FRONTEND_PORT", "BACKEND_PORT", "CORS_ALLOWED_ORIGINS", "JWT_SECRET",
    "SPRING_PROFILES_ACTIVE", "DEMO_ADMIN_EMAIL", "DEMO_ADMIN_PASSWORD"
)
$originalEnvironment = @{}
foreach ($name in $managedEnvironment) {
    $originalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

try {
    $npmCommand = Get-Command npm.cmd -ErrorAction SilentlyContinue
    if (-not $npmCommand) {
        $npmCommand = Get-Command npm -ErrorAction SilentlyContinue
    }
    if (-not $npmCommand) {
        throw "Node.js and npm are required to run the frontend type check."
    }

    Push-Location $frontendRoot
    try {
        & $npmCommand.Source run typecheck
        Confirm-Check ($LASTEXITCODE -eq 0) "Frontend TypeScript check passes"
    } finally {
        Pop-Location
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker is required to run the Phase 7 release check."
    }

    $env:FRONTEND_PORT = $FrontendPort
    $env:BACKEND_PORT = $BackendPort
    $env:CORS_ALLOWED_ORIGINS = $frontendBaseUrl
    $env:JWT_SECRET = "phase-7-release-check-jwt-secret-at-least-32-bytes"
    $env:SPRING_PROFILES_ACTIVE = "demo"
    $env:DEMO_ADMIN_EMAIL = $adminEmail
    $env:DEMO_ADMIN_PASSWORD = $adminPassword

    Write-Host "Starting an isolated UniSpace release stack..."
    Invoke-Compose -ComposeArguments @("down", "--volumes", "--remove-orphans")
    $upArguments = @("up", "--detach", "--wait", "--wait-timeout", "180")
    if (-not $SkipBuild) {
        Invoke-Compose -ComposeArguments @("build", "--pull")
    }
    Invoke-Compose -ComposeArguments $upArguments

    $seedSql = "INSERT INTO classrooms (id, room_number, building, capacity, is_available) VALUES ('$classroomId', 'R-701', 'Academic Building', 40, true); INSERT INTO classroom_equipment (classroom_id, equipment) VALUES ('$classroomId', 'Projector');"
    Invoke-Compose -ComposeArguments @("exec", "-T", "database", "psql", "-U", "unispace", "-d", "unispace", "-v", "ON_ERROR_STOP=1", "-c", $seedSql)

    $frontendHealth = Invoke-Api -Method GET -Path "/healthz"
    Confirm-Check ($frontendHealth.Content.Trim() -eq "ok") "Frontend container is healthy"
    $backendHealth = Invoke-Api -Method GET -BaseUrl $backendBaseUrl -Path "/actuator/health"
    Confirm-Check ($backendHealth.Json.status -eq "UP") "Backend and PostgreSQL are healthy"
    $proxyHealth = Invoke-Api -Method GET -Path "/public/health-check"
    Confirm-Check ($proxyHealth.Content -eq "OK") "Frontend reverse proxy reaches the backend"
    $deepLink = Invoke-Api -Method GET -Path "/profile"
    Confirm-Check ($deepLink.Content -match '<div id="root"></div>') "SPA deep links return the application shell"

    $forbidden = Invoke-Api -Method GET -Path "/roles/all" -ExpectedStatus 403
    Confirm-Check ($forbidden.Status -eq 403) "Administrative routes reject anonymous requests"

    $adminToken = Login -Email $adminEmail -Password $adminPassword
    $adminHeaders = Get-AuthHeaders $adminToken
    $adminMe = Invoke-Api -Method GET -Path "/api/auth/me" -Headers $adminHeaders
    Confirm-Check ($adminMe.Json.user.roles -contains "ADMIN") "Demo administrator identity is available"

    $crUser = New-TestUser -AdminHeaders $adminHeaders -Name "Release CR" -Email $crEmail -Roles @("STUDENT", "CR")
    $studentUser = New-TestUser -AdminHeaders $adminHeaders -Name "Release Student" -Email $studentEmail -Roles @("STUDENT")
    $crToken = Login -Email $crEmail -Password $testPassword
    $studentToken = Login -Email $studentEmail -Password $testPassword
    $crHeaders = Get-AuthHeaders $crToken
    $studentHeaders = Get-AuthHeaders $studentToken

    $classrooms = Invoke-Api -Method GET -Path "/api/classrooms?available=true"
    Confirm-Check (($classrooms.Json | Where-Object id -eq $classroomId).Count -eq 1) "Public classroom availability uses PostgreSQL data"

    $nextMondayOffset = (([int][DayOfWeek]::Monday - [int](Get-Date).DayOfWeek + 7) % 7)
    if ($nextMondayOffset -eq 0) { $nextMondayOffset = 7 }
    $bookingDate = (Get-Date).Date.AddDays($nextMondayOffset)

    $studentCreate = Invoke-Api -Method POST -Path "/api/bookings/room/book" -Headers $studentHeaders -Body (New-BookingBody $bookingDate "10:00" "11:00" "Unauthorized student attempt") -ExpectedStatus 403
    Confirm-Check ($studentCreate.Status -eq 403) "Students cannot create CR-only booking requests"

    $created = Invoke-Api -Method POST -Path "/api/bookings/room/book" -Headers $crHeaders -Body (New-BookingBody $bookingDate "10:00" "11:00" "Release approval flow") -ExpectedStatus 201
    $bookingId = $created.Json.id
    Confirm-Check ($created.Json.status -eq "pending") "CR can submit a pending booking"

    $pending = Invoke-Api -Method GET -Path "/api/bookings/pending" -Headers $adminHeaders
    Confirm-Check (($pending.Json | Where-Object { $_.pending.id -eq $bookingId }).Count -eq 1) "Administrator can see pending bookings"

    $foreignDelete = Invoke-Api -Method DELETE -Path "/student/my/bookings/$bookingId" -Headers $studentHeaders -ExpectedStatus 404
    Confirm-Check ($foreignDelete.Status -eq 404) "Students cannot cancel another user's booking"

    $null = Invoke-Api -Method PATCH -Path "/api/bookings/$bookingId/approve" -Headers $adminHeaders -ExpectedStatus 204
    $schedule = Invoke-Api -Method GET -Path "/api/bookings/classSchedule/next/$classroomId"
    Confirm-Check (($schedule.Json.extras | Where-Object id -eq $bookingId).Count -eq 1) "Approved booking appears on next week's public schedule"

    $overlap = Invoke-Api -Method POST -Path "/api/bookings/room/book" -Headers $crHeaders -Body (New-BookingBody $bookingDate "10:30" "11:30" "Conflicting release booking") -ExpectedStatus 409
    Confirm-Check ($overlap.Status -eq 409) "Overlapping approved bookings are rejected"

    $null = Invoke-Api -Method DELETE -Path "/student/my/bookings/$bookingId" -Headers $crHeaders -ExpectedStatus 204
    $myBookings = Invoke-Api -Method GET -Path "/student/my/bookings" -Headers $crHeaders
    Confirm-Check (($myBookings.Json | Where-Object id -eq $bookingId).Count -eq 0) "CR can cancel their own booking"

    $toReject = Invoke-Api -Method POST -Path "/api/bookings/room/book" -Headers $crHeaders -Body (New-BookingBody $bookingDate "12:00" "13:00" "Release rejection flow") -ExpectedStatus 201
    $null = Invoke-Api -Method PATCH -Path "/api/bookings/$($toReject.Json.id)/reject" -Headers $adminHeaders -ExpectedStatus 204
    $myBookings = Invoke-Api -Method GET -Path "/student/my/bookings" -Headers $crHeaders
    Confirm-Check (($myBookings.Json | Where-Object { $_.id -eq $toReject.Json.id -and $_.status -eq "rejected" }).Count -eq 1) "Rejected booking remains visible with its final status"

    $null = Invoke-Api -Method POST -Path "/student/role-request" -Headers $studentHeaders -Body @{ requestedRole = "CR"; reason = "Phase 7 verification" }
    $pendingRoles = Invoke-Api -Method GET -Path "/roles/pending" -Headers $adminHeaders
    $roleRequest = $pendingRoles.Json | Where-Object email -eq $studentEmail | Select-Object -First 1
    Confirm-Check ($null -ne $roleRequest) "Student can submit a CR role request"
    $null = Invoke-Api -Method PATCH -Path "/roles/$($roleRequest.id)/approve" -Headers $adminHeaders -ExpectedStatus 204
    $studentMe = Invoke-Api -Method GET -Path "/api/auth/me" -Headers $studentHeaders
    Confirm-Check ($studentMe.Json.user.roles -contains "CR") "Approved role change takes effect without restarting services"

    Write-Host "`nPhase 7 release check passed: $script:checksPassed assertions."
}
finally {
    if (-not $KeepStack) {
        Write-Host "Cleaning up the isolated release stack..."
        try { Invoke-Compose -ComposeArguments @("down", "--volumes", "--remove-orphans") } catch { Write-Warning $_ }
    } else {
        Write-Host "Release stack kept running at $frontendBaseUrl (project: $projectName)."
    }

    foreach ($name in $managedEnvironment) {
        [Environment]::SetEnvironmentVariable($name, $originalEnvironment[$name], "Process")
    }
}
