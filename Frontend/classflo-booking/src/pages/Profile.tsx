// import { useState, useEffect, useRef } from "react";
// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Badge } from "@/components/ui/badge";
// import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
// import { Navbar } from "@/components/Navbar";
// import { useAuth } from "@/hooks/useAuth";
// import { useNavigate } from "react-router-dom";
// import {
//   User,
//   Calendar,
//   BookOpen,
//   Bell,
//   Clock,
//   MapPin,
//   Edit,
// } from "lucide-react";
// import { gsap } from "gsap";

// export default function Profile() {
//   const { user, profile } = useAuth();
//   const navigate = useNavigate();
//   const profileRef = useRef<HTMLDivElement>(null);

//   const [routine, setRoutine] = useState<any[]>([]);
//   const [bookingRequests, setBookingRequests] = useState<any[]>([]);
//   const [loading, setLoading] = useState({
//     routine: false,
//     notifications: false,
//     bookings: true,
//   });
//   const [error, setError] = useState({
//     routine: null,
//     notifications: null,
//     bookings: null,
//   });

//   useEffect(() => {
//     if (!user) {
//       navigate("/auth");
//       return;
//     }

//     // Fetching all data when component mounts
//     fetchRoutine();
//     fetchBookingRequests();
//   }, [user, navigate]);

//   useEffect(() => {
//     if (profileRef.current) {
//       gsap.fromTo(
//         ".profile-section",
//         { y: 30, opacity: 0 },
//         { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
//       );
//     }
//   }, []);

//   // Loading skeletons
//   const renderLoadingSkeleton = (count: number) => {
//     return Array(count)
//       .fill(0)
//       .map((_, i) => (
//         <div key={i} className="p-4 border rounded-lg animate-pulse">
//           <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
//           <div className="h-3 bg-gray-200 rounded w-1/2"></div>
//         </div>
//       ));
//   };

//   // Mock routine data
//   // const routine = [
//   //   {
//   //     day: "Monday",
//   //     classes: [
//   //       { time: "08:00-09:15", subject: "Data Structures", room: "101", type: "regular" },
//   //       { time: "10:30-11:45", subject: "Algorithms", room: "202", type: "regular" },
//   //       { time: "14:30-15:45", subject: "Extra Class", room: "301", type: "extra" }
//   //     ]
//   //   },
//   //   {
//   //     day: "Tuesday",
//   //     classes: [
//   //       { time: "09:15-10:30", subject: "Database Systems", room: "Lab-1", type: "regular" },
//   //       { time: "11:45-13:00", subject: "Software Engineering", room: "202", type: "regular" }
//   //     ]
//   //   }
//   // ];

//   const notifications = [
//     {
//       id: 1,
//       title: "Booking Approved",
//       message: "Your booking for Room 101 has been approved",
//       time: "2 hours ago",
//       type: "success",
//     },
//     {
//       id: 2,
//       title: "New Schedule Update",
//       message: "Extra class added for tomorrow",
//       time: "5 hours ago",
//       type: "info",
//     },
//   ];

//   // const bookingRequests = [
//   //   {
//   //     id: 1,
//   //     room: "301",
//   //     date: "2024-08-01",
//   //     time: "14:30-15:45",
//   //     status: "pending",
//   //     reason: "Group Study Session"
//   //   },
//   //   {
//   //     id: 2,
//   //     room: "Lab-1",
//   //     date: "2024-07-30",
//   //     time: "10:00-11:15",
//   //     status: "approved",
//   //     reason: "Project Demo"
//   //   }
//   // ];

