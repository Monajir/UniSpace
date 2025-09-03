//// V2

// import { useState, useEffect, useRef } from "react";
// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Badge } from "@/components/ui/badge";
// import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
// import { Navbar } from "@/components/Navbar";
// import { useAuth } from "@/hooks/useAuth";
// import { useNavigate } from "react-router-dom";
// import { 
//   Users, 
//   Calendar, 
//   CheckCircle, 
//   XCircle, 
//   Clock, 
//   BarChart3,
//   Shield,
//   BookOpen,
//   UserPlus,
//   UserCheck,
//   UserX
// } from "lucide-react";
// import { gsap } from "gsap";

// export interface Booking {
//   id: string;
//   classroom_id: string;
//   user_id: string;
//   faculty_name: string;
//   faculty_email: string;
//   reason: string;
//   day: string;
//   course_code: string;
//   booking_date: string;
//   start_time: string;
//   end_time: string;
//   status: 'pending' | 'booked' | 'rejected' | 'available';
//   created_at: string;
// }

// export interface ApiResponse {
//   pending: Booking,
//   user_name: string,
//   room_number: string
// }

// // Role Management Interfaces
// export interface PendingRole {
//   _id: string;
//   userId: string;
//   userName: string;
//   userEmail: string;
//   requestedRole: string;
//   currentRole: string;
//   requestDate: string;
//   justification?: string;
// }

// export interface PendingRoleResponse {
//   _id: string;
//   userId: string;
//   userName: string;
//   userEmail: string;
//   requestedRole: string;
//   currentRole: string;
//   requestDate: string;
//   justification?: string;
// }

// export default function AdminDashboard() {
//   const { profile } = useAuth();
//   const navigate = useNavigate();
//   const dashboardRef = useRef<HTMLDivElement>(null);

//   // State for pending booking requests
//   const [pendingRequests, setPendingRequests] = useState<ApiResponse[]>([]);
//   // State for pending role requests
//   const [pendingRoleRequests, setPendingRoleRequests] = useState<PendingRoleResponse[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [roleLoading, setRoleLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);
//   const [roleError, setRoleError] = useState<string | null>(null);
  
//   useEffect(() => {
//     if (profile?.role.toLowerCase() !== 'admin') {
//       navigate("/");
//       return;
//     }
//     fetchPendingRequests();
//     fetchPendingRoleRequests();
//   }, [profile, navigate]);

//   const fetchPendingRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/api/bookings/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to fetch pending requests');
//       }

//       const data: ApiResponse[] = await response.json();
//       setPendingRequests(data);
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setLoading(false);
//     }
//   };

//   const fetchPendingRoleRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/roles/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         if (response.status === 404) {
//           setPendingRoleRequests([]);
//           setRoleLoading(false);
//           return;
//         }
//         throw new Error('Failed to fetch pending role requests');
//       }

//       const data: PendingRoleResponse[] = await response.json();
//       setPendingRoleRequests(data);
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setRoleLoading(false);
//     }
//   };

//   useEffect(() => {
//     if (dashboardRef.current) {
//       gsap.fromTo(".dashboard-card",
//         { y: 30, opacity: 0 },
//         { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
//       );
//     }
//   }, []);

//   // Mock data
//   const stats = {
//     totalBookings: 125,
//     pendingRequests: pendingRequests.length,
//     pendingRoleRequests: pendingRoleRequests.length,
//     activeUsers: 245,
//     roomUtilization: 78
//   };

//   const handleApprove = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve request');
//       }

//       // Refresh the list after approval
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to approve request');
//     }
//   };

//   const handleReject = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to reject request');
//     }
//   };

//   const handleApproveRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve role request');
//       }

//       // Refresh the list after approval
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to approve role request');
//     }
//   };

//   const handleRejectRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject role request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to reject role request');
//     }
//   };

//   // Format date for display
//   const formatDate = (dateString: string) => {
//     const date = new Date(dateString);
//     return date.toLocaleDateString('en-US', {
//       year: 'numeric',
//       month: 'short',
//       day: 'numeric'
//     });
//   };

//   // Loading display
//   if (loading || roleLoading) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="animate-pulse space-y-4">
//             <div className="h-10 bg-gray-200 rounded w-1/2 mx-auto"></div>
//             <div className="h-4 bg-gray-200 rounded w-1/3 mx-auto"></div>
//             <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
//               {[...Array(4)].map((_, i) => (
//                 <div key={i} className="h-32 bg-gray-200 rounded"></div>
//               ))}
//             </div>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   // Error display
//   if (error || roleError) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
//             <strong className="font-bold">Error: </strong>
//             <span className="block sm:inline">{error || roleError}</span>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />
      
//       <div ref={dashboardRef} className="pt-32 pb-20 px-4">
//         <div className="container mx-auto">
//           <div className="text-center mb-12">
//             <h1 className="text-4xl font-bold mb-4">
//               <span className="gradient-primary bg-clip-text text-transparent">
//                 Admin Dashboard
//               </span>
//             </h1>
//             <p className="text-xl text-muted-foreground">
//               Manage bookings, users, and system analytics
//             </p>
//           </div>

//           {/* Stats Grid */}
//           <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Total Bookings</CardTitle>
//                 <Calendar className="h-4 w-4 text-muted-foreground" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-primary">{stats.totalBookings}</div>
//                 <p className="text-xs text-muted-foreground">
//                   +12% from last month
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Booking Requests</CardTitle>
//                 <Clock className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Awaiting approval
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Role Requests</CardTitle>
//                 <UserPlus className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRoleRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Role change requests
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Room Utilization</CardTitle>
//                 <BarChart3 className="h-4 w-4 text-success" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-success">{stats.roomUtilization}%</div>
//                 <p className="text-xs text-muted-foreground">
//                   Average this month
//                 </p>
//               </CardContent>
//             </Card>
//           </div>

//           {/* Main Content */}
//           <Tabs defaultValue="requests" className="space-y-6">
//             <TabsList className="glass">
//               <TabsTrigger value="requests">Pending Booking Requests</TabsTrigger>
//               <TabsTrigger value="roles">Role Requests</TabsTrigger>
//               <TabsTrigger value="users">User Management</TabsTrigger>
//               <TabsTrigger value="analytics">Analytics</TabsTrigger>
//             </TabsList>

