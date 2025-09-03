// import { useState, useEffect, useRef } from "react";
// import { useParams, useNavigate } from "react-router-dom";
// import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Badge } from "@/components/ui/badge";
// import { Navbar } from "@/components/Navbar";
// import { useClassrooms } from "@/hooks/useClassrooms";
// import { useBookings } from "@/hooks/useBookings";
// import { ArrowLeft, ArrowRight, Calendar, Clock, MapPin, Users } from "lucide-react";
// import { gsap } from "gsap";
// import { start } from "repl";

// const toMinutes = (t: string) => {
//   const [h, m] = t.split(":").map(Number);
//   return h * 60 + m;
// };
// //
// ////
// ////// Need the fix the Logic of two slots classes
// ////
// //
// const overlaps = (
//   aStart: string,
//   aEnd: string,
//   bStart: string,
//   bEnd: string
// ) => {
//   // const a1 = toMinutes(aStart);
//   // const a2 = toMinutes(aEnd);
//   // const b1 = toMinutes(bStart);
//   // const b2 = toMinutes(bEnd);
//   // return Math.max(a1, b1) < Math.min(a2, b2);
//   return aStart === bStart;
// };

// const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"];

// type SlotStatus = "available" | "booked" | "pending" | "past";

// type Slot = { label: string; start: string; end: string };

// const timeSlots: Slot[] = [
//   { label: "08:00", start: "08:00", end: "09:15" },
//   { label: "09:15", start: "09:15", end: "10:30" },
//   { label: "10:30", start: "10:30", end: "11:45" },
//   { label: "11:45", start: "11:45", end: "13:00" },
//   { label: "14:30", start: "14:30", end: "15:45" },
//   { label: "15:45", start: "15:45", end: "17:00" },
// ];

// export default function ClassroomSchedule() {
//   const { id } = useParams<{ id: string }>();
//   const navigate = useNavigate();
//   const [WeekOffset, setWeekOffset] = useState(0); // 0 = current week, 1 = next week, etc.
//   const { classrooms } = useClassrooms();
//   const { bookings, routines, nextBookings } = useBookings(id);
//   const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null); // TimeSlot
//   const calendarRef = useRef<HTMLDivElement>(null);

//   const classroom = classrooms.find((c) => String(c.id) === String(id));

//   useEffect(() => {
//     if (calendarRef.current) {
//       gsap.fromTo(
//         ".time-slot",
//         { scale: 0.9, opacity: 0 },
//         {
//           scale: 1,
//           opacity: 1,
//           duration: 0.5,
//           stagger: 0.02,
//           ease: "power2.out",
//         }
//       );
//     }
//   }, []);

//   const formatDateForInput = (date: Date) => {
//     const year = date.getFullYear();
//     const month = String(date.getMonth() + 1).padStart(2, "0"); // Months are 0-indexed
//     const day = String(date.getDate()).padStart(2, "0");
//     return `${year}-${month}-${day}`; // YYYY-MM-DD format for <input type="date">
//   };

//   // const handleSlotClick = (
//   //   day: string,
//   //   start_time: string,
//   //   end_time: string,
//   //   week: number
//   // ) => {
//   //   const slot: Slot = timeSlots.find((x) => x.start === start_time);
//   //   const status = getNextWeekSlotStatus(day, slot).status;

//   //   if (status === "available" && week === 2) {
//   //     const dayIndex = [
//   //       "Sunday",
//   //       "Monday",
//   //       "Tuesday",
//   //       "Wednesday",
//   //       "Thursday",
//   //       "Friday",
//   //       "Saturday",
//   //     ].indexOf(day);

//   //     const today = new Date();
//   //     const todayMidnight = new Date(
//   //       today.getFullYear(),
//   //       today.getMonth(), // Month is ZERO indexed
//   //       today.getDate()
//   //     );

//   //     const slotDate = new Date(todayMidnight);
//   //     slotDate.setDate(slotDate.getDate() - slotDate.getDay() + dayIndex);
//   //     // slotDate = Sunday(day = 0) + dayIndex

//   //     // const formattedDate = slotDate.toISOString().split('T')[0]; // As my local time is ahead of UTC it was reducing the date by one
//   //     const formattedDate = formatDateForInput(slotDate);

