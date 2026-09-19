import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  AlertCircle,
  BookOpen,
  CalendarDays,
  CheckCircle2,
  Clock3,
  Mail,
  MapPin,
  RefreshCw,
  ShieldCheck,
  User,
  XCircle,
} from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { NotificationFeed } from "@/components/NotificationFeed";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/hooks/useAuth";
import { apiErrorMessage, apiUrl } from "@/lib/api";
import { notifyBookingScheduleChanged } from "@/lib/bookingScheduleEvents";
import { toast } from "sonner";

interface FacultyBooking {
  pending: {
    id: string;
    classroom_id: string;
    user_id: string;
    faculty_email: string;
    faculty_name: string;
    reason: string;
    course_code: string | null;
    day: string;
    booking_date: string;
    start_time: string;
    end_time: string;
    status: "pending" | "booked" | "rejected";
    created_at: string;
    approved_at: string | null;
  };
  user_name: string;
  room_number: string;
}

interface BookingAction {
  type: "approve" | "reject";
  booking: FacultyBooking;
}

const isFacultyBookingList = (value: unknown): value is FacultyBooking[] =>
  Array.isArray(value) &&
  value.every(
    (item) =>
      item !== null &&
      typeof item === "object" &&
      "pending" in item &&
      item.pending !== null &&
      typeof item.pending === "object" &&
      "id" in item.pending &&
      typeof item.pending.id === "string" &&
      "status" in item.pending &&
      typeof item.pending.status === "string" &&
      "user_name" in item &&
      typeof item.user_name === "string" &&
      "room_number" in item &&
      typeof item.room_number === "string"
  );

const formatDate = (value: string) =>
  new Intl.DateTimeFormat("en", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));

const statusClass = (status: FacultyBooking["pending"]["status"]) => {
  if (status === "booked") return "border-success/40 bg-success/10 text-success";
  if (status === "rejected") return "border-destructive/40 bg-destructive/10 text-destructive";
  return "border-warning/40 bg-warning/10 text-warning";
};