//             <TabsContent value="requests">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Clock className="h-5 w-5" />
//                     Pending Booking Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <Calendar className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending booking requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRequests.map((request) => (
//                         <div
//                           key={request.pending.id}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="space-y-1">
//                             <div className="flex items-center gap-4">
//                               <span className="font-semibold">Room {request.room_number}</span>
//                               <Badge variant="secondary">{request.user_name}</Badge>
//                               <span className="text-sm text-muted-foreground">
//                                 {request.pending.booking_date} • {request.pending.start_time}
//                               </span>
//                             </div>
//                             <p className="text-sm text-muted-foreground">
//                               Faculty: {request.pending.faculty_name} • {request.pending.reason}
//                             </p>
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApprove(request.pending.id)}
//                             >
//                               <CheckCircle className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleReject(request.pending.id)}
//                             >
//                               <XCircle className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="roles">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <UserPlus className="h-5 w-5" />
//                     Pending Role Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRoleRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <UserCheck className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending role requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRoleRequests.map((request) => (
//                         <div
//                           key={request._id}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="space-y-1 flex-1">
//                             <div className="flex items-center gap-4 mb-2">
//                               <span className="font-semibold">{request.userName}</span>
//                               <Badge variant="outline">{request.userEmail}</Badge>
//                               <span className="text-sm text-muted-foreground">
//                                 Requested on: {formatDate(request.requestDate)}
//                               </span>
//                             </div>
//                             <div className="flex items-center gap-4">
//                               <div className="flex items-center gap-2">
//                                 <span className="text-sm text-muted-foreground">Current Role:</span>
//                                 <Badge variant="secondary">{request.currentRole}</Badge>
//                               </div>
//                               <div className="flex items-center gap-2">
//                                 <span className="text-sm text-muted-foreground">Requested Role:</span>
//                                 <Badge variant="default">{request.requestedRole}</Badge>
//                               </div>
//                             </div>
//                             {request.justification && (
//                               <p className="text-sm text-muted-foreground mt-2">
//                                 <span className="font-medium">Justification:</span> {request.justification}
//                               </p>
//                             )}
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApproveRole(request._id)}
//                             >
//                               <UserCheck className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleRejectRole(request._id)}
//                             >
//                               <UserX className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="users">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Shield className="h-5 w-5" />
//                     User Management
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="text-center py-8">
//                     <Users className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                     <p className="text-muted-foreground">
//                       User management features coming soon...
//                     </p>
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="analytics">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <BarChart3 className="h-5 w-5" />
//                     Room Usage Analytics
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="text-center py-8">
//                     <BarChart3 className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                     <p className="text-muted-foreground">
//                       Analytics dashboard coming soon...
//                     </p>
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>
//           </Tabs>
//         </div>
//       </div>
//     </div>
//   );
// }



//// V3

// import { useState, useEffect, useRef } from "react";
// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Badge } from "@/components/ui/badge";
// import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
// import { Navbar } from "@/components/Navbar";
// import { useAuth } from "@/hooks/useAuth";
// import { useNavigate } from "react-router-dom";
// import { 
//   Users, 
//   Calendar, 
//   CheckCircle, 
//   XCircle, 
//   Clock, 
//   BarChart3,
//   Shield,
//   BookOpen,
//   UserPlus,
//   UserCheck,
//   UserX,
//   Mail
// } from "lucide-react";
// import { gsap } from "gsap";

// export interface Booking {
//   id: string;
//   classroom_id: string;
//   user_id: string;
//   faculty_name: string;
//   faculty_email: string;
//   reason: string;
//   day: string;
//   course_code: string;
//   booking_date: string;
//   start_time: string;
//   end_time: string;
//   status: 'pending' | 'booked' | 'rejected' | 'available';
//   created_at: string;
// }

// export interface ApiResponse {
//   pending: Booking,
//   user_name: string,
//   room_number: string
// }

// // Role Management Interface - Updated to match backend
// export interface PendingRoleResponse {
//   _id: string;
//   email: string;
//   role: string;
//   name: string;
// }

// export default function AdminDashboard() {
//   const { profile } = useAuth();
//   const navigate = useNavigate();
//   const dashboardRef = useRef<HTMLDivElement>(null);

//   // State for pending booking requests
//   const [pendingRequests, setPendingRequests] = useState<ApiResponse[]>([]);
//   // State for pending role requests
//   const [pendingRoleRequests, setPendingRoleRequests] = useState<PendingRoleResponse[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [roleLoading, setRoleLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);
//   const [roleError, setRoleError] = useState<string | null>(null);
  
//   useEffect(() => {
//     if (profile?.role.toLowerCase() !== 'admin') {
//       navigate("/");
//       return;
//     }
//     fetchPendingRequests();
//     fetchPendingRoleRequests();
//   }, [profile, navigate]);

//   const fetchPendingRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/api/bookings/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to fetch pending requests');
//       }

//       const data: ApiResponse[] = await response.json();
//       setPendingRequests(data);
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setLoading(false);
//     }
//   };

//   const fetchPendingRoleRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/roles/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         if (response.status === 404) {
//           setPendingRoleRequests([]);
//           setRoleLoading(false);
//           return;
//         }
//         throw new Error('Failed to fetch pending role requests');
//       }

//       const data: PendingRoleResponse[] = await response.json();
//       setPendingRoleRequests(data);
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setRoleLoading(false);
//     }
//   };

//   useEffect(() => {
//     if (dashboardRef.current) {
//       gsap.fromTo(".dashboard-card",
//         { y: 30, opacity: 0 },
//         { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
//       );
//     }
//   }, []);

//   // Mock data
//   const stats = {
//     totalBookings: 125,
//     pendingRequests: pendingRequests.length,
//     pendingRoleRequests: pendingRoleRequests.length,
//     activeUsers: 245,
//     roomUtilization: 78
//   };

//   const handleApprove = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve request');
//       }

//       // Refresh the list after approval
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to approve request');
//     }
//   };

//   const handleReject = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to reject request');
//     }
//   };

//   const handleApproveRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve role request');
//       }

//       // Refresh the list after approval
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to approve role request');
//     }
//   };

//   const handleRejectRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject role request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to reject role request');
//     }
//   };

//   // Loading display
//   if (loading || roleLoading) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="animate-pulse space-y-4">
//             <div className="h-10 bg-gray-200 rounded w-1/2 mx-auto"></div>
//             <div className="h-4 bg-gray-200 rounded w-1/3 mx-auto"></div>
//             <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
//               {[...Array(4)].map((_, i) => (
//                 <div key={i} className="h-32 bg-gray-200 rounded"></div>
//               ))}
//             </div>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   // Error display
//   if (error || roleError) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
//             <strong className="font-bold">Error: </strong>
//             <span className="block sm:inline">{error || roleError}</span>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />
      
//       <div ref={dashboardRef} className="pt-32 pb-20 px-4">
//         <div className="container mx-auto">
//           <div className="text-center mb-12">
//             <h1 className="text-4xl font-bold mb-4">
//               <span className="gradient-primary bg-clip-text text-transparent">
//                 Admin Dashboard
//               </span>
//             </h1>
//             <p className="text-xl text-muted-foreground">
//               Manage bookings, users, and system analytics
//             </p>
//           </div>