//   //     setSelectedSlot(slot);
//   //     navigate(
//   //       `/booking/${id}?day=${day}&start_time=${start_time}&end_time=${end_time}&slotDate=${formattedDate}`
//   //     );
//   //   } else if (status === "available" && week === 1) {
//   //     const today = new Date();
//   //     const todayMidnight = new Date(
//   //       today.getFullYear(),
//   //       today.getMonth(),
//   //       today.getDate()
//   //     );
//   //     // MONDAY FIRST DAY OF THE WEEK
//   //     const dayIndex = [
//   //       "Monday",
//   //       "Tuesday",
//   //       "Wednesday",
//   //       "Thursday",
//   //       "Friday",
//   //       "Sunday",
//   //       "Saturday",
//   //     ].indexOf(day);
//   //     if (dayIndex === -1) throw new Error(`Invalid day: ${day}`);

//   //     const monday = new Date(todayMidnight);
//   //     monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7));

//   //     const slotDate = new Date(monday);
//   //     slotDate.setDate(monday.getDate() + dayIndex);

//   //     const formattedDate = formatDateForInput(slotDate);

//   //     setSelectedSlot(slot);
//   //     navigate(
//   //       `/booking/${id}?day=${day}&start_time=${start_time}&end_time=${end_time}&slotDate=${formattedDate}`
//   //     );
//   //   }
//   // };

//   const handleSlotClick = (
//   day: string,
//   start_time: string,
//   end_time: string,
//   week: number
// ) => {
//   const slot: Slot = timeSlots.find((x) => x.start === start_time);
//   const status = getNextWeekSlotStatus(day, slot).status;

//   if (status === "available" && (week === 1 || week === 2)) {
//     const dayIndex = [
//       "Sunday",
//       "Monday",
//       "Tuesday",
//       "Wednesday",
//       "Thursday",
//       "Friday",
//       "Saturday",
//     ].indexOf(day);

//     const today = new Date();
//     const todayMidnight = new Date(
//       today.getFullYear(),
//       today.getMonth(),
//       today.getDate()
//     );

//     // Calculate the start of the week (Sunday)
//     const sunday = new Date(todayMidnight);
//     sunday.setDate(sunday.getDate() - sunday.getDay()); // Go back to Sunday

//     // Add 7 days for each week offset (week 1 = next week, week 2 = week after next)
//     const targetSunday = new Date(sunday);
//     targetSunday.setDate(sunday.getDate() + (WeekOffset * 7));

//     // Calculate the specific slot date
//     const slotDate = new Date(targetSunday);
//     slotDate.setDate(targetSunday.getDate() + dayIndex);

//     const formattedDate = formatDateForInput(slotDate);

//     setSelectedSlot(slot);
//     navigate(
//       `/booking/${id}?day=${day}&start_time=${start_time}&end_time=${end_time}&slotDate=${formattedDate}`
//     );
//   }
// };

//   const getStatusColor = (status: string) => {
//     switch (status) {
//       case "available":
//         return "status-available";
//       case "booked":
//         return "status-booked";
//       case "pending":
//         return "status-pending";
//       case "past":
//         return "status-past";
//       default:
//         return "";
//     }
//   };

//   // Core Functions
// function getWeeklySlotStatus(
//   day: string,
//   slot: Slot
// ): { status: SlotStatus; course_code?: string; faculty_name?: string } {
//   /// Setting past as grey
//   const today = new Date();
//   const todayMidnight = new Date(
//     today.getFullYear(),
//     today.getMonth(),
//     today.getDate()
//   );
//   // MONDAY FIRST DAY OF THE WEEK
//   const dayIndex = [
//     "Monday",
//     "Tuesday",
//     "Wednesday",
//     "Thursday",
//     "Friday",
//     "Sunday",
//     "Saturday",
//   ].indexOf(day);
//   if (dayIndex === -1) throw new Error(`Invalid day: ${day}`);

//   // Get Monday of this week // Monday is start of the week
//   const monday = new Date(todayMidnight);
//   monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7)); // adjust to Monday

//   // Actual date for the given "day"
//   const slotDate = new Date(monday);
//   slotDate.setDate(monday.getDate() + dayIndex);

//   if (slotDate < todayMidnight) {
//     return { status: "past" as SlotStatus }; // Handle "past" style in UI
//   }

//   // 1) Weekly routine wins (treat as booked)
//   const r = routines.find(
//     (x) =>
//       x.day === day &&
//       overlaps(slot.start, slot.end, x.start_time, x.end_time)
//   );
//   if (r) {
//     return {
//       status: "booked",
//       course_code: r.course_code,
//       faculty_name: r.faculty_name,
//     };
//   }

