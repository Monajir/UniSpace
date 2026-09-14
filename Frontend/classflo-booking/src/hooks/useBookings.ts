// import { useState, useEffect } from "react";

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

// export interface Routine {
//   id: string;
//   classroom_id: string;
//   faculty_name: string;
//   course_code: string;
//   day: string;
//   start_time: string;
//   end_time: string;
// }

// export interface RoomScheduleResponse {
//   classroomId: string;
//   regular: Routine[];
//   extras: Booking[];
// }

// export interface ApiResponse<T> {
//   data?: T;
//   message?: string;
//   error?: string;
// }

// export function useBookings(classroomId: string) {
//   const [bookings, setBookings] = useState<Booking[]>([]);
//   const [nextBookings, setNextBookings] = useState<Booking[]>([]);
//   const [routines, setRoutines] = useState<Routine[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState<string | null>(null);

//   useEffect(() => {
//     async function fetchBookings() {
//       try {
//         const url = `http://localhost:8082/api/bookings/classSchedule/${classroomId}`;
//         const nextUrl = `http://localhost:8082/api/bookings/classSchedule/next/${classroomId}`;
        
//         const response = await fetch(url);
//         const nextResponse = await fetch(nextUrl);

//         if (response.ok) {
//           const data: RoomScheduleResponse = await response.json();
//           setBookings(data.extras || []);
//           setRoutines(data.regular || []);
//         } else {
//           throw new Error('Failed to fetch this week bookings');
//         }

//         if (nextResponse.ok) {
//           const data: RoomScheduleResponse = await nextResponse.json();
//           setNextBookings(data.extras || []);
//           // setRoutines(data.regular || []); // getting null from nextweeks response for regular routine
//         } else {
//           throw new Error('Failed to fetch this week bookings');
//         }
        
//       } catch (err) {
//         setError(err instanceof Error ? err.message : 'An error occurred');
//       } finally {
//         setLoading(false);
//       }
//     }

//     fetchBookings();
//   }, [classroomId]);

//   const createBooking = async (
//     booking: Omit<Booking, 'id' | 'created_at' | 'status' | 'faculty_name' | 'course_code'>
//   ): Promise<ApiResponse<Booking>> => {
//     try {
//       const token = localStorage.getItem('auth_token');
//       const response = await fetch('http://localhost:8082/api/bookings/room/book', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//           'Authorization': `Bearer ${token}`
//         },
//         body: JSON.stringify(booking),
//         credentials: 'include'
//       });

//       if (!response.ok) {
//         const errorData = await response.json().catch(() => ({}));
//         throw new Error(errorData.message || 'Booking failed');
//       }

//       const responseData = await response.json();
//       const newBooking = responseData.data;

//       if(newBooking) {
//         setBookings(prev => [...prev, newBooking]);
//       }

//       return {
//         data: newBooking,
//         message: responseData.message || 'Booking created successfully'
//       };

//       // if (response.ok) {
//       //   const data = await response.json();
//       //   setBookings(prev => [...prev, data]);
//       //   return { data, error: null };
//       // } else {
//       //   const errorData = await response.json();
//       //   throw new Error(errorData.message || 'Failed to create booking');
//       // }
//     } catch (err) {
//       // return { data: null, error: err instanceof Error ? err.message : 'An error occurred' };
//       console.error('Booking error:', err);
//       return { 
//         error: err instanceof Error ? err.message : 'Failed to create booking',
//         data: null
//       };
//     }
//   };

//   return { bookings, routines, nextBookings, loading, error, createBooking };
// }




import { useState, useEffect, useCallback } from "react";
import { apiErrorMessage, apiUrl } from "@/lib/api";
import {
  BOOKING_SCHEDULE_UPDATED_EVENT,
  isBookingScheduleStorageEvent,
  notifyBookingScheduleChanged,
} from "@/lib/bookingScheduleEvents";

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

export interface Routine {
  id: string;
  classroom_id: string;
  faculty_name: string;
  course_code: string;
  day: string;
  start_time: string;
  end_time: string;
}

export interface RoomScheduleResponse {
  classroomId: string;
  regular: Routine[];
  extras: Booking[];
}

export interface ApiResponse<T> {
  data?: T;
  message?: string;
  error?: string;
}

export function useBookings(classroomId: string) {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [nextBookings, setNextBookings] = useState<Booking[]>([]);
  const [routines, setRoutines] = useState<Routine[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchBookings = useCallback(async () => {
      try {
        setLoading(true);
        setError(null);
        
        const url = apiUrl(`/api/bookings/classSchedule/${classroomId}`);
        const nextUrl = apiUrl(`/api/bookings/classSchedule/next/${classroomId}`);
        
        // Fetch current week data
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`Failed to fetch this week bookings: ${response.status}`);
        }
        const data: RoomScheduleResponse = await response.json();
        setBookings(data.extras || []);
        setRoutines(data.regular || []);

        // Fetch next week data
        const nextResponse = await fetch(nextUrl);
        if (!nextResponse.ok) {
          console.warn('Failed to fetch next week bookings, using empty array');
          setNextBookings([]);
        } else {
          const nextData: RoomScheduleResponse = await nextResponse.json();
          setNextBookings(nextData.extras || []);
          // The current-week response already supplies the recurring routine data.
        }
        
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
        console.error('Error fetching bookings:', err);
      } finally {
        setLoading(false);
      }
  }, [classroomId]);

  useEffect(() => {
    if (classroomId) {
      void fetchBookings();
    }

    const refresh = () => void fetchBookings();
    const refreshFromStorage = (event: StorageEvent) => {
      if (isBookingScheduleStorageEvent(event)) refresh();
    };

    window.addEventListener(BOOKING_SCHEDULE_UPDATED_EVENT, refresh);
    window.addEventListener("storage", refreshFromStorage);
    window.addEventListener("focus", refresh);

    return () => {
      window.removeEventListener(BOOKING_SCHEDULE_UPDATED_EVENT, refresh);
      window.removeEventListener("storage", refreshFromStorage);
      window.removeEventListener("focus", refresh);
    };
  }, [classroomId, fetchBookings]);

  const createBooking = async (
    booking: Omit<Booking, 'id' | 'created_at' | 'status' | 'faculty_name' | 'user_id'>
  ): Promise<ApiResponse<Booking>> => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(apiUrl('/api/bookings/room/book'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(booking),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(await apiErrorMessage(response, `Booking failed with status ${response.status}`));
      }

      const newBooking: Booking = await response.json();

      if (newBooking) {
        // Add the new booking to the appropriate list based on its date
        const bookingDate = new Date(newBooking.booking_date);
        const today = new Date();
        const nextWeek = new Date(today);
        nextWeek.setDate(today.getDate() + 7);
        
        if (bookingDate >= nextWeek) {
          // Booking is for next week or beyond
          setNextBookings(prev => [...prev, newBooking]);
        } else {
          // Booking is for this week
          setBookings(prev => [...prev, newBooking]);
        }
        notifyBookingScheduleChanged();
      }

      return {
        data: newBooking,
        message: 'Booking created successfully'
      };

    } catch (err) {
      console.error('Booking error:', err);
      return { 
        error: err instanceof Error ? err.message : 'Failed to create booking'
      };
    }
  };

  return { bookings, routines, nextBookings, loading, error, createBooking };
}
