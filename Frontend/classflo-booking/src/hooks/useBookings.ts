import { useState, useEffect } from "react";

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

  useEffect(() => {
    async function fetchBookings() {
      try {
        const url = `http://localhost:8082/api/bookings/classSchedule/${classroomId}`;
        const nextUrl = `http://localhost:8082/api/bookings/classSchedule/next/${classroomId}`;
        
        const response = await fetch(url);
        const nextResponse = await fetch(nextUrl);

        if (response.ok) {
          const data: RoomScheduleResponse = await response.json();
          setBookings(data.extras || []);
          setRoutines(data.regular || []);
        } else {
          throw new Error('Failed to fetch this week bookings');
        }

        if (nextResponse.ok) {
          const data: RoomScheduleResponse = await nextResponse.json();
          setNextBookings(data.extras || []);
          // setRoutines(data.regular || []); // getting null from nextweeks response for regular routine
        } else {
          throw new Error('Failed to fetch this week bookings');
        }
        
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
      } finally {
        setLoading(false);
      }
    }

    fetchBookings();
  }, [classroomId]);

  const createBooking = async (
    booking: Omit<Booking, 'id' | 'created_at' | 'status' | 'faculty_name' | 'course_code'>
  ): Promise<ApiResponse<Booking>> => {
    try {
      const token = localStorage.getItem('auth_token');
      const response = await fetch('http://localhost:8082/api/bookings/room/book', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(booking),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Booking failed');
      }

      const responseData = await response.json();
      const newBooking = responseData.data;

      if(newBooking) {
        setBookings(prev => [...prev, newBooking]);
      }

      return {
        data: newBooking,
        message: responseData.message || 'Booking created successfully'
      };

      // if (response.ok) {
      //   const data = await response.json();
      //   setBookings(prev => [...prev, data]);
      //   return { data, error: null };
      // } else {
      //   const errorData = await response.json();
      //   throw new Error(errorData.message || 'Failed to create booking');
      // }
    } catch (err) {
      // return { data: null, error: err instanceof Error ? err.message : 'An error occurred' };
      console.error('Booking error:', err);
      return { 
        error: err instanceof Error ? err.message : 'Failed to create booking',
        data: null
      };
    }
  };

  return { bookings, routines, nextBookings, loading, error, createBooking };
}