//   // 2) Bookings for this week (backend already filtered to the week)
//   const relevant = bookings.filter((b) =>
//     b.day === day &&
//     overlaps(slot.start, slot.end, b.start_time, b.end_time)
//   );

//   if (relevant.length > 0) {
//     // prioritize statuses: pending < approved < rejected (rejected doesn't block)
//     const hasApproved = relevant.find((b) => b.status === "booked");
//     const hasPending = relevant.find((b) => b.status === "pending");
//     if (hasApproved) return {
//       status: "booked",
//       course_code: hasApproved.course_code,
//       faculty_name: hasApproved.faculty_name,
//     };
//     if (hasPending) return {
//       status: "pending",
//       course_code: hasPending.course_code,
//       faculty_name: hasPending.faculty_name,
//     };
//   }

//   return { status: "available" };
// }

// function getNextWeekSlotStatus(
//   day: string,
//   slot: Slot
// ): { status: SlotStatus; course_code?: string; faculty_name?: string } {
//   /// Setting past as grey
//   const today = new Date();
//   const todayMidnight = new Date(
//     today.getFullYear(),
//     today.getMonth(),
//     today.getDate()
//   );

//   const dayIndex = [
//     "Sunday",
//     "Monday",
//     "Tuesday",
//     "Wednesday",
//     "Thursday",
//     "Friday",
//     "Saturday",
//   ].indexOf(day);
//   if (dayIndex === -1) throw new Error(`Invalid day: ${day}`);

//   // Get Sunday of this week
//   const sunday = new Date(todayMidnight);
//   sunday.setDate(sunday.getDate() - sunday.getDay()); // go back to Sunday

//   // Actual date for the given "day"
//   const slotDate = new Date(sunday);
//   slotDate.setDate(sunday.getDate() + dayIndex);

//   const r = routines.find(
//     (x) =>
//       x.day === day &&
//       overlaps(slot.start, slot.end, x.start_time, x.end_time)
//   );
//   if (r) {
//     return {
//       status: "booked",
//       course_code: r.course_code,
//       faculty_name: r.faculty_name,
//     };
//   }

//   // 2) Bookings for next week (backend already filtered to the week)
//   const relevant = nextBookings.filter((b) =>
//     overlaps(slot.start, slot.end, b.start_time, b.end_time)
//   );

//   if (relevant.length > 0) {
//     // prioritize statuses: pending < approved < rejected (rejected doesn't block)
//     const hasApproved = relevant.some((b) => b.status === "booked");
//     const hasPending = relevant.some((b) => b.status === "pending");
//     if (hasApproved) return { status: "booked" };
//     if (hasPending) return { status: "pending" };
//   }

//   return { status: "available" };
// }

//   if (!classroom) {
//     return (
//       <div className="min-h-screen gradient-surface">
//         <Navbar />
//         <div className="pt-32 text-center">
//           <p className="text-xl text-muted-foreground">Classroom not found</p>
//         </div>
//       </div>
//     );
//   }

//   return (
//     <div className="min-h-screen gradient-surface">
//       <Navbar />

//       <div className="pt-32 pb-20 px-4">
//         <div className="container mx-auto">
//           <Button
//             variant="ghost"
//             onClick={() => navigate("/")}
//             className="mb-6 hover-scale"
//           >
//             <ArrowLeft className="mr-2 h-4 w-4" />
//             Back to Classrooms
//           </Button>

//           {/* Classroom Info */}
//           <Card className="glass shadow-elegant mb-8">
//             <CardHeader>
//               <CardTitle className="flex items-center gap-4">
//                 <span className="text-3xl font-bold text-primary">
//                   Room {classroom.room_number}
//                 </span>
//                 <Badge variant="secondary" className="glass">
//                   Available
//                 </Badge>
//               </CardTitle>

//               <div className="flex flex-wrap gap-6 text-muted-foreground mt-4">
//                 <div className="flex items-center gap-2">
//                   <MapPin className="h-4 w-4" />
//                   {classroom.building}
//                 </div>
//                 <div className="flex items-center gap-2">
//                   <Users className="h-4 w-4" />
//                   Capacity: {classroom.capacity}
//                 </div>
//                 <div className="flex items-center gap-2">
//                   <Calendar className="h-4 w-4" />
//                   Weekly Schedule
//                 </div>
//               </div>
//             </CardHeader>
//           </Card>