//   const fetchRoutine = async () => {
//     try {
//       setLoading((prev) => ({ ...prev, routine: true }));
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/student/my/routine`, {
//         method: 'GET',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         credentials: 'include'
//       });
//       const data = await response.json();
//       setRoutine(data);
//     } catch (err) {
//       setError((prev) => ({ ...prev, routine: err.message }));
//     } finally {
//       setLoading((prev) => ({ ...prev, routine: false }));
//     }
//   };

//   // const fetchNotifications = async () => {
//   //   try {
//   //     setLoading(prev => ({...prev, notifications: true}));
//   //     const response = await fetch('/api/notifications');
//   //     const data = await response.json();
//   //     setNotifications(data);
//   //   } catch (err) {
//   //     setError(prev => ({...prev, notifications: err.message}));
//   //   } finally {
//   //     setLoading(prev => ({...prev, notifications: false}));
//   //   }
//   // };

//   const fetchBookingRequests = async () => {
//     try {
//       setLoading((prev) => ({ ...prev, bookings: true }));
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/student/my/bookings`, {
//         method: 'GET',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         credentials: 'include'
//       });
//       const data = await response.json();
//       setBookingRequests(data);
//     } catch (err) {
//       setError((prev) => ({ ...prev, bookings: err.message }));
//     } finally {
//       setLoading((prev) => ({ ...prev, bookings: false }));
//     }
//   };

//   const getStatusColor = (status: string) => {
//     switch (status) {
//       case "approved":
//         return "status-available";
//       case "pending":
//         return "status-pending";
//       case "rejected":
//         return "status-booked";
//       default:
//         return "";
//     }
//   };

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />

//       <div ref={profileRef} className="pt-32 pb-20 px-4">
//         <div className="container mx-auto max-w-6xl">
//           {/* Profile Header */}
//           <Card className="profile-section glass shadow-elegant mb-8">
//             <CardHeader>
//               <div className="flex items-center justify-between">
//                 <div className="flex items-center gap-4">
//                   <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center">
//                     <User className="h-8 w-8 text-primary-foreground" />
//                   </div>
//                   <div>
//                     <CardTitle className="text-2xl">
//                       {profile?.full_name}
//                     </CardTitle>
//                     <p className="text-muted-foreground">{profile?.email}</p>
//                     <div className="flex items-center gap-2 mt-2">
//                       <Badge variant="secondary" className="capitalize">
//                         {profile?.role}
//                       </Badge>
//                       {profile?.program && (
//                         <Badge variant="outline">{profile.program}</Badge>
//                       )}
//                       {profile?.semester && (
//                         <Badge variant="outline">
//                           Semester {profile.semester}
//                         </Badge>
//                       )}
//                     </div>
//                   </div>
//                 </div>

//                 <Button variant="outline" className="hover-scale">
//                   <Edit className="h-4 w-4 mr-2" />
//                   Edit Profile
//                 </Button>
//               </div>
//             </CardHeader>
//           </Card>

//           {/* Main Content */}
//           <Tabs defaultValue="schedule" className="space-y-6">
//             <TabsList className="glass">
//               <TabsTrigger value="schedule">My Schedule</TabsTrigger>
//               <TabsTrigger value="bookings">My Bookings</TabsTrigger>
//               <TabsTrigger value="notifications">Notifications</TabsTrigger>
//             </TabsList>

//             <TabsContent value="schedule">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Calendar className="h-5 w-5" />
//                     Weekly Schedule
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {loading.routine ? (
//                     <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
//                   ) : error.routine ? (
//                     <div className="text-center py-8 text-destructive">
//                       Failed to load schedule: {error.routine}
//                       <Button
//                         variant="ghost"
//                         // onClick={fetchRoutine}
//                         className="mt-2"
//                       >
//                         Retry
//                       </Button>
//                     </div>
//                   ) : routine.length === 0 ? (
//                     <div className="text-center py-8 text-muted-foreground">
//                       No schedule data available
//                     </div>
//                   ) : (
//                     <div className="space-y-6">
//                       {routine.map((day) => (
//                         <div key={day.day} className="space-y-3">
//                           <h3 className="font-semibold text-lg">{day.day}</h3>
//                           <div className="grid gap-3">
//                             {day.classes.map((cls, index) => (
//                               <div
//                                 key={index}
//                                 className={`p-4 rounded-lg border-2 transition-all hover-lift ${
//                                   cls.type === "extra"
//                                     ? "border-warning/50 bg-warning/10"
//                                     : "border-primary/50 bg-primary/10"
//                                 }`}
//                               >
//                                 <div className="flex items-center justify-between">
//                                   <div>
//                                     <div className="font-semibold">
//                                       {cls.subject}
//                                     </div>
//                                     <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
//                                       <div className="flex items-center gap-1">
//                                         <Clock className="h-3 w-3" />
//                                         {cls.time}
//                                       </div>
//                                       <div className="flex items-center gap-1">
//                                         <MapPin className="h-3 w-3" />
//                                         Room {cls.room}
//                                       </div>
//                                     </div>
//                                   </div>
//                                   <Badge
//                                     variant={
//                                       cls.type === "extra"
//                                         ? "secondary"
//                                         : "outline"
//                                     }
//                                   >
//                                     {cls.type === "extra"
//                                       ? "Extra Class"
//                                       : "Regular"}
//                                   </Badge>
//                                 </div>
//                               </div>
//                             ))}
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="bookings">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <BookOpen className="h-5 w-5" />
//                     My Booking Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {loading.bookings ? (
//                     <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
//                   ) : error.bookings ? (
//                     <div className="text-center py-8 text-destructive">
//                       Failed to load schedule: {error.bookings}
//                       <Button
//                         variant="ghost"
//                         onClick={fetchBookingRequests}
//                         className="mt-2"
//                       >
//                         Retry
//                       </Button>
//                     </div>
//                   ) : routine.length === 0 ? (
//                     <div className="text-center py-8 text-muted-foreground">
//                       No booking data available
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {bookingRequests.map((booking) => (
//                         <div
//                           key={booking.id}
//                           className="flex items-center justify-between p-4 border rounded-lg hover-lift"
//                         >
//                           <div className="space-y-1">
//                             <div className="flex items-center gap-4">
//                               <span className="font-semibold">
//                                 Room {booking.room}
//                               </span>
//                               <span className="text-sm text-muted-foreground">
//                                 {booking.date} • {booking.time}
//                               </span>
//                             </div>
//                             <p className="text-sm text-muted-foreground">
//                               {booking.reason}
//                             </p>
//                           </div>

//                           <Badge
//                             className={`capitalize ${getStatusColor(
//                               booking.status
//                             )}`}
//                           >
//                             {booking.status}
//                           </Badge>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="notifications">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Bell className="h-5 w-5" />
//                     Notifications
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="space-y-4">
//                     {notifications.map((notification) => (
//                       <div
//                         key={notification.id}
//                         className="flex items-start gap-4 p-4 border rounded-lg hover-lift"
//                       >
//                         <div className="flex-1">
//                           <h4 className="font-semibold">
//                             {notification.title}
//                           </h4>
//                           <p className="text-sm text-muted-foreground">
//                             {notification.message}
//                           </p>
//                           <span className="text-xs text-muted-foreground">
//                             {notification.time}
//                           </span>
//                         </div>
//                       </div>
//                     ))}
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
//   User,
//   Calendar,
//   BookOpen,
//   Bell,
//   Clock,
//   MapPin,
//   Edit,
//   X,
//   MoreHorizontal,
// } from "lucide-react";
// import { gsap } from "gsap";
// import {
//   DropdownMenu,
//   DropdownMenuContent,
//   DropdownMenuItem,
//   DropdownMenuTrigger,
// } from "@/components/ui/dropdown-menu";
// import {
//   AlertDialog,
//   AlertDialogAction,
//   AlertDialogCancel,
//   AlertDialogContent,
//   AlertDialogDescription,
//   AlertDialogFooter,
//   AlertDialogHeader,
//   AlertDialogTitle,
// } from "@/components/ui/alert-dialog";
// import { toast } from "sonner";

// export default function Profile() {
//   const { user, profile } = useAuth();
//   const navigate = useNavigate();
//   const profileRef = useRef<HTMLDivElement>(null);

//   const [routine, setRoutine] = useState<any[]>([]);
//   const [bookingRequests, setBookingRequests] = useState<any[]>([]);
//   const [loading, setLoading] = useState({
//     routine: false,
//     notifications: false,
//     bookings: true,
//   });
//   const [error, setError] = useState({
//     routine: null,
//     notifications: null,
//     bookings: null,
//   });
//   const [cancelBookingId, setCancelBookingId] = useState<string | null>(null);
//   const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);

//   useEffect(() => {
//     if (!user) {
//       navigate("/auth");
//       return;
//     }

//     fetchRoutine();
//     fetchBookingRequests();
//   }, [user, navigate]);

//   useEffect(() => {
//     if (profileRef.current) {
//       gsap.fromTo(
//         ".profile-section",
//         { y: 30, opacity: 0 },
//         { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
//       );
//     }
//   }, []);

//   const renderLoadingSkeleton = (count: number) => {
//     return Array(count)
//       .fill(0)
//       .map((_, i) => (
//         <div key={i} className="p-4 border rounded-lg animate-pulse">
//           <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
//           <div className="h-3 bg-gray-200 rounded w-1/2"></div>
//         </div>
//       ));
//   };

//   const notifications = [
//     {
//       id: 1,
//       title: "Booking Approved",
//       message: "Your booking for Room AB2 302 has been approved",
//       time: "2 hours ago",
//       type: "success",
//     },
//     {
//       id: 2,
//       title: "New Schedule Update",
//       message: "Extra class added for tomorrow",
//       time: "5 hours ago",
//       type: "info",
//     },
//   ];

//   const fetchRoutine = async () => {
//     try {
//       setLoading((prev) => ({ ...prev, routine: true }));
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/student/my/routine`, {
//         method: 'GET',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         credentials: 'include'
//       });
//       const data = await response.json();
//       setRoutine(data);
//     } catch (err) {
//       setError((prev) => ({ ...prev, routine: err.message }));
//     } finally {
//       setLoading((prev) => ({ ...prev, routine: false }));
//     }
//   };

//   const fetchBookingRequests = async () => {
//     try {
//       setLoading((prev) => ({ ...prev, bookings: true }));
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/student/my/bookings`, {
//         method: 'GET',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         credentials: 'include'
//       });
//       const data = await response.json();
//       setBookingRequests(data);
//     } catch (err) {
//       setError((prev) => ({ ...prev, bookings: err.message }));
//     } finally {
//       setLoading((prev) => ({ ...prev, bookings: false }));
//     }
//   };

//   const handleCancelBooking = async () => {
//     if (!cancelBookingId) return;

//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch(`http://localhost:8082/student/my/bookings/${cancelBookingId}`, {
//         method: 'DELETE',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         credentials: 'include'
//       });

//       if (response.ok) {
//         toast.success("Booking cancelled successfully");
//         // Remove the cancelled booking from the list
//         setBookingRequests(prev => prev.filter(booking => booking.id !== cancelBookingId));
//       } else {
//         throw new Error("Failed to cancel booking");
//       }
//     } catch (err) {
//       toast.error("Failed to cancel booking");
//       console.error("Cancel booking error:", err);
//     } finally {
//       setIsCancelDialogOpen(false);
//       setCancelBookingId(null);
//     }
//   };

//   const openCancelDialog = (bookingId: string) => {
//     setCancelBookingId(bookingId);
//     setIsCancelDialogOpen(true);
//   };

//   const getStatusColor = (status: string) => {
//     switch (status) {
//       case "approved":
//         return "status-available";
//       case "pending":
//         return "status-pending";
//       case "rejected":
//         return "status-booked";
//       case "cancelled":
//         return "status-booked";
//       default:
//         return "";
//     }
//   };

//   const canCancelBooking = (booking: any) => {
//     // Only allow cancellation for pending or approved bookings that haven't passed
//     // return ["pending", "approved"].includes(booking.status);
//     // Allow all to be cancelled 
//     return true;
//   };

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />

//       <div ref={profileRef} className="pt-32 pb-20 px-4">
//         <div className="container mx-auto max-w-6xl">
//           {/* Profile Header */}
//           <Card className="profile-section glass shadow-elegant mb-8">
//             <CardHeader>
//               <div className="flex items-center justify-between">
//                 <div className="flex items-center gap-4">
//                   <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center">
//                     <User className="h-8 w-8 text-primary-foreground" />
//                   </div>
//                   <div>
//                     <CardTitle className="text-2xl">
//                       {profile?.full_name}
//                     </CardTitle>
//                     <p className="text-muted-foreground">{profile?.email}</p>
//                     <div className="flex items-center gap-2 mt-2">
//                       <Badge variant="secondary" className="capitalize">
//                         {profile?.role}
//                       </Badge>
//                       {profile?.program && (
//                         <Badge variant="outline">{profile.program}</Badge>
//                       )}
//                       {profile?.semester && (
//                         <Badge variant="outline">
//                           Semester {profile.semester}
//                         </Badge>
//                       )}
//                     </div>
//                   </div>
//                 </div>

//                 {/* <Button variant="outline" className="hover-scale">
//                   <Edit className="h-4 w-4 mr-2" />
//                   Edit Profile
//                 </Button> */}
//               </div>
//             </CardHeader>
//           </Card>

//           {/* Main Content */}
//           <Tabs defaultValue="schedule" className="space-y-6">
//             <TabsList className="glass">
//               <TabsTrigger value="schedule">My Schedule</TabsTrigger>
//               <TabsTrigger value="bookings">My Bookings</TabsTrigger>
//               <TabsTrigger value="notifications">Notifications</TabsTrigger>
//             </TabsList>

//             <TabsContent value="schedule">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Calendar className="h-5 w-5" />
//                     Weekly Schedule
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {loading.routine ? (
//                     <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
//                   ) : error.routine ? (
//                     <div className="text-center py-8 text-destructive">
//                       Failed to load schedule: {error.routine}
//                       <Button
//                         variant="ghost"
//                         className="mt-2"
//                       >
//                         Retry
//                       </Button>
//                     </div>
//                   ) : routine.length === 0 ? (
//                     <div className="text-center py-8 text-muted-foreground">
//                       No schedule data available
//                     </div>
//                   ) : (
//                     <div className="space-y-6">
//                       {routine.map((day) => (
//                         <div key={day.day} className="space-y-3">
//                           <h3 className="font-semibold text-lg">{day.day}</h3>
//                           <div className="grid gap-3">
//                             {day.classes.map((cls, index) => (
//                               <div
//                                 key={index}
//                                 className={`p-4 rounded-lg border-2 transition-all hover-lift ${
//                                   cls.type === "extra"
//                                     ? "border-warning/50 bg-warning/10"
//                                     : "border-primary/50 bg-primary/10"
//                                 }`}
//                               >
//                                 <div className="flex items-center justify-between">
//                                   <div>
//                                     <div className="font-semibold">
//                                       {cls.subject}
//                                     </div>
//                                     <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
//                                       <div className="flex items-center gap-1">
//                                         <Clock className="h-3 w-3" />
//                                         {cls.time}
//                                       </div>
//                                       <div className="flex items-center gap-1">
//                                         <MapPin className="h-3 w-3" />
//                                         Room {cls.room}
//                                       </div>
//                                     </div>
//                                   </div>
//                                   <Badge
//                                     variant={
//                                       cls.type === "extra"
//                                         ? "secondary"
//                                         : "outline"
//                                     }
//                                   >
//                                     {cls.type === "extra"
//                                       ? "Extra Class"
//                                       : "Regular"}
//                                   </Badge>
//                                 </div>
//                               </div>
//                             ))}
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="bookings">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <BookOpen className="h-5 w-5" />
//                     My Booking Requests
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   {loading.bookings ? (
//                     <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
//                   ) : error.bookings ? (
//                     <div className="text-center py-8 text-destructive">
//                       Failed to load bookings: {error.bookings}
//                       <Button
//                         variant="ghost"
//                         onClick={fetchBookingRequests}
//                         className="mt-2"
//                       >
//                         Retry
//                       </Button>
//                     </div>
//                   ) : bookingRequests.length === 0 ? (
//                     <div className="text-center py-8 text-muted-foreground">
//                       No booking data available
//                     </div>
//                   ) : (
//                     <div className="space-y-4">
//                       {bookingRequests.map((booking) => (
//                         <div
//                           key={booking.id}
//                           className="group flex items-center justify-between p-4 border rounded-lg hover-lift transition-all duration-200"
//                         >
//                           <div className="flex-1 space-y-1">
//                             <div className="flex items-center gap-4">
//                               <span className="font-semibold">
//                                 Room {booking.room}
//                               </span>
//                               <span className="text-sm text-muted-foreground">
//                                 {booking.date} • {booking.time}
//                               </span>
//                             </div>
//                             <p className="text-sm text-muted-foreground">
//                               {booking.reason}
//                             </p>
//                           </div>

//                           <div className="flex items-center gap-3">
//                             <Badge
//                               className={`capitalize ${getStatusColor(
//                                 booking.status
//                               )}`}
//                             >
//                               {booking.status}
//                             </Badge>

//                             {canCancelBooking(booking) && (
//                               <DropdownMenu>
//                                 <DropdownMenuTrigger asChild>
//                                   <Button
//                                     variant="ghost"
//                                     size="icon"
//                                     className="h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity duration-200 hover:bg-destructive/10 hover:text-destructive"
//                                   >
//                                     <MoreHorizontal className="h-4 w-4" />
//                                   </Button>
//                                 </DropdownMenuTrigger>
//                                 <DropdownMenuContent align="end">
//                                   <DropdownMenuItem
//                                     className="text-destructive focus:text-destructive"
//                                     onClick={() => openCancelDialog(booking.id)}
//                                   >
//                                     <X className="h-4 w-4 mr-2" />
//                                     Cancel Booking
//                                   </DropdownMenuItem>
//                                 </DropdownMenuContent>
//                               </DropdownMenu>
//                             )}
//                           </div>
//                         </div>
//                       ))}
//                     </div>
//                   )}
//                 </CardContent>
//               </Card>
//             </TabsContent>

//             <TabsContent value="notifications">
//               <Card className="profile-section glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Bell className="h-5 w-5" />
//                     Notifications
//                   </CardTitle>
//                 </CardHeader>
//                 <CardContent>
//                   <div className="space-y-4">
//                     {notifications.map((notification) => (
//                       <div
//                         key={notification.id}
//                         className="flex items-start gap-4 p-4 border rounded-lg hover-lift"
//                       >
//                         <div className="flex-1">
//                           <h4 className="font-semibold">
//                             {notification.title}
//                           </h4>
//                           <p className="text-sm text-muted-foreground">
//                             {notification.message}
//                           </p>
//                           <span className="text-xs text-muted-foreground">
//                             {notification.time}
//                           </span>
//                         </div>
//                       </div>
//                     ))}
//                   </div>
//                 </CardContent>
//               </Card>
//             </TabsContent>
//           </Tabs>
//         </div>
//       </div>

//       {/* Cancel Booking Confirmation Dialog */}
//       <AlertDialog open={isCancelDialogOpen} onOpenChange={setIsCancelDialogOpen}>
//         <AlertDialogContent>
//           <AlertDialogHeader>
//             <AlertDialogTitle>Cancel Booking</AlertDialogTitle>
//             <AlertDialogDescription>
//               Are you sure you want to cancel this booking? This action cannot be undone.
//             </AlertDialogDescription>
//           </AlertDialogHeader>
//           <AlertDialogFooter>
//             <AlertDialogCancel>Keep Booking</AlertDialogCancel>
//             <AlertDialogAction
//               onClick={handleCancelBooking}
//               className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
//             >
//               Cancel Booking
//             </AlertDialogAction>
//           </AlertDialogFooter>
//         </AlertDialogContent>
//       </AlertDialog>
//     </div>
//   );
// }

//// V3

import { useState, useEffect, useRef, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/hooks/useAuth";
import { useNavigate } from "react-router-dom";
import {
  User,
  Calendar,
  BookOpen,
  Clock,
  MapPin,
  Edit,
  X,
  MoreHorizontal,
  Shield,
} from "lucide-react";
import { gsap } from "gsap";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
import { toast } from "sonner";
import { apiUrl } from "@/lib/api";
import { NotificationFeed } from "@/components/NotificationFeed";

interface RoutineClass {
  subject: string;
  time: string;
  room: string;
  type: string;
}

interface RoutineDay {
  day: string;
  classes: RoutineClass[];
}

interface BookingRequest {
  id: string;
  room: string;
  date: string;
  time: string;
  status: string;
  reason: string;
}

const errorMessage = (error: unknown) =>
  error instanceof Error ? error.message : "An unexpected error occurred";

const responseErrorMessage = async (response: Response, fallback: string) => {
  const body = await response.text();
  if (!body) return fallback;

  try {
    const parsed: unknown = JSON.parse(body);
    if (typeof parsed === "string") return parsed;
    if (
      parsed &&
      typeof parsed === "object" &&
      "message" in parsed &&
      typeof parsed.message === "string"
    ) {
      return parsed.message;
    }
  } catch {
    // Plain-text error responses are useful as-is.
  }

  return body;
};

const isRoutine = (value: unknown): value is RoutineDay[] =>
  Array.isArray(value) &&
  value.every(
    (day) =>
      day !== null &&
      typeof day === "object" &&
      "day" in day &&
      typeof day.day === "string" &&
      "classes" in day &&
      Array.isArray(day.classes)
  );

const isBookingRequests = (value: unknown): value is BookingRequest[] =>
  Array.isArray(value) &&
  value.every(
    (booking) =>
      booking !== null &&
      typeof booking === "object" &&
      ["id", "room", "date", "time", "status", "reason"].every(
        (key) => key in booking && typeof booking[key as keyof typeof booking] === "string"
      )
  );

export default function Profile() {
  const { user, profile, loading: authLoading, signOut } = useAuth();
  const navigate = useNavigate();
  const profileRef = useRef<HTMLDivElement>(null);
  const hasStudentRole =
    user?.roles?.some((role) => role.toUpperCase() === "STUDENT") ?? false;
  const hasFacultyRole =
    user?.roles?.some((role) => role.toUpperCase() === "FACULTY") ?? false;

  const [routine, setRoutine] = useState<RoutineDay[]>([]);
  const [bookingRequests, setBookingRequests] = useState<BookingRequest[]>([]);
  const [loading, setLoading] = useState({
    routine: false,
    bookings: false,
    roleRequest: false,
  });
  const [error, setError] = useState({
    routine: null,
    bookings: null,
    roleRequest: null,
  });
  const [cancelBookingId, setCancelBookingId] = useState<string | null>(null);
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);
  const [roleRequestDialogOpen, setRoleRequestDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState("");

  useEffect(() => {
    if (profileRef.current) {
      gsap.fromTo(
        ".profile-section",
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
      );
    }
  }, []);

  const renderLoadingSkeleton = (count: number) => {
    return Array(count)
      .fill(0)
      .map((_, i) => (
        <div key={i} className="p-4 border rounded-lg animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
          <div className="h-3 bg-gray-200 rounded w-1/2"></div>
        </div>
      ));
  };

  const handleUnauthorized = useCallback(async () => {
    await signOut();
    navigate("/auth");
  }, [navigate, signOut]);

  const fetchRoutine = useCallback(async () => {
    try {
      setLoading((prev) => ({ ...prev, routine: true }));
      setError((prev) => ({ ...prev, routine: null }));
      const token = localStorage.getItem('auth_token');
      if (!token) {
        await handleUnauthorized();
        return;
      }
      const response = await fetch(apiUrl('/api/me/routine'), {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (response.status === 401) {
        await handleUnauthorized();
        return;
      }
      if (!response.ok) {
        throw new Error(await responseErrorMessage(response, "Failed to load schedule"));
      }

      const data: unknown = await response.json();
      if (!isRoutine(data)) {
        throw new Error("The server returned invalid schedule data");
      }
      setRoutine(data);
    } catch (err) {
      setError((prev) => ({ ...prev, routine: errorMessage(err) }));
    } finally {
      setLoading((prev) => ({ ...prev, routine: false }));
    }
  }, [handleUnauthorized]);

  const fetchBookingRequests = useCallback(async () => {
    try {
      setLoading((prev) => ({ ...prev, bookings: true }));
      setError((prev) => ({ ...prev, bookings: null }));
      const token = localStorage.getItem('auth_token');
      if (!token) {
        await handleUnauthorized();
        return;
      }
      const response = await fetch(apiUrl('/api/me/bookings'), {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (response.status === 401) {
        await handleUnauthorized();
        return;
      }
      if (!response.ok) {
        throw new Error(await responseErrorMessage(response, "Failed to load bookings"));
      }

      const data: unknown = await response.json();
      if (!isBookingRequests(data)) {
        throw new Error("The server returned invalid booking data");
      }
      setBookingRequests(data);
    } catch (err) {
      setError((prev) => ({ ...prev, bookings: errorMessage(err) }));
    } finally {
      setLoading((prev) => ({ ...prev, bookings: false }));
    }
  }, [handleUnauthorized]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      navigate("/auth");
      return;
    }
    if (hasFacultyRole) {
      navigate("/faculty");
      return;
    }

    if (hasStudentRole) {
      void fetchRoutine();
      void fetchBookingRequests();
    } else {
      setRoutine([]);
      setBookingRequests([]);
      setError((prev) => ({ ...prev, routine: null, bookings: null }));
      setLoading((prev) => ({ ...prev, routine: false, bookings: false }));
    }
  }, [authLoading, fetchBookingRequests, fetchRoutine, hasFacultyRole, hasStudentRole, navigate, user]);

  const handleCancelBooking = async () => {
    if (!cancelBookingId) return;

    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(apiUrl(`/api/me/bookings/${cancelBookingId}`), {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (response.ok) {
        toast.success("Booking cancelled successfully");
        setBookingRequests(prev => prev.filter(booking => booking.id !== cancelBookingId));
      } else {
        throw new Error("Failed to cancel booking");
      }
    } catch (err) {
      toast.error("Failed to cancel booking");
      console.error("Cancel booking error:", err);
    } finally {
      setIsCancelDialogOpen(false);
      setCancelBookingId(null);
    }
  };

  const handleRoleRequest = async () => {
    if (!selectedRole) return;

    try {
      setLoading((prev) => ({ ...prev, roleRequest: true }));
      const token = localStorage.getItem('auth_token');
      const response = await fetch(apiUrl('/api/role-requests'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          requestedRole: selectedRole,
          reason: `Requesting role change to ${selectedRole}`
        }),
        credentials: 'include'
      });

      if (response.ok) {
        toast.success("Role request submitted successfully");
        setRoleRequestDialogOpen(false);
        setSelectedRole("");
      } else {
        throw new Error("Failed to submit role request");
      }
    } catch (err) {
      toast.error("Failed to submit role request");
      setError((prev) => ({ ...prev, roleRequest: errorMessage(err) }));
    } finally {
      setLoading((prev) => ({ ...prev, roleRequest: false }));
    }
  };

  const openCancelDialog = (bookingId: string) => {
    setCancelBookingId(bookingId);
    setIsCancelDialogOpen(true);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "approved":
        return "status-available";
      case "pending":
        return "status-pending";
      case "rejected":
        return "status-booked";
      case "cancelled":
        return "status-booked";
      default:
        return "";
    }
  };

  const canCancelBooking = (_booking: BookingRequest) => {
    return true;
  };

  // Define available roles that users can request
  const availableRoles = ["cr", "student"];

  if (authLoading) {
    return (
      <div className="min-h-screen gradient-surface flex items-center justify-center">
        <div className="text-muted-foreground">Loading profile...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen gradient-surface">
      <Navbar />

      <div ref={profileRef} className="pt-32 pb-20 px-4">
        <div className="container mx-auto max-w-6xl">
          {/* Profile Header */}
          <Card className="profile-section glass shadow-elegant mb-8">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center">
                    <User className="h-8 w-8 text-primary-foreground" />
                  </div>
                  <div>
                    <CardTitle className="text-2xl">
                      {profile?.full_name}
                    </CardTitle>
                    <p className="text-muted-foreground">{profile?.email}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="secondary" className="capitalize">
                        {profile?.role}
                      </Badge>
                      {profile?.program && (
                        <Badge variant="outline">{profile.program}</Badge>
                      )}
                      {profile?.semester && (
                        <Badge variant="outline">
                          Semester {profile.semester}
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>

                {hasStudentRole && <div className="flex items-center gap-2">
                  <Button 
                    variant="outline" 
                    className="hover-scale"
                    onClick={() => setRoleRequestDialogOpen(true)}
                  >
                    <Shield className="h-4 w-4 mr-2" />
                    Request Role Change
                  </Button>
                </div>}
              </div>
            </CardHeader>
          </Card>

          {/* Main Content */}
          <Tabs defaultValue={hasStudentRole ? "schedule" : "notifications"} className="space-y-6">
            <TabsList className="glass">
              {hasStudentRole && <TabsTrigger value="schedule">My Schedule</TabsTrigger>}
              {hasStudentRole && <TabsTrigger value="bookings">My Bookings</TabsTrigger>}
              <TabsTrigger value="notifications">Notifications</TabsTrigger>
            </TabsList>

            {hasStudentRole && <TabsContent value="schedule">
              <Card className="profile-section glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Calendar className="h-5 w-5" />
                    Weekly Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {loading.routine ? (
                    <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
                  ) : error.routine ? (
                    <div className="text-center py-8 text-destructive">
                      Failed to load schedule: {error.routine}
                      <Button
                        variant="ghost"
                        onClick={fetchRoutine}
                        className="mt-2"
                      >
                        Retry
                      </Button>
                    </div>
                  ) : routine.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      No schedule data available
                    </div>
                  ) : (
                    <div className="space-y-6">
                      {routine.map((day) => (
                        <div key={day.day} className="space-y-3">
                          <h3 className="font-semibold text-lg">{day.day}</h3>
                          <div className="grid gap-3">
                            {day.classes.map((cls, index) => (
                              <div
                                key={index}
                                className={`p-4 rounded-lg border-2 transition-all hover-lift ${
                                  cls.type === "extra"
                                    ? "border-warning/50 bg-warning/10"
                                    : "border-primary/50 bg-primary/10"
                                }`}
                              >
                                <div className="flex items-center justify-between">
                                  <div>
                                    <div className="font-semibold">
                                      {cls.subject}
                                    </div>
                                    <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                                      <div className="flex items-center gap-1">
                                        <Clock className="h-3 w-3" />
                                        {cls.time}
                                      </div>
                                      <div className="flex items-center gap-1">
                                        <MapPin className="h-3 w-3" />
                                        Room {cls.room}
                                      </div>
                                    </div>
                                  </div>
                                  <Badge
                                    variant={
                                      cls.type === "extra"
                                        ? "secondary"
                                        : "outline"
                                    }
                                  >
                                    {cls.type === "extra"
                                      ? "Extra Class"
                                      : "Regular"}
                                  </Badge>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>}

            {hasStudentRole && <TabsContent value="bookings">
              <Card className="profile-section glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BookOpen className="h-5 w-5" />
                    My Booking Requests
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {loading.bookings ? (
                    <div className="space-y-4">{renderLoadingSkeleton(3)}</div>
                  ) : error.bookings ? (
                    <div className="text-center py-8 text-destructive">
                      Failed to load bookings: {error.bookings}
                      <Button
                        variant="ghost"
                        onClick={fetchBookingRequests}
                        className="mt-2"
                      >
                        Retry
                      </Button>
                    </div>
                  ) : bookingRequests.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      No booking data available
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {bookingRequests.map((booking) => (
                        <div
                          key={booking.id}
                          className="group flex items-center justify-between p-4 border rounded-lg hover-lift transition-all duration-200"
                        >
                          <div className="flex-1 space-y-1">
                            <div className="flex items-center gap-4">
                              <span className="font-semibold">
                                Room {booking.room}
                              </span>
                              <span className="text-sm text-muted-foreground">
                                {booking.date} • {booking.time}
                              </span>
                            </div>
                            <p className="text-sm text-muted-foreground">
                              {booking.reason}
                            </p>
                          </div>

                          <div className="flex items-center gap-3">
                            <Badge
                              className={`capitalize ${getStatusColor(
                                booking.status
                              )}`}
                            >
                              {booking.status}
                            </Badge>

                            {canCancelBooking(booking) && (
                              <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity duration-200 hover:bg-destructive/10 hover:text-destructive"
                                  >
                                    <MoreHorizontal className="h-4 w-4" />
                                  </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                  <DropdownMenuItem
                                    className="text-destructive focus:text-destructive"
                                    onClick={() => openCancelDialog(booking.id)}
                                  >
                                    <X className="h-4 w-4 mr-2" />
                                    Cancel Booking
                                  </DropdownMenuItem>
                                </DropdownMenuContent>
                              </DropdownMenu>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>}

            <TabsContent value="notifications">
              <NotificationFeed />
            </TabsContent>
          </Tabs>
        </div>
      </div>

      {/* Cancel Booking Confirmation Dialog */}
      <AlertDialog open={isCancelDialogOpen} onOpenChange={setIsCancelDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel Booking</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to cancel this booking? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Keep Booking</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancelBooking}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Cancel Booking
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Role Request Dialog */}
      {hasStudentRole && <AlertDialog open={roleRequestDialogOpen} onOpenChange={setRoleRequestDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Request Role Change</AlertDialogTitle>
            <AlertDialogDescription>
              Select the role you would like to request. Your request will be reviewed by an administrator.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="py-4">
            <div className="grid gap-2">
              {availableRoles.map((role) => (
                <Button
                  key={role}
                  variant={selectedRole === role ? "default" : "outline"}
                  onClick={() => setSelectedRole(role)}
                  className="capitalize"
                >
                  {role}
                </Button>
              ))}
            </div>
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleRoleRequest}
              disabled={!selectedRole || loading.roleRequest}
            >
              {loading.roleRequest ? "Submitting..." : "Submit Request"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>}
    </div>
  );
}