//           {/* Stats Grid */}
//           <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Total Bookings</CardTitle>
//                 <Calendar className="h-4 w-4 text-muted-foreground" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-primary">{stats.totalBookings}</div>
//                 <p className="text-xs text-muted-foreground">
//                   +12% from last month
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Booking Requests</CardTitle>
//                 <Clock className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Awaiting approval
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Role Requests</CardTitle>
//                 <UserPlus className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRoleRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Role change requests
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Room Utilization</CardTitle>
//                 <BarChart3 className="h-4 w-4 text-success" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-success">{stats.roomUtilization}%</div>
//                 <p className="text-xs text-muted-foreground">
//                   Average this month
//                 </p>
//               </CardContent>
//             </Card>
//           </div>

//           {/* Main Content */}
//           <Tabs defaultValue="requests" className="space-y-6">
//             <TabsList className="glass">
//               <TabsTrigger value="requests">Pending Booking Requests</TabsTrigger>
//               <TabsTrigger value="roles">Role Requests</TabsTrigger>
//               <TabsTrigger value="users">User Management</TabsTrigger>
//               <TabsTrigger value="analytics">Analytics</TabsTrigger>
//             </TabsList>

//             <TabsContent value="requests">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Clock className="h-5 w-5" />
//                     Pending Booking Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <Calendar className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending booking requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRequests.map((request) => (
//                         <div
//                           key={`booking-${request.pending.id}`}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="space-y-1">
//                             <div className="flex items-center gap-4">
//                               <span className="font-semibold">Room {request.room_number}</span>
//                               <Badge variant="secondary">{request.user_name}</Badge>
//                               <span className="text-sm text-muted-foreground">
//                                 {request.pending.booking_date} • {request.pending.start_time}
//                               </span>
//                             </div>
//                             <p className="text-sm text-muted-foreground">
//                               Faculty: {request.pending.faculty_name} • {request.pending.reason}
//                             </p>
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApprove(request.pending.id)}
//                             >
//                               <CheckCircle className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleReject(request.pending.id)}
//                             >
//                               <XCircle className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="roles">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <UserPlus className="h-5 w-5" />
//                     Pending Role Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRoleRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <UserCheck className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending role requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRoleRequests.map((request) => (
//                         <div
//                           key={`role-${request._id}`}
//                           className="flex flex-col md:flex-row md:items-center justify-between p-4 border rounded-lg hover-lift gap-4"
//                         >
//                           <div className="space-y-2 flex-1">
//                             <div className="flex items-center gap-3">
//                               <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
//                                 <span className="font-semibold text-primary">
//                                   {request.name.charAt(0).toUpperCase()}
//                                 </span>
//                               </div>
//                               <div>
//                                 <h3 className="font-semibold">{request.name}</h3>
//                                 <div className="flex items-center gap-2 text-sm text-muted-foreground">
//                                   <Mail className="h-3 w-3" />
//                                   {request.email}
//                                 </div>
//                               </div>
//                             </div>
                            
//                             <div className="flex items-center gap-3">
//                               <Badge variant="outline" className="flex items-center gap-1">
//                                 <Shield className="h-3 w-3" />
//                                 Requested Role: {request.role}
//                               </Badge>
//                             </div>
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApproveRole(request._id)}
//                             >
//                               <UserCheck className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleRejectRole(request._id)}
//                             >
//                               <UserX className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="users">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Shield className="h-5 w-5" />
//                     User Management
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="text-center py-8">
//                     <Users className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                     <p className="text-muted-foreground">
//                       User management features coming soon...
//                     </p>
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="analytics">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <BarChart3 className="h-5 w-5" />
//                     Room Usage Analytics
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="text-center py-8">
//                     <BarChart3 className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                     <p className="text-muted-foreground">
//                       Analytics dashboard coming soon...
//                     </p>
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>
//           </Tabs>
//         </div>
//       </div>
//     </div>
//   );
// }



//// V4

// import { useState, useEffect, useRef } from "react";
// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Badge } from "@/components/ui/badge";
// import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
// import { Input } from "@/components/ui/input";
// import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
// import { Navbar } from "@/components/Navbar";
// import { useAuth } from "@/hooks/useAuth";
// import { useNavigate } from "react-router-dom";
// import { 
//   Users, 
//   Calendar, 
//   CheckCircle, 
//   XCircle, 
//   Clock, 
//   BarChart3,
//   Shield,
//   BookOpen,
//   UserPlus,
//   UserCheck,
//   UserX,
//   Mail,
//   Search,
//   Filter,
//   Plus,
//   Trash2,
//   MoreVertical
// } from "lucide-react";
// import { gsap } from "gsap";

// export interface Booking {
//   id: string;
//   classroom_id: string;
//   user_id: string;
//   faculty_name: string;
//   faculty_email: string;
//   reason: string;
//   day: string;
//   course_code: string;
//   booking_date: string;
//   start_time: string;
//   end_time: string;
//   status: 'pending' | 'booked' | 'rejected' | 'available';
//   created_at: string;
// }

// export interface ApiResponse {
//   pending: Booking,
//   user_name: string,
//   room_number: string
// }

// // Role Management Interface - Updated to match backend
// export interface PendingRoleResponse {
//   id: string;
//   email: string;
//   role: string;
//   name: string;
// }

// // User Management Interfaces
// export interface User {
//   _id: string;
//   name: string;
//   email: string;
//   roles: string[];
//   program: string;
//   section: string;
//   semester: string;
// }

// export default function AdminDashboard() {
//   const { profile } = useAuth();
//   const navigate = useNavigate();
//   const dashboardRef = useRef<HTMLDivElement>(null);

//   // State for pending booking requests
//   const [pendingRequests, setPendingRequests] = useState<ApiResponse[]>([]);
//   // State for pending role requests
//   const [pendingRoleRequests, setPendingRoleRequests] = useState<PendingRoleResponse[]>([]);
//   // State for user management
//   const [users, setUsers] = useState<User[]>([]);
//   const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
//   const [searchTerm, setSearchTerm] = useState("");
//   const [roleFilter, setRoleFilter] = useState("all");
//   const [programFilter, setProgramFilter] = useState("all");
//   const [semesterFilter, setSemesterFilter] = useState("all");
  
//   const [loading, setLoading] = useState(true);
//   const [roleLoading, setRoleLoading] = useState(true);
//   const [userLoading, setUserLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);
//   const [roleError, setRoleError] = useState<string | null>(null);
//   const [userError, setUserError] = useState<string | null>(null);
  