//           {/* Schedule Grid */}
//           <Card ref={calendarRef} className="glass shadow-elegant">
//             <CardHeader>
//               <CardTitle className="flex items-center gap-2">
//                 <Clock className="h-5 w-5" />
//                 Weekly Schedule
//               </CardTitle>
//             </CardHeader>

//             <CardContent>
//               <div className="overflow-x-auto">
//                 <div className="min-w-[800px]">
//                   {/* Header */}
//                   <div className="grid grid-cols-6 gap-2 mb-4">
//                     <div className="p-3 font-semibold text-center">Time</div>
//                     {days.map((day) => (
//                       <div
//                         key={`day-${day}`}
//                         className="p-3 font-semibold text-center"
//                       >
//                         {day}
//                       </div>
//                     ))}
//                   </div>
//                   {timeSlots.map((slot) => (
//                     <div
//                       key={`row-${slot.start}-${slot.end}`}
//                       className="grid grid-cols-6 gap-2 mb-2"
//                     >
//                       <div className="p-3 font-medium text-center bg-muted rounded-lg">
//                         {slot.label}
//                       </div>
//                       {days.map((day) => {
//                         const { status, course_code, faculty_name } =
//                           getWeeklySlotStatus(day, slot);
//                         return (
//                           <div
//                             key={`cell-${day}-${slot.start}-${slot.end}`}
//                             className={`time-slot p-3 rounded-lg border-2 cursor-pointer transition-all duration-200 ${getStatusColor(
//                               status
//                             )} ${
//                               status === "available"
//                                 ? "hover-scale hover:shadow-soft"
//                                 : status === "past"
//                                 ? "cursor-not-allowed opacity-50" // <--- softer look for past
//                                 : "cursor-not-allowed opacity-75"
//                             }`}
//                             onClick={() =>
//                               status === "available" &&
//                               handleSlotClick(day, slot.start, slot.end, 1)
//                             }
//                           >
//                             <div className="text-center">
//                               <div className="text-sm font-medium capitalize">
//                                 {status}
//                               </div>
//                               {status !== "available" && course_code && (
//                                 <div className="text-xs mt-1 opacity-70">
//                                   {course_code}
//                                 </div>
//                               )}
//                             </div>
//                           </div>
//                         );
//                       })}
//                     </div>
//                   ))}
//                 </div>
//               </div>

//               {/* Legend */}
//               <div className="flex flex-wrap gap-4 mt-6 pt-6 border-t">
//                 <div className="flex items-center gap-2">
//                   <div className="w-4 h-4 rounded status-available"></div>
//                   <span className="text-sm">Available</span>
//                 </div>
//                 <div className="flex items-center gap-2">
//                   <div className="w-4 h-4 rounded status-booked"></div>
//                   <span className="text-sm">Booked</span>
//                 </div>
//                 <div className="flex items-center gap-2">
//                   <div className="w-4 h-4 rounded status-pending"></div>
//                   <span className="text-sm">Pending</span>
//                 </div>
//               </div>
//             </CardContent>
//           </Card>

//           <div className="flex justify-end mb-6 mt-6">
//             <Button
//               variant="ghost"
//               onClick={() => setWeekOffset((prev) => prev + 1)}
//               // disabled={nextBookings.length === 0}
//               className="ml-auto"
//             >
//               Next Week
//               <ArrowRight className="mr-2 h-4 w-4" />
//             </Button>
//           </div>

//           {/* Next Week Schedule (conditionally rendered) */}
//           {WeekOffset > 0 && (
//             <div className="mt-4">
//               <Card ref={calendarRef} className="glass shadow-elegant">
//                 <CardHeader>
//                   <CardTitle className="flex items-center gap-2">
//                     <Clock className="h-5 w-5" />
//                     Next Weekly Schedule
//                   </CardTitle>
//                 </CardHeader>

