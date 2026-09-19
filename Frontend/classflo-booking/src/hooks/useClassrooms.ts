import { useState, useEffect } from "react";
import { apiUrl } from "@/lib/api";

export interface Classroom {
  id: string;
  room_number: string;
  building: string;
  capacity: number;
  equipment: string[];
  is_available: boolean;
}

export function useClassrooms() {
  const [classrooms, setClassrooms] = useState<Classroom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchClassrooms() {
      try {
        const response = await fetch(apiUrl('/api/classrooms'));
        
        if (response.ok) {
          const data = await response.json();
          setClassrooms(data);
        } else {
          throw new Error('Failed to fetch classrooms');
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
      } finally {
        setLoading(false);
      }
    }

    fetchClassrooms();
  }, []);

  return { classrooms, loading, error };
}