//   useEffect(() => {
//     if (profile?.role.toLowerCase() !== 'admin') {
//       navigate("/");
//       return;
//     }
//     fetchPendingRequests();
//     fetchPendingRoleRequests();
//     fetchAllUsers();
//   }, [profile, navigate]);

//   const fetchPendingRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/api/bookings/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to fetch pending requests');
//       }

//       const data: ApiResponse[] = await response.json();
//       setPendingRequests(data);
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setLoading(false);
//     }
//   };

//   const fetchPendingRoleRequests = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/roles/pending', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         if (response.status === 404) {
//           setPendingRoleRequests([]);
//           setRoleLoading(false);
//           return;
//         }
//         throw new Error('Failed to fetch pending role requests');
//       }

//       const data: PendingRoleResponse[] = await response.json();
//       setPendingRoleRequests(data);
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setRoleLoading(false);
//     }
//   };

//   const fetchAllUsers = async () => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       // Replace with your actual API endpoint for fetching users
//       const response = await fetch('http://localhost:8082/roles/all', {
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to fetch users');
//       }

//       const data: User[] = await response.json();
//       setUsers(data);
//       setFilteredUsers(data);
//     } catch (err) {
//       setUserError(err instanceof Error ? err.message : 'An error occurred');
//     } finally {
//       setUserLoading(false);
//     }
//   };

//   // Filter users based on search and filter criteria
//   useEffect(() => {
//     let result = users;
    
//     // Filter by search term
//     if (searchTerm) {
//       result = result.filter(user => 
//         user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
//         user.email.toLowerCase().includes(searchTerm.toLowerCase())
//       );
//     }
    
//     // Filter by role
//     if (roleFilter !== "all") {
//       result = result.filter(user => user.roles.includes(roleFilter));
//     }
    
//     // Filter by program
//     if (programFilter !== "all") {
//       result = result.filter(user => user.program === programFilter);
//     }
    
//     // Filter by semester
//     if (semesterFilter !== "all") {
//       result = result.filter(user => user.semester === semesterFilter);
//     }
    
//     setFilteredUsers(result);
//   }, [searchTerm, roleFilter, programFilter, semesterFilter, users]);

//   useEffect(() => {
//     if (dashboardRef.current) {
//       gsap.fromTo(".dashboard-card",
//         { y: 30, opacity: 0 },
//         { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
//       );
//     }
//   }, []);

//   // Mock data
//   const stats = {
//     totalBookings: 125,
//     pendingRequests: pendingRequests.length,
//     pendingRoleRequests: pendingRoleRequests.length,
//     activeUsers: users.length,
//     roomUtilization: 78
//   };

//   const handleApprove = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve request');
//       }

//       // Refresh the list after approval
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to approve request');
//     }
//   };

//   const handleReject = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/api/bookings/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRequests();
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to reject request');
//     }
//   };

//   const handleApproveRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/approve`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to approve role request');
//       }

//       // Refresh the list after approval
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to approve role request');
//     }
//   };

//   const handleRejectRole = async (id: string) => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/roles/${id}/reject`, {
//         method: 'PATCH',
//         headers: {
//           'Authorization': `Bearer ${token}`
//         }
//       });

//       if (!response.ok) {
//         throw new Error('Failed to reject role request');
//       }

//       // Refresh the list after rejection
//       fetchPendingRoleRequests();
//     } catch (err) {
//       setRoleError(err instanceof Error ? err.message : 'Failed to reject role request');
//     }
//   };

//   const handleCreateUser = () => {
//     // Implement create user functionality
//     alert("Create User functionality would open a modal or form here");
//   };

//   const handleDeleteUser = (userId: string) => {
//     // Implement delete user functionality
//     if (window.confirm("Are you sure you want to delete this user?")) {
//       alert(`Delete user with ID: ${userId}`);
//       // Here you would make an API call to delete the user
//     }
//   };

//   // Get unique values for filters - FIXED: Properly handle roles array
//   const getUniqueValues = (key: keyof User) => {
//     if (key === 'roles') {
//       // For roles, we need to flatten the array of arrays
//       const allRoles = users.flatMap(user => user.roles);
//       return Array.from(new Set(allRoles));
//     }
    
//     const values = users.map(user => user[key]).filter(Boolean);
//     return Array.from(new Set(values));
//   };

//   const roles = getUniqueValues('roles') as string[];
//   const programs = getUniqueValues('program') as string[];
//   const semesters = getUniqueValues('semester') as string[];

//   // Loading display
//   if (loading || roleLoading || userLoading) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="animate-pulse space-y-4">
//             <div className="h-10 bg-gray-200 rounded w-1/2 mx-auto"></div>
//             <div className="h-4 bg-gray-200 rounded w-1/3 mx-auto"></div>
//             <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
//               {[...Array(4)].map((_, i) => (
//                 <div key={i} className="h-32 bg-gray-200 rounded"></div>
//               ))}
//             </div>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   // Error display
//   if (error || roleError || userError) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 pb-20 px-4 container mx-auto text-center">
//           <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
//             <strong className="font-bold">Error: </strong>
//             <span className="block sm:inline">{error || roleError || userError}</span>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />
      
//       <div ref={dashboardRef} className="pt-32 pb-20 px-4">
//         <div className="container mx-auto">
//           <div className="text-center mb-12">
//             <h1 className="text-4xl font-bold mb-4">
//               <span className="gradient-primary bg-clip-text text-transparent">
//                 Admin Dashboard
//               </span>
//             </h1>
//             <p className="text-xl text-muted-foreground">
//               Manage bookings, users, and system analytics
//             </p>
//           </div>

//           {/* Stats Grid */}
//           <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Total Bookings</CardTitle>
//                 <Calendar className="h-4 w-4 text-muted-foreground" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-primary">{stats.totalBookings}</div>
//                 <p className="text-xs text-muted-foreground">
//                   +12% from last month
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Booking Requests</CardTitle>
//                 <Clock className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Awaiting approval
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Pending Role Requests</CardTitle>
//                 <UserPlus className="h-4 w-4 text-warning" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-warning">{stats.pendingRoleRequests}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Role change requests
//                 </p>
//               </CardContent>
//             </Card>

//             <Card className="dashboard-card glass hover-lift">
//               <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
//                 <CardTitle className="text-sm font-medium">Active Users</CardTitle>
//                 <Users className="h-4 w-4 text-success" />
//               </CardHeader>
//               <CardContent>
//                 <div className="text-2xl font-bold text-success">{stats.activeUsers}</div>
//                 <p className="text-xs text-muted-foreground">
//                   Registered in system
//                 </p>
//               </CardContent>
//             </Card>
//           </div>