//                 <CardContent>
//                   <div className="overflow-x-auto">
//                     <div className="min-w-[800px]">
//                       {/* Header */}
//                       <div className="grid grid-cols-6 gap-2 mb-4">
//                         <div className="p-3 font-semibold text-center">
//                           Time
//                         </div>
//                         {days.map((day) => (
//                           <div
//                             key={`day-${day}`}
//                             className="p-3 font-semibold text-center"
//                           >
//                             {day}
//                           </div>
//                         ))}
//                       </div>
//                       {timeSlots.map((slot) => (
//                         <div
//                           key={`row-${slot.start}-${slot.end}`}
//                           className="grid grid-cols-6 gap-2 mb-2"
//                         >
//                           <div className="p-3 font-medium text-center bg-muted rounded-lg">
//                             {slot.label}
//                           </div>
//                           {days.map((day) => {
//                             const { status, course_code, faculty_name } =
//                               getNextWeekSlotStatus(day, slot);
//                             return (
//                               <div
//                                 key={`cell-${day}-${slot.start}-${slot.end}`}
//                                 className={`time-slot p-3 rounded-lg border-2 cursor-pointer transition-all duration-200 ${getStatusColor(
//                                   status
//                                 )} ${
//                                   status === "available"
//                                     ? "hover-scale hover:shadow-soft"
//                                     : status === "past"
//                                     ? "cursor-not-allowed opacity-50" // <--- softer look for past
//                                     : "cursor-not-allowed opacity-75"
//                                 }`}
//                                 onClick={() =>
//                                   status === "available" &&
//                                   handleSlotClick(day, slot.start, slot.end, 2)
//                                 }
//                               >
//                                 <div className="text-center">
//                                   <div className="text-sm font-medium capitalize">
//                                     {status}
//                                   </div>
//                                   {status !== "available" && course_code && (
//                                     <div className="text-xs mt-1 opacity-70">
//                                       {course_code}
//                                     </div>
//                                   )}
//                                 </div>
//                               </div>
//                             );
//                           })}
//                         </div>
//                       ))}
//                     </div>
//                   </div>

//                   {/* Legend */}
//                   <div className="flex flex-wrap gap-4 mt-6 pt-6 border-t">
//                     <div className="flex items-center gap-2">
//                       <div className="w-4 h-4 rounded status-available"></div>
//                       <span className="text-sm">Available</span>
//                     </div>
//                     <div className="flex items-center gap-2">
//                       <div className="w-4 h-4 rounded status-booked"></div>
//                       <span className="text-sm">Booked</span>
//                     </div>
//                     <div className="flex items-center gap-2">
//                       <div className="w-4 h-4 rounded status-pending"></div>
//                       <span className="text-sm">Pending</span>
//                     </div>
//                   </div>
//                 </CardContent>
//               </Card>
//               {/* <ScheduleGrid
//                 days={days}
//                 timeSlots={timeSlots}
//                 bookings={nextWeekBookings}
//                 routines={routines}
//                 onSlotClick={handleSlotClick}
//               /> */}
//             </div>
//           )}
//         </div>
//       </div>
//     </div>
//   );
// }

import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Navbar } from "@/components/Navbar";
import { useClassrooms } from "@/hooks/useClassrooms";
import { useBookings } from "@/hooks/useBookings";
import {
  ArrowLeft,
  ArrowRight,
  Calendar,
  Clock,
  MapPin,
  Users,
  ChevronLeft,
  ChevronRight,
  Info,
} from "lucide-react";
import { gsap } from "gsap";

const toMinutes = (t: string) => {
  const [h, m] = t.split(":").map(Number);
  return h * 60 + m;
};

const overlaps = (
  aStart: string,
  aEnd: string,
  bStart: string,
  bEnd: string
) => {
  return aStart === bStart;
};

const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"];

type SlotStatus = "available" | "booked" | "pending" | "past";

type Slot = { label: string; start: string; end: string };

const timeSlots: Slot[] = [
  { label: "08:00", start: "08:00", end: "09:15" },
  { label: "09:15", start: "09:15", end: "10:30" },
  { label: "10:30", start: "10:30", end: "11:45" },
  { label: "11:45", start: "11:45", end: "13:00" },
  { label: "14:30", start: "14:30", end: "15:45" },
  { label: "15:45", start: "15:45", end: "17:00" },
];