export default function FacultyDashboard() {
  const { user, profile, loading: authLoading, signOut } = useAuth();
  const navigate = useNavigate();
  const isFaculty = user?.roles.some((role) => role.toUpperCase() === "FACULTY") ?? false;
  const [bookings, setBookings] = useState<FacultyBooking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [action, setAction] = useState<BookingAction | null>(null);
  const [submittingId, setSubmittingId] = useState<string | null>(null);

  const handleUnauthorized = useCallback(async () => {
    await signOut();
    navigate("/auth");
  }, [navigate, signOut]);

  const fetchBookings = useCallback(async () => {
    const token = localStorage.getItem("auth_token");
    if (!token) {
      await handleUnauthorized();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await fetch(apiUrl("/api/bookings/assigned-to-me"), {
        headers: { Authorization: `Bearer ${token}` },
        credentials: "include",
      });
      if (response.status === 401 || response.status === 403) {
        await handleUnauthorized();
        return;
      }
      if (!response.ok) {
        throw new Error(await apiErrorMessage(response, "Failed to load faculty bookings"));
      }

      const data: unknown = await response.json();
      if (!isFacultyBookingList(data)) {
        throw new Error("The server returned invalid faculty booking data");
      }
      setBookings(data);
    } catch (fetchError) {
      setError(fetchError instanceof Error ? fetchError.message : "Failed to load faculty bookings");
    } finally {
      setLoading(false);
    }
  }, [handleUnauthorized]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      navigate("/auth");
      return;
    }
    if (!isFaculty) {
      navigate("/profile");
      return;
    }
    void fetchBookings();
  }, [authLoading, fetchBookings, isFaculty, navigate, user]);

  const pending = useMemo(
    () => bookings.filter((booking) => booking.pending.status === "pending"),
    [bookings]
  );
  const reviewed = useMemo(
    () => bookings.filter((booking) => booking.pending.status !== "pending"),
    [bookings]
  );
  const approvedCount = bookings.filter((booking) => booking.pending.status === "booked").length;
  const rejectedCount = bookings.filter((booking) => booking.pending.status === "rejected").length;

  const submitDecision = async () => {
    if (!action) return;
    const token = localStorage.getItem("auth_token");
    if (!token) {
      await handleUnauthorized();
      return;
    }

    const { booking, type } = action;
    try {
      setSubmittingId(booking.pending.id);
      const response = await fetch(
        apiUrl(`/api/bookings/${booking.pending.id}/${type}`),
        {
          method: "PATCH",
          headers: { Authorization: `Bearer ${token}` },
          credentials: "include",
        }
      );
      if (response.status === 401 || response.status === 403) {
        await handleUnauthorized();
        return;
      }
      if (!response.ok) {
        throw new Error(await apiErrorMessage(response, `Failed to ${type} booking`));
      }

      setAction(null);
      await fetchBookings();
      notifyBookingScheduleChanged();
      toast.success(type === "approve" ? "Booking approved" : "Booking rejected");
    } catch (decisionError) {
      toast.error(decisionError instanceof Error ? decisionError.message : "Booking update failed");
    } finally {
      setSubmittingId(null);
    }
  };

  if (authLoading || (loading && bookings.length === 0)) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <main className="container mx-auto max-w-6xl px-4 pb-20 pt-32 space-y-6">
          <Skeleton className="h-32 w-full rounded-2xl" />
          <div className="grid gap-4 md:grid-cols-3">
            {[0, 1, 2].map((item) => <Skeleton key={item} className="h-28 rounded-2xl" />)}
          </div>
          <Skeleton className="h-72 w-full rounded-2xl" />
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen gradient-surface">
      <Navbar />
      <main className="container mx-auto max-w-6xl px-4 pb-20 pt-32">
        <Card className="glass shadow-elegant mb-6 overflow-hidden">
          <CardContent className="p-6 md:p-8 flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
            <div>
              <div className="mb-2 flex items-center gap-2 text-sm font-medium text-primary">
                <ShieldCheck className="h-4 w-4" /> Faculty review workspace
              </div>
              <h1 className="text-3xl font-bold tracking-tight">Welcome, {profile?.full_name || "Faculty"}</h1>
              <p className="mt-2 max-w-2xl text-muted-foreground">
                Review classroom requests assigned to {user?.email}. Your decisions update the room calendar immediately.
              </p>
            </div>
            <Button variant="outline" onClick={() => void fetchBookings()} disabled={loading}>
              <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
              Refresh
            </Button>
          </CardContent>
        </Card>

        <div className="mb-6 grid gap-4 md:grid-cols-3">
          <Card className="glass shadow-elegant">
            <CardContent className="flex items-center justify-between p-5">
              <div><p className="text-sm text-muted-foreground">Awaiting review</p><p className="text-3xl font-bold">{pending.length}</p></div>
              <Clock3 className="h-8 w-8 text-warning" />
            </CardContent>
          </Card>
          <Card className="glass shadow-elegant">
            <CardContent className="flex items-center justify-between p-5">
              <div><p className="text-sm text-muted-foreground">Approved</p><p className="text-3xl font-bold">{approvedCount}</p></div>
              <CheckCircle2 className="h-8 w-8 text-success" />
            </CardContent>
          </Card>
          <Card className="glass shadow-elegant">
            <CardContent className="flex items-center justify-between p-5">
              <div><p className="text-sm text-muted-foreground">Rejected</p><p className="text-3xl font-bold">{rejectedCount}</p></div>
              <XCircle className="h-8 w-8 text-destructive" />
            </CardContent>
          </Card>
        </div>

        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Could not load requests</AlertTitle>
            <AlertDescription className="flex items-center justify-between gap-4">
              <span>{error}</span><Button size="sm" variant="outline" onClick={() => void fetchBookings()}>Retry</Button>
            </AlertDescription>
          </Alert>
        )}

        <Card className="glass shadow-elegant mb-6">
          <CardHeader><CardTitle className="flex items-center gap-2"><BookOpen className="h-5 w-5" />Requests awaiting your decision</CardTitle></CardHeader>
          <CardContent>
            {pending.length === 0 ? (
              <div className="py-12 text-center"><CheckCircle2 className="mx-auto mb-3 h-12 w-12 text-success/70" /><p className="font-medium">You are all caught up</p><p className="text-sm text-muted-foreground">No requests assigned to you are awaiting review.</p></div>
            ) : (
              <div className="grid gap-4 lg:grid-cols-2">
                {pending.map((request) => (
                  <Card key={request.pending.id} className="border-warning/30 bg-warning/5">
                    <CardContent className="p-5 space-y-4">
                      <div className="flex items-start justify-between gap-4"><div><p className="font-semibold text-lg">{request.pending.course_code || "Classroom request"}</p><p className="text-sm text-muted-foreground">Requested by {request.user_name}</p></div><Badge className={statusClass(request.pending.status)}>Pending</Badge></div>
                      <div className="grid gap-3 text-sm sm:grid-cols-2">
                        <div className="flex gap-2"><MapPin className="mt-0.5 h-4 w-4 text-primary" /><span>Room {request.room_number}</span></div>
                        <div className="flex gap-2"><CalendarDays className="mt-0.5 h-4 w-4 text-primary" /><span>{formatDate(request.pending.booking_date)}</span></div>
                        <div className="flex gap-2"><Clock3 className="mt-0.5 h-4 w-4 text-primary" /><span>{request.pending.start_time}–{request.pending.end_time}</span></div>
                        <div className="flex gap-2"><User className="mt-0.5 h-4 w-4 text-primary" /><span>{request.user_name}</span></div>
                      </div>
                      <div className="rounded-lg border bg-background/30 p-3"><p className="mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">Reason</p><p className="text-sm">{request.pending.reason}</p></div>
                      <div className="flex gap-2">
                        <Button className="flex-1" onClick={() => setAction({ type: "approve", booking: request })} disabled={submittingId !== null}><CheckCircle2 className="mr-2 h-4 w-4" />Approve</Button>
                        <Button className="flex-1" variant="destructive" onClick={() => setAction({ type: "reject", booking: request })} disabled={submittingId !== null}><XCircle className="mr-2 h-4 w-4" />Reject</Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <div className="mb-6">
          <NotificationFeed />
        </div>

        <Card className="glass shadow-elegant">
          <CardHeader><CardTitle className="flex items-center gap-2"><ShieldCheck className="h-5 w-5" />Decision history</CardTitle></CardHeader>
          <CardContent>
            {reviewed.length === 0 ? <p className="py-8 text-center text-muted-foreground">No reviewed requests yet.</p> : (
              <div className="space-y-3">
                {reviewed.map((request) => (
                  <div key={request.pending.id} className="flex flex-col gap-3 rounded-xl border p-4 md:flex-row md:items-center md:justify-between">
                    <div><div className="flex flex-wrap items-center gap-2"><p className="font-semibold">{request.pending.course_code || "Classroom request"}</p><Badge className={statusClass(request.pending.status)}>{request.pending.status === "booked" ? "Approved" : "Rejected"}</Badge></div><p className="mt-1 text-sm text-muted-foreground">Room {request.room_number} · {formatDate(request.pending.booking_date)} · {request.pending.start_time}–{request.pending.end_time}</p></div>
                    <div className="text-sm text-muted-foreground md:text-right"><p className="flex items-center gap-1 md:justify-end"><User className="h-3.5 w-3.5" />{request.user_name}</p><p className="flex items-center gap-1 md:justify-end"><Mail className="h-3.5 w-3.5" />{request.pending.faculty_email}</p></div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </main>

      <AlertDialog open={action !== null} onOpenChange={(open) => !open && setAction(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{action?.type === "approve" ? "Approve booking?" : "Reject booking?"}</AlertDialogTitle>
            <AlertDialogDescription>
              {action ? `${action.type === "approve" ? "Approve" : "Reject"} ${action.booking.pending.course_code || "this request"} for Room ${action.booking.room_number} on ${formatDate(action.booking.pending.booking_date)}?` : "Confirm this decision."}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={submittingId !== null}>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={() => void submitDecision()} disabled={submittingId !== null} className={action?.type === "reject" ? "bg-destructive text-destructive-foreground hover:bg-destructive/90" : ""}>
              {submittingId ? "Saving..." : action?.type === "approve" ? "Approve booking" : "Reject booking"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