//           {/* Main Content */}
//           <Tabs defaultValue="requests" className="space-y-6">
//             <TabsList className="glass">
//               <TabsTrigger value="requests">Pending Booking Requests</TabsTrigger>
//               <TabsTrigger value="roles">Role Requests</TabsTrigger>
//               <TabsTrigger value="users">User Management</TabsTrigger>
//               <TabsTrigger value="analytics">Analytics</TabsTrigger>
//             </TabsList>

//             <TabsContent value="requests">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Clock className="h-5 w-5" />
//                     Pending Booking Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <Calendar className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending booking requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRequests.map((request) => (
//                         <div
//                           key={`booking-${request.pending.id}`}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="space-y-1">
//                             <div className="flex items-center gap-4">
//                               <span className="font-semibold">Room {request.room_number}</span>
//                               <Badge variant="secondary">{request.user_name}</Badge>
//                               <span className="text-sm text-muted-foreground">
//                                 {request.pending.booking_date} • {request.pending.start_time}
//                               </span>
//                             </div>
//                             <p className="text-sm text-muted-foreground">
//                               Faculty: {request.pending.faculty_name} • {request.pending.reason}
//                             </p>
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApprove(request.pending.id)}
//                             >
//                               <CheckCircle className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleReject(request.pending.id)}
//                             >
//                               <XCircle className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="roles">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <UserPlus className="h-5 w-5" />
//                     Pending Role Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {pendingRoleRequests.length === 0 ? (
//                     <div className="text-center py-8">
//                       <UserCheck className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         No pending role requests at this time.
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {pendingRoleRequests.map((request) => (
//                         <div
//                           key={`role-${request.id}`}
//                           className="flex flex-col md:flex-row md:items-center justify-between p-4 border rounded-lg hover-lift gap-4"
//                         >
//                           <div className="space-y-2 flex-1">
//                             <div className="flex items-center gap-3">
//                               <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
//                                 <span className="font-semibold text-primary">
//                                   {request.name.charAt(0).toUpperCase()}
//                                 </span>
//                               </div>
//                               <div>
//                                 <h3 className="font-semibold">{request.name}</h3>
//                                 <div className="flex items-center gap-2 text-sm text-muted-foreground">
//                                   <Mail className="h-3 w-3" />
//                                   {request.email}
//                                 </div>
//                               </div>
//                             </div>
                            
//                             <div className="flex items-center gap-3">
//                               <Badge variant="outline" className="flex items-center gap-1">
//                                 <Shield className="h-3 w-3" />
//                                 Requested Role: {request.role}
//                               </Badge>
//                             </div>
//                           </div>
                          
//                           <div className="flex gap-2">
//                             <Button
//                               size="sm"
//                               variant="outline"
//                               className="hover-scale"
//                               onClick={() => handleApproveRole(request.id)}
//                             >
//                               <UserCheck className="h-4 w-4 mr-1" />
//                               Approve
//                             </Button>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               className="hover-scale"
//                               onClick={() => handleRejectRole(request.id)}
//                             >
//                               <UserX className="h-4 w-4 mr-1" />
//                               Reject
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="users">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader className="flex flex-row items-center justify-between">
//                   <CardTitle className="flex items-center gap-2">
//                     <Shield className="h-5 w-5" />
//                     User Management
//                   </CardTitle>
//                   <div className="flex gap-2">
//                     <Button onClick={handleCreateUser} className="flex items-center gap-2">
//                       <Plus className="h-4 w-4" />
//                       Create User
//                     </Button>
//                   </div>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="flex flex-col md:flex-row gap-4 mb-6">
//                     <div className="relative flex-1">
//                       <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
//                       <Input
//                         type="search"
//                         placeholder="Search users by name or email..."
//                         className="pl-8"
//                         value={searchTerm}
//                         onChange={(e) => setSearchTerm(e.target.value)}
//                       />
//                     </div>
                    
//                     <div className="flex gap-2 flex-wrap">
//                       <Select value={roleFilter} onValueChange={setRoleFilter}>
//                         <SelectTrigger className="w-[130px]">
//                           <Filter className="h-4 w-4 mr-2" />
//                           <SelectValue placeholder="Role" />
//                         </SelectTrigger>
//                         <SelectContent>
//                           <SelectItem value="all">All Roles</SelectItem>
//                           {roles.map(role => (
//                             <SelectItem key={role} value={role}>{role}</SelectItem>
//                           ))}
//                         </SelectContent>
//                       </Select>
                      
//                       <Select value={programFilter} onValueChange={setProgramFilter}>
//                         <SelectTrigger className="w-[130px]">
//                           <Filter className="h-4 w-4 mr-2" />
//                           <SelectValue placeholder="Program" />
//                         </SelectTrigger>
//                         <SelectContent>
//                           <SelectItem value="all">All Programs</SelectItem>
//                           {programs.map(program => (
//                             <SelectItem key={program} value={program}>{program}</SelectItem>
//                           ))}
//                         </SelectContent>
//                       </Select>
                      
//                       <Select value={semesterFilter} onValueChange={setSemesterFilter}>
//                         <SelectTrigger className="w-[130px]">
//                           <Filter className="h-4 w-4 mr-2" />
//                           <SelectValue placeholder="Semester" />
//                         </SelectTrigger>
//                         <SelectContent>
//                           <SelectItem value="all">All Semesters</SelectItem>
//                           {semesters.map(semester => (
//                             <SelectItem key={semester} value={semester}>{semester}</SelectItem>
//                           ))}
//                         </SelectContent>
//                       </Select>
//                     </div>
//                   </div>
                  
//                   {filteredUsers.length === 0 ? (
//                     <div className="text-center py-8">
//                       <Users className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                       <p className="text-muted-foreground">
//                         {users.length === 0 ? 'No users found in the system.' : 'No users match your search criteria.'}
//                       </p>
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {filteredUsers.map((user) => (
//                         <div
//                           key={`user-${user._id}`}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="flex items-center gap-4">
//                             <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
//                               <span className="font-semibold text-primary text-lg">
//                                 {user.name.charAt(0).toUpperCase()}
//                               </span>
//                             </div>
//                             <div>
//                               <h3 className="font-semibold">{user.name}</h3>
//                               <p className="text-sm text-muted-foreground flex items-center gap-1">
//                                 <Mail className="h-3 w-3" />
//                                 {user.email}
//                               </p>
//                               <div className="flex gap-2 mt-1">
//                                 {user.roles.map(role => (
//                                   <Badge key={`${user._id}-${role}`} variant="secondary">{role}</Badge>
//                                 ))}
//                               </div>
//                             </div>
//                           </div>
                          
//                           <div className="flex items-center gap-4">
//                             <div className="text-right">
//                               <p className="text-sm">{user.program}</p>
//                               <p className="text-xs text-muted-foreground">
//                                 Sem {user.semester} • Sec {user.section}
//                               </p>
//                             </div>
//                             <Button
//                               size="sm"
//                               variant="destructive"
//                               onClick={() => handleDeleteUser(user._id)}
//                             >
//                               <Trash2 className="h-4 w-4" />
//                             </Button>
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="analytics">
//               <Card className="dashboard-card glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <BarChart3 className="h-5 w-5" />
//                     Room Usage Analytics
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="text-center py-8">
//                     <BarChart3 className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
//                     <p className="text-muted-foreground">
//                       Analytics dashboard coming soon...
//                     </p>
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>
//           </Tabs>
//         </div>
//       </div>
//     </div>
//   );
// }

import { useState, useEffect, useRef, useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { 
  Users, 
  Calendar, 
  CheckCircle, 
  XCircle, 
  Clock, 
  BarChart3,
  Shield,
  BookOpen,
  UserPlus,
  UserCheck,
  UserX,
  Mail,
  Search,
  Filter,
  Plus,
  Trash2,
  MoreVertical,
  X
} from "lucide-react";
import { gsap } from "gsap";

export interface Booking {
  id: string;
  classroom_id: string;
  user_id: string;
  faculty_name: string;
  faculty_email: string;
  reason: string;
  day: string;
  course_code: string;
  booking_date: string;
  start_time: string;
  end_time: string;
  status: 'pending' | 'booked' | 'rejected' | 'available';
  created_at: string;
}

export interface ApiResponse {
  pending: Booking,
  user_name: string,
  room_number: string
}

// Role Management Interface - Updated to match backend
export interface PendingRoleResponse {
  _id: string;
  email: string;
  role: string;
  name: string;
}

// User Management Interfaces
export interface User {
  _id: string;
  name: string;
  email: string;
  roles: string[];
  program: string;
  section: string;
  semester: string;
}

// Available roles for the create user form
const availableRoles = ["STUDENT", "FACULTY", "ADMIN"];

export default function AdminDashboard() {
  const { profile } = useAuth();
  const navigate = useNavigate();
  const dashboardRef = useRef<HTMLDivElement>(null);

  // State for pending booking requests
  const [pendingRequests, setPendingRequests] = useState<ApiResponse[]>([]);
  // State for pending role requests
  const [pendingRoleRequests, setPendingRoleRequests] = useState<PendingRoleResponse[]>([]);
  // State for user management
  const [users, setUsers] = useState<User[] | null>(null);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [programFilter, setProgramFilter] = useState("all");
  const [semesterFilter, setSemesterFilter] = useState("all");
  
  // State for create user modal
  const [isCreateUserModalOpen, setIsCreateUserModalOpen] = useState(false);
  const [newUser, setNewUser] = useState({
    name: "",
    email: "",
    section: "",
    password: "",
    roles: [] as string[],
    program: "",
    semester: ""
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [createUserError, setCreateUserError] = useState<string | null>(null);
  
  const [loading, setLoading] = useState(true);
  const [roleLoading, setRoleLoading] = useState(true);
  const [userLoading, setUserLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [roleError, setRoleError] = useState<string | null>(null);
  const [userError, setUserError] = useState<string | null>(null);
  
  useEffect(() => {
    if (profile?.role.toLowerCase() !== 'admin') {
      navigate("/");
      return;
    }
    fetchPendingRequests();
    fetchPendingRoleRequests();
    fetchAllUsers();
  }, [profile, navigate]);

  const fetchPendingRequests = async () => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch('http://localhost:8082/api/bookings/pending', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch pending requests');
      }

      const data: ApiResponse[] = await response.json();
      setPendingRequests(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingRoleRequests = async () => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch('http://localhost:8082/roles/pending', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        if (response.status === 404) {
          setPendingRoleRequests([]);
          setRoleLoading(false);
          return;
        }
        throw new Error('Failed to fetch pending role requests');
      }

      const data: PendingRoleResponse[] = await response.json();
      setPendingRoleRequests(data);
    } catch (err) {
      setRoleError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setRoleLoading(false);
    }
  };

  const fetchAllUsers = async () => {
    try {
      const token = localStorage.getItem('auth_token');
      // Replace with your actual API endpoint for fetching users
      const response = await fetch('http://localhost:8082/roles/all', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch users');
      }

      const data: User[] = await response.json();
      console.log(data);
      setUsers(data);
      setFilteredUsers(data);
    } catch (err) {
      setUserError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setUserLoading(false);
    }
  };

  // Filter users based on search and filter criteria
  useEffect(() => {
    if (!users) {
      setFilteredUsers([]);
      return;
    }
    
    let result = users;
    
    // Filter by search term
    if (searchTerm) {
      result = result.filter(user => 
        user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
    
    // Filter by role
    if (roleFilter !== "all") {
      result = result.filter(user => user.roles.includes(roleFilter));
    }
    
    // Filter by program
    if (programFilter !== "all") {
      result = result.filter(user => user.program === programFilter);
    }
    
    // Filter by semester
    if (semesterFilter !== "all") {
      result = result.filter(user => user.semester === semesterFilter);
    }
    
    setFilteredUsers(result);
  }, [searchTerm, roleFilter, programFilter, semesterFilter, users]);

  useEffect(() => {
    if (dashboardRef.current) {
      gsap.fromTo(".dashboard-card",
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
      );
    }
  }, []);

  // Mock data
  const stats = {
    totalBookings: 125,
    pendingRequests: pendingRequests.length,
    pendingRoleRequests: pendingRoleRequests.length,
    activeUsers: users?.length || 0,
    roomUtilization: 78
  };

  const handleApprove = async (id: string) => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(`http://localhost:8082/api/bookings/${id}/approve`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to approve request');
      }

      // Refresh the list after approval
      fetchPendingRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve request');
    }
  };

  const handleReject = async (id: string) => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(`http://localhost:8082/api/bookings/${id}/reject`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to reject request');
      }

      // Refresh the list after rejection
      fetchPendingRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reject request');
    }
  };

  const handleApproveRole = async (id: string) => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(`http://localhost:8082/roles/${id}/approve`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to approve role request');
      }

      // Refresh the list after approval
      fetchAllUsers();
      fetchPendingRoleRequests();
    } catch (err) {
      setRoleError(err instanceof Error ? err.message : 'Failed to approve role request');
    }
  };

  const handleRejectRole = async (id: string) => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(`http://localhost:8082/roles/${id}/reject`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to reject role request');
      }

      // Refresh the list after rejection
      fetchPendingRoleRequests();
    } catch (err) {
      setRoleError(err instanceof Error ? err.message : 'Failed to reject role request');
    }
  };

  const handleCreateUser = () => {
    setIsCreateUserModalOpen(true);
  };

  const handleCloseCreateUserModal = () => {
    setIsCreateUserModalOpen(false);
    setNewUser({
      name: "",
      email: "",
      section: "",
      password: "",
      roles: [],
      program: "",
      semester: ""
    });
    setCreateUserError(null);
  };

  const handleNewUserChange = (field: string, value: string | string[]) => {
    setNewUser(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleRoleToggle = (role: string) => {
    setNewUser(prev => {
      const roles = prev.roles.includes(role)
        ? prev.roles.filter(r => r !== role)
        : [...prev.roles, role];
      
      return {
        ...prev,
        roles
      };
    });
  };

  const handleCreateUserSubmit = async () => {
    // Basic validation
    if (!newUser.name || !newUser.email || !newUser.password || newUser.roles.length === 0) {
      setCreateUserError("Please fill in all required fields");
      return;
    }

    setIsSubmitting(true);
    setCreateUserError(null);

    try {
      const token = localStorage.getItem('auth_token');

      console.log('Token exists: ', !!token);
      console.log('Request payload: ', newUser);

      const response = await fetch('http://localhost:8082/roles/create/user', {
        method: 'POST',
        headers: {
          // 'Authorization': `Bearer ${token}`,
          // This is a temporary fix, find the reason behind why it isn't working
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newUser)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create user');
      }

      // Refresh the user list
      fetchAllUsers();
      handleCloseCreateUserModal();
    } catch (err) {
      setCreateUserError(err instanceof Error ? err.message : 'Failed to create user');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteUser = async (userId: string) => {
    
    const token = localStorage.getItem('auth_token');
      const response = await fetch(`http://localhost:8082/roles/delete/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newUser)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create user');
      }

      // Refresh the user list
      fetchAllUsers();
      handleCloseCreateUserModal();
  };

  // Get unique values for filters - FIXED: Properly handle roles array
  const getUniqueValues = (key: keyof User) => {
    if (!users) return [];
    
    if (key === 'roles') {
      // For roles, we need to flatten the array of arrays
      const allRoles = users.flatMap(user => user.roles);
      return Array.from(new Set(allRoles));
    }
    
    const values = users.map(user => user[key]).filter(Boolean);
    return Array.from(new Set(values));
  };

  // const roles = getUniqueValues('roles') as string[];
  // const programs = getUniqueValues('program') as string[];
  // const semesters = getUniqueValues('semester') as string[];

  // Replace the direct calls with useMemo
  const roles = useMemo(() => {
    if (!users) return [];
    return getUniqueValues('roles') as string[];
  }, [users]);

  const programs = useMemo(() => {
    if (!users) return [];
    return getUniqueValues('program') as string[];
  }, [users]);

  const semesters = useMemo(() => {
    if (!users) return [];
    return getUniqueValues('semester') as string[];
  }, [users]);


  // Loading display
  if (loading || roleLoading || userLoading) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 pb-20 px-4 container mx-auto text-center">
          <div className="animate-pulse space-y-4">
            <div className="h-10 bg-gray-200 rounded w-1/2 mx-auto"></div>
            <div className="h-4 bg-gray-200 rounded w-1/3 mx-auto"></div>
            <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="h-32 bg-gray-200 rounded"></div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Error display
  if (error || roleError || userError) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 pb-20 px-4 container mx-auto text-center">
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
            <strong className="font-bold">Error: </strong>
            <span className="block sm:inline">{error || roleError || userError}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen gradient-surface">
      <Navbar />
      
      <div ref={dashboardRef} className="pt-32 pb-20 px-4">
        <div className="container mx-auto">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">
              <span className="gradient-primary bg-clip-text text-transparent">
                Admin Dashboard
              </span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Manage bookings, users, and system analytics
            </p>
          </div>

          {/* Stats Grid */}
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <Card className="dashboard-card glass hover-lift">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Bookings</CardTitle>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-primary">{stats.totalBookings}</div>
                <p className="text-xs text-muted-foreground">
                  +12% from last month
                </p>
              </CardContent>
            </Card>

            <Card className="dashboard-card glass hover-lift">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Pending Booking Requests</CardTitle>
                <Clock className="h-4 w-4 text-warning" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-warning">{stats.pendingRequests}</div>
                <p className="text-xs text-muted-foreground">
                  Awaiting approval
                </p>
              </CardContent>
            </Card>

            <Card className="dashboard-card glass hover-lift">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Pending Role Requests</CardTitle>
                <UserPlus className="h-4 w-4 text-warning" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-warning">{stats.pendingRoleRequests}</div>
                <p className="text-xs text-muted-foreground">
                  Role change requests
                </p>
              </CardContent>
            </Card>

            <Card className="dashboard-card glass hover-lift">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Users</CardTitle>
                <Users className="h-4 w-4 text-success" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-success">{stats.activeUsers}</div>
                <p className="text-xs text-muted-foreground">
                  Registered in system
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Main Content */}
          <Tabs defaultValue="requests" className="space-y-6">
            <TabsList className="glass">
              <TabsTrigger value="requests">Pending Booking Requests</TabsTrigger>
              <TabsTrigger value="roles">Role Requests</TabsTrigger>
              <TabsTrigger value="users">User Management</TabsTrigger>
              {/* <TabsTrigger value="analytics">Analytics</TabsTrigger> */}
            </TabsList>

            <TabsContent value="requests">
              <Card className="dashboard-card glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Clock className="h-5 w-5" />
                    Pending Booking Requests
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {pendingRequests.length === 0 ? (
                    <div className="text-center py-8">
                      <Calendar className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">
                        No pending booking requests at this time.
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {pendingRequests.map((request) => (
                        <div
                          key={`booking-${request.pending.id}`}
                          className="flex items-center justify-between p-4 border rounded-lg hover-lift"
                        >
                          <div className="space-y-1">
                            <div className="flex items-center gap-4">
                              <span className="font-semibold">Room {request.room_number}</span>
                              <Badge variant="secondary">{request.user_name}</Badge>
                              <span className="text-sm text-muted-foreground">
                                {request.pending.booking_date} • {request.pending.start_time}
                              </span>
                            </div>
                            <p className="text-sm text-muted-foreground">
                              Faculty: {request.pending.faculty_name} • {request.pending.reason}
                            </p>
                          </div>
                          
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              className="hover-scale"
                              onClick={() => handleApprove(request.pending.id)}
                            >
                              <CheckCircle className="h-4 w-4 mr-1" />
                              Approve
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              className="hover-scale"
                              onClick={() => handleReject(request.pending.id)}
                            >
                              <XCircle className="h-4 w-4 mr-1" />
                              Reject
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="roles">
              <Card className="dashboard-card glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <UserPlus className="h-5 w-5" />
                    Pending Role Requests
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {pendingRoleRequests.length === 0 ? (
                    <div className="text-center py-8">
                      <UserCheck className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">
                        No pending role requests at this time.
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {pendingRoleRequests.map((request) => (
                        <div
                          key={`role-${request._id}`}
                          className="flex flex-col md:flex-row md:items-center justify-between p-4 border rounded-lg hover-lift gap-4"
                        >
                          <div className="space-y-2 flex-1">
                            <div className="flex items-center gap-3">
                              <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                                <span className="font-semibold text-primary">
                                  {request.name.charAt(0).toUpperCase()}
                                </span>
                              </div>
                              <div>
                                <h3 className="font-semibold">{request.name}</h3>
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                  <Mail className="h-3 w-3" />
                                  {request.email}
                                </div>
                              </div>
                            </div>
                            
                            <div className="flex items-center gap-3">
                              <Badge variant="outline" className="flex items-center gap-1">
                                <Shield className="h-3 w-3" />
                                Requested Role: {request.role}
                              </Badge>
                            </div>
                          </div>
                          
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              className="hover-scale"
                              onClick={() => handleApproveRole(request._id)}
                            >
                              <UserCheck className="h-4 w-4 mr-1" />
                              Approve
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              className="hover-scale"
                              onClick={() => handleRejectRole(request._id)}
                            >
                              <UserX className="h-4 w-4 mr-1" />
                              Reject
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="users">
              <Card className="dashboard-card glass shadow-elegant">
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <Shield className="h-5 w-5" />
                    User Management
                  </CardTitle>
                  <div className="flex gap-2">
                    <Button onClick={handleCreateUser} className="flex items-center gap-2">
                      <Plus className="h-4 w-4" />
                      Create User
                    </Button>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-col md:flex-row gap-4 mb-6">
                    <div className="relative flex-1">
                      <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                      <Input
                        type="search"
                        placeholder="Search users by name or email..."
                        className="pl-8"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                      />
                    </div>
                    
                    <div className="flex gap-2 flex-wrap">
                      <Select value={roleFilter} onValueChange={setRoleFilter}>
                        <SelectTrigger className="w-[130px]">
                          <Filter className="h-4 w-4 mr-2" />
                          <SelectValue placeholder="Role" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All Roles</SelectItem>
                          {(roles ?? []).map(role => (
                            <SelectItem key={role} value={role}>{role}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      
                      <Select value={programFilter} onValueChange={setProgramFilter}>
                        <SelectTrigger className="w-[130px]">
                          <Filter className="h-4 w-4 mr-2" />
                          <SelectValue placeholder="Program" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All Programs</SelectItem>
                          {programs.map(program => (
                            <SelectItem key={program} value={program}>{program}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      
                      <Select value={semesterFilter} onValueChange={setSemesterFilter}>
                        <SelectTrigger className="w-[130px]">
                          <Filter className="h-4 w-4 mr-2" />
                          <SelectValue placeholder="Semester" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All Semesters</SelectItem>
                          {semesters.map(semester => (
                            <SelectItem key={semester} value={semester}>{semester}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                  
                  {!users ? (
                    <div className="text-center py-8">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                      <p className="text-muted-foreground">Loading users...</p>
                    </div>
                  ) : filteredUsers.length === 0 ? (
                    <div className="text-center py-8">
                      <Users className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">
                        {users.length === 0 ? 'No users found in the system.' : 'No users match your search criteria.'}
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {filteredUsers.map((user) => (
                        <div
                          key={`user-${user._id}`}
                          className="flex items-center justify-between p-4 border rounded-lg hover-lift"
                        >
                          <div className="flex items-center gap-4">
                            <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                              <span className="font-semibold text-primary text-lg">
                                {user.name.charAt(0).toUpperCase()}
                              </span>
                            </div>
                            <div>
                              <h3 className="font-semibold">{user.name}</h3>
                              <p className="text-sm text-muted-foreground flex items-center gap-1">
                                <Mail className="h-3 w-3" />
                                {user.email}
                              </p>
                              <div className="flex gap-2 mt-1">
                                {user.roles.map(role => (
                                  <Badge key={`${user._id}-${role}`} variant="secondary">{role}</Badge>
                                ))}
                              </div>
                            </div>
                          </div>
                          
                          <div className="flex items-center gap-4">
                            <div className="text-right">
                              <p className="text-sm">{user.program}</p>
                              <p className="text-xs text-muted-foreground">
                                Sem {user.semester} • Sec {user.section}
                              </p>
                            </div>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => handleDeleteUser(user._id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="analytics">
              <Card className="dashboard-card glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BarChart3 className="h-5 w-5" />
                    Room Usage Analytics
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-center py-8">
                    <BarChart3 className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">
                      Analytics dashboard coming soon...
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>

      {/* Create User Modal */}
      <Dialog open={isCreateUserModalOpen} onOpenChange={setIsCreateUserModalOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <UserPlus className="h-5 w-5" />
              Create New User
            </DialogTitle>
          </DialogHeader>
          
          <div className="grid gap-4 py-4">
            {createUserError && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                {createUserError}
              </div>
            )}
            
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input
                  id="name"
                  value={newUser.name}
                  onChange={(e) => handleNewUserChange("name", e.target.value)}
                  placeholder="Enter full name"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="email">Email *</Label>
                <Input
                  id="email"
                  type="email"
                  value={newUser.email}
                  onChange={(e) => handleNewUserChange("email", e.target.value)}
                  placeholder="Enter email address"
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="password">Password *</Label>
              <Input
                id="password"
                type="password"
                value={newUser.password}
                onChange={(e) => handleNewUserChange("password", e.target.value)}
                placeholder="Enter password"
              />
            </div>
            
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="program">Program</Label>
                <Input
                  id="program"
                  value={newUser.program}
                  onChange={(e) => handleNewUserChange("program", e.target.value)}
                  placeholder="e.g., Computer Science"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="section">Section</Label>
                <Input
                  id="section"
                  value={newUser.section}
                  onChange={(e) => handleNewUserChange("section", e.target.value)}
                  placeholder="e.g., A"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="semester">Semester</Label>
                <Input
                  id="semester"
                  value={newUser.semester}
                  onChange={(e) => handleNewUserChange("semester", e.target.value)}
                  placeholder="e.g., 5"
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <Label>Roles *</Label>
              <div className="flex gap-2 flex-wrap">
                {availableRoles.map(role => (
                  <Badge
                    key={role}
                    variant={newUser.roles.includes(role) ? "default" : "outline"}
                    className="cursor-pointer px-3 py-1"
                    onClick={() => handleRoleToggle(role)}
                  >
                    {role}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
          
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseCreateUserModal}>
              Cancel
            </Button>
            <Button 
              onClick={handleCreateUserSubmit} 
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating..." : "Create User"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}