export default function ClassroomSchedule() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [weekOffset, setWeekOffset] = useState(0);
  const { classrooms } = useClassrooms();
  const { bookings, routines, nextBookings } = useBookings(id);
  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null);
  const [showNextWeek, setShowNextWeek] = useState(false);
  const calendarRef = useRef<HTMLDivElement>(null);

  const classroom = classrooms.find((c) => String(c.id) === String(id));

  useEffect(() => {
    if (calendarRef.current) {
      gsap.fromTo(
        ".time-slot",
        { scale: 0.9, opacity: 0 },
        {
          scale: 1,
          opacity: 1,
          duration: 0.5,
          stagger: 0.02,
          ease: "power2.out",
        }
      );
    }
  }, [weekOffset, showNextWeek]);

  const formatDateForInput = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const handleSlotClick = (
    day: string,
    start_time: string,
    end_time: string,
    week: number
  ) => {
    const slot: Slot = timeSlots.find((x) => x.start === start_time);
    const status =
      week === 1
        ? getWeeklySlotStatus(day, slot).status
        : getNextWeekSlotStatus(day, slot).status;

    if (status === "available") {
      const dayIndex = [
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday",
      ].indexOf(day);

      const today = new Date();
      const todayMidnight = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate()
      );

      // Calculate the Monday of the current week
      const monday = new Date(todayMidnight);
      monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7));

      // Calculate the target date based on week offset
      const targetDate = new Date(monday);
      targetDate.setDate(monday.getDate() + weekOffset * 7 + dayIndex);

      const formattedDate = formatDateForInput(targetDate);

      setSelectedSlot(slot);
      navigate(
        `/booking/${id}?day=${day}&start_time=${start_time}&end_time=${end_time}&slotDate=${formattedDate}`
      );
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "available":
        return "status-available";
      case "booked":
        return "status-booked";
      case "pending":
        return "status-pending";
      case "past":
        return "status-past";
      default:
        return "";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "available":
        return "✓";
      case "booked":
        return "✗";
      case "pending":
        return "⏳";
      case "past":
        return "⌛";
      default:
        return "";
    }
  };

  // Core Functions - Fixed versions
  function getWeeklySlotStatus(
    day: string,
    slot: Slot
  ): { status: SlotStatus; course_code?: string; faculty_name?: string } {
    /// Setting past as grey
    const today = new Date();
    const todayMidnight = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate()
    );

    // MONDAY FIRST DAY OF THE WEEK
    const dayIndex = [
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday",
      "Sunday",
    ].indexOf(day);
    if (dayIndex === -1) throw new Error(`Invalid day: ${day}`);

    // Get Monday of this week // Monday is start of the week
    const monday = new Date(todayMidnight);
    monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7)); // adjust to Monday

    // Actual date for the given "day"
    const slotDate = new Date(monday);
    slotDate.setDate(monday.getDate() + dayIndex);

    if (slotDate < todayMidnight) {
      return { status: "past" as SlotStatus }; // Handle "past" style in UI
    }

    // 1) Weekly routine wins (treat as booked)
    const r = routines.find(
      (x) =>
        x.day === day &&
        overlaps(slot.start, slot.end, x.start_time, x.end_time)
    );
    if (r) {
      return {
        status: "booked",
        course_code: r.course_code,
        faculty_name: r.faculty_name,
      };
    }

    // 2) Bookings for this week (backend already filtered to the week)
    const relevant = bookings.filter(
      (b) =>
        b.day === day &&
        overlaps(slot.start, slot.end, b.start_time, b.end_time)
    );

    if (relevant.length > 0) {
      // prioritize statuses: pending < approved < rejected (rejected doesn't block)
      const hasApproved = relevant.find((b) => b.status === "booked");
      const hasPending = relevant.find((b) => b.status === "pending");
      if (hasApproved)
        return {
          status: "booked",
          course_code: hasApproved.course_code,
          faculty_name: hasApproved.faculty_name,
        };
      if (hasPending)
        return {
          status: "pending",
          course_code: hasPending.course_code,
          faculty_name: hasPending.faculty_name,
        };
    }

    return { status: "available" };
  }

  // Fixed getNextWeekSlotStatus function
function getNextWeekSlotStatus(
  day: string,
  slot: Slot
): { status: SlotStatus; course_code?: string; faculty_name?: string } {
  // 1) Weekly routine wins (treat as booked) - routines repeat every week
  const r = routines.find(
    (x) =>
      x.day === day &&
      overlaps(slot.start, slot.end, x.start_time, x.end_time)
  );
  if (r) {
    return {
      status: "booked",
      course_code: r.course_code,
      faculty_name: r.faculty_name,
    };
  }

  // 2) Bookings for next week - make sure we're checking the right day
  const relevant = nextBookings.filter((b) =>
    b.day === day && // This was missing in your original code
    overlaps(slot.start, slot.end, b.start_time, b.end_time)
  );

  if (relevant.length > 0) {
    // prioritize statuses: pending < approved < rejected (rejected doesn't block)
    const hasApproved = relevant.find((b) => b.status === "booked");
    const hasPending = relevant.find((b) => b.status === "pending");
    
    if (hasApproved) return {
      status: "booked",
      course_code: hasApproved.course_code,
      faculty_name: hasApproved.faculty_name,
    };
    if (hasPending) return { 
      status: "pending",
      course_code: hasPending.course_code,
      faculty_name: hasPending.faculty_name, 
    };
  }

  return { status: "available" };
}

  if (!classroom) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 text-center">
          <p className="text-xl text-muted-foreground">Classroom not found</p>
        </div>
      </div>
    );
  }

  // Calculate dates for the current week
  const today = new Date();
  const monday = new Date(today);
  monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
  const weekDates = days.map((_, i) => {
    const date = new Date(monday);
    date.setDate(monday.getDate() + i + weekOffset * 7);
    return date;
  });

  return (
    <div className="min-h-screen gradient-surface">
      <Navbar />

      <div className="pt-28 pb-16 px-4">
        <div className="max-w-6xl mx-auto">
          <Button
            variant="ghost"
            onClick={() => navigate("/")}
            className="mb-6 hover-scale"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Classrooms
          </Button>

          {/* Classroom Info */}
          <Card className="glass shadow-elegant mb-8">
            <CardHeader>
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <CardTitle className="flex items-center gap-4">
                  <span className="text-3xl font-bold text-primary">
                    Room {classroom.room_number}
                  </span>
                  <Badge variant="secondary" className="glass">
                    Available
                  </Badge>
                </CardTitle>

                <div className="flex flex-wrap gap-6 text-muted-foreground">
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4" />
                    {classroom.building}
                  </div>
                  <div className="flex items-center gap-2">
                    <Users className="h-4 w-4" />
                    Capacity: {classroom.capacity}
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Weekly Schedule
                  </div>
                </div>
              </div>
            </CardHeader>
          </Card>

          {/* Week Navigation */}
          <div className="flex justify-between items-center mb-6">
            <Button
              variant="outline"
              onClick={() => setWeekOffset((prev) => Math.max(0, prev - 1))}
              disabled={weekOffset === 0}
              className="flex items-center gap-2"
            >
              <ChevronLeft className="h-4 w-4" />
              Previous Week
            </Button>

            <div className="text-center">
              <h2 className="text-xl font-bold text-primary">
                {weekOffset === 0 ? "Current Week" : `Week ${weekOffset + 1}`}
              </h2>
              <p className="text-sm text-muted-foreground mt-1">
                {weekDates[0].toLocaleDateString()} -{" "}
                {weekDates[4].toLocaleDateString()}
              </p>
            </div>

            <Button
              variant="outline"
              onClick={() => setWeekOffset((prev) => prev + 1)}
              disabled={weekOffset >= 1} // Disable when weekOffset is 1 or more (week 2+)
              className="flex items-center gap-2"
            >
              Next Week
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>

          {/* Schedule Grid */}
          <Card ref={calendarRef} className="glass shadow-elegant">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Clock className="h-5 w-5" />
                Weekly Schedule
              </CardTitle>
            </CardHeader>

            <CardContent>
              <div className="overflow-x-auto">
                <div className="min-w-[800px]">
                  {/* Header */}
                  <div className="grid grid-cols-6 gap-2 mb-4">
                    <div className="p-3 font-semibold text-center">Time</div>
                    {days.map((day, i) => (
                      <div
                        key={`day-${day}`}
                        className="p-3 font-semibold text-center"
                      >
                        <div>{day}</div>
                        <div className="text-xs font-normal text-muted-foreground mt-1">
                          {weekDates[i].toLocaleDateString(undefined, {
                            month: "short",
                            day: "numeric",
                          })}
                        </div>
                      </div>
                    ))}
                  </div>

                  {timeSlots.map((slot) => (
                    <div
                      key={`row-${slot.start}-${slot.end}`}
                      className="grid grid-cols-6 gap-2 mb-2"
                    >
                      <div className="p-3 font-medium text-center bg-muted rounded-lg flex flex-col justify-center">
                        <span>{slot.label}</span>
                        <span className="text-xs text-muted-foreground">
                          to {slot.end}
                        </span>
                      </div>
                      {days.map((day) => {
                        const { status, course_code, faculty_name } =
                          weekOffset === 0
                            ? getWeeklySlotStatus(day, slot)
                            : getNextWeekSlotStatus(day, slot);
                        return (
                          <div
                            key={`cell-${day}-${slot.start}-${slot.end}`}
                            className={`time-slot p-3 rounded-lg border-2 cursor-pointer transition-all duration-200 ${getStatusColor(
                              status
                            )} ${
                              status === "available"
                                ? "hover-scale hover:shadow-soft"
                                : status === "past"
                                ? "cursor-not-allowed opacity-50"
                                : "cursor-not-allowed opacity-75"
                            }`}
                            onClick={() =>
                              status === "available" &&
                              handleSlotClick(
                                day,
                                slot.start,
                                slot.end,
                                weekOffset === 0 ? 1 : 2
                              )
                            }
                          >
                            <div className="text-center">
                              <div className="text-sm font-medium flex items-center justify-center gap-1">
                                <span className="text-xs">
                                  {getStatusIcon(status)}
                                </span>
                                <span className="capitalize">{status}</span>
                              </div>
                              {status !== "available" && course_code && (
                                <div className="text-xs mt-1 opacity-70">
                                  {course_code}
                                  {faculty_name && (
                                    <div className="truncate">
                                      {faculty_name}
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>

              {/* Legend */}
              <div className="flex flex-wrap gap-4 mt-8 pt-6 border-t border-muted justify-center">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded status-available"></div>
                  <span className="text-sm">Available</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded status-booked"></div>
                  <span className="text-sm">Booked</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded status-pending"></div>
                  <span className="text-sm">Pending</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded status-past"></div>
                  <span className="text-sm">Past</span>
                </div>
              </div>

              <div className="text-center mt-4 text-sm text-muted-foreground">
                <p>Click on available time slots to book this classroom</p>
              </div>
            </CardContent>
          </Card>

          {/* Next Week Toggle */}
          {/* <div className="flex justify-center mt-8">
            <Button
              variant="outline"
              onClick={() => setShowNextWeek(!showNextWeek)}
              className="flex items-center gap-2"
            >
              {showNextWeek ? 'Hide Next Week' : 'Show Next Week'}
              {showNextWeek ? <ChevronRight className="h-4 w-4" /> : <Info className="h-4 w-4" />}
            </Button>
          </div> */}

          {/* Next Week Schedule */}
          {showNextWeek && (
            <div className="mt-6">
              <Card className="glass shadow-elegant">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Clock className="h-5 w-5" />
                    Next Week Schedule
                  </CardTitle>
                </CardHeader>

                <CardContent>
                  <div className="overflow-x-auto">
                    <div className="min-w-[800px]">
                      {/* Header */}
                      <div className="grid grid-cols-6 gap-2 mb-4">
                        <div className="p-3 font-semibold text-center">
                          Time
                        </div>
                        {days.map((day) => (
                          <div
                            key={`day-${day}`}
                            className="p-3 font-semibold text-center"
                          >
                            {day}
                          </div>
                        ))}
                      </div>
                      {timeSlots.map((slot) => (
                        <div
                          key={`row-${slot.start}-${slot.end}`}
                          className="grid grid-cols-6 gap-2 mb-2"
                        >
                          <div className="p-3 font-medium text-center bg-muted rounded-lg">
                            {slot.label}
                          </div>
                          {days.map((day) => {
                            const { status, course_code, faculty_name } =
                              getNextWeekSlotStatus(day, slot);
                            return (
                              <div
                                key={`cell-${day}-${slot.start}-${slot.end}`}
                                className={`time-slot p-3 rounded-lg border-2 cursor-pointer transition-all duration-200 ${getStatusColor(
                                  status
                                )} ${
                                  status === "available"
                                    ? "hover-scale hover:shadow-soft"
                                    : status === "past"
                                    ? "cursor-not-allowed opacity-50"
                                    : "cursor-not-allowed opacity-75"
                                }`}
                                onClick={() =>
                                  status === "available" &&
                                  handleSlotClick(day, slot.start, slot.end, 2)
                                }
                              >
                                <div className="text-center">
                                  <div className="text-sm font-medium flex items-center justify-center gap-1">
                                    <span className="text-xs">
                                      {getStatusIcon(status)}
                                    </span>
                                    <span className="capitalize">{status}</span>
                                  </div>
                                  {status !== "available" && course_code && (
                                    <div className="text-xs mt-1 opacity-70">
                                      {course_code}
                                      {faculty_name && (
                                        <div className="truncate">
                                          {faculty_name}
                                        </div>
                                      )}
                                    </div>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      ))}
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
