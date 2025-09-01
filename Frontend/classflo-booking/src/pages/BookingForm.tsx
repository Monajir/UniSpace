import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Navbar } from "@/components/Navbar";
import { useClassrooms } from "@/hooks/useClassrooms";
import { useBookings } from "@/hooks/useBookings";
import { useAuth } from "@/hooks/useAuth";
import { useToast } from "@/hooks/use-toast";
import { ArrowLeft, Calendar, Clock, User } from "lucide-react";
import { gsap } from "gsap";

export default function BookingForm() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { classrooms } = useClassrooms();
  const { createBooking } = useBookings(id);
  const { user, profile } = useAuth();
  const { toast } = useToast();
  
  const [isLoading, setIsLoading] = useState(false);
  const formRef = useRef<HTMLDivElement>(null);
  
  const day = searchParams.get("day");
  const start_time = searchParams.get("start_time");
  const end_time = searchParams.get("end_time");
  const date = searchParams.get("slotDate");
  const classroom = classrooms.find(c => c.id === id);

  useEffect(() => {
    if (!user) {
      navigate("/auth");
      return;
    }
  }, [user, navigate]);

  useEffect(() => {
    if (formRef.current) {
      gsap.fromTo(formRef.current,
        { y: 50, opacity: 0, scale: 0.95 },
        { y: 0, opacity: 1, scale: 1, duration: 0.8, ease: "power3.out" }
      );
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!user || !id) return;

    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const facultyEmail = formData.get("facultyEmail") as string;
    const reason = formData.get("reason") as string;
    const customDate = formData.get("customDate") as string;
    const startTime = formData.get("startTime") as string;
    const endTime = formData.get("endTime") as string;

    const bookingData = {
      classroom_id: id,
      user_id: user.id,
      faculty_email: facultyEmail,
      day, 
      reason,
      booking_date: customDate || new Date().toISOString().split('T')[0],
      start_time: startTime,
      end_time: endTime,
    };

    const { data, error } = await createBooking(bookingData);

    if (error) {
      toast({
        title: "Error",
        description: error,
        variant: "destructive",
      });
    } else {
      toast({
        title: "Success",
        description: "Booking request submitted successfully!",
      });
      navigate(`/classroom/${id}`);
    }

    setIsLoading(false);
  };

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

  return (
    <div className="min-h-screen gradient-surface">
      <Navbar />
      
      <div className="pt-32 pb-20 px-4">
        <div className="container mx-auto max-w-2xl">
          <Button
            variant="ghost"
            onClick={() => navigate(`/classroom/${id}`)}
            className="mb-6 hover-scale"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Schedule
          </Button>

          <Card ref={formRef} className="glass shadow-elegant">
            <CardHeader>
              <CardTitle className="text-2xl font-bold text-center">
                Book Room {classroom.room_number}
              </CardTitle>
              
              <div className="flex flex-wrap gap-4 justify-center text-muted-foreground mt-4">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4" />
                  {day || "Select Date"}
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4" />
                  {start_time || "Select Time"}
                </div>
                <div className="flex items-center gap-2">
                  <User className="h-4 w-4" />
                  {profile?.full_name || "User"}
                </div>
              </div>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="customDate">Date</Label>
                    <Input
                      id="customDate"
                      name="customDate"
                      type="date"
                      required
                      min={new Date().toISOString().split('T')[0]}
                      defaultValue={date}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="facultyEmail">Faculty Email</Label>
                    <Input
                      id="facultyEmail"
                      name="facultyEmail"
                      type="text"
                      placeholder="Enter Faculty Email"
                      required
                    />
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="startTime">Start Time</Label>
                    <Input
                      id="startTime"
                      name="startTime"
                      type="time"
                      defaultValue={start_time}
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="endTime">End Time</Label>
                    <Input
                      id="endTime"
                      name="endTime"
                      type="time"
                      defaultValue={end_time}
                      required
                    />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="reason">Reason for Booking</Label>
                  <Textarea
                    id="reason"
                    name="reason"
                    placeholder="Please describe the purpose of your booking..."
                    required
                    className="min-h-[100px]"
                  />
                </div>
                
                <div className="bg-muted/50 p-4 rounded-lg">
                  <h4 className="font-semibold mb-2">Booking Guidelines:</h4>
                  <ul className="text-sm text-muted-foreground space-y-1">
                    <li>• Maximum booking duration: 1 hour 15 minutes</li>
                    <li>• Bookings require faculty approval</li>
                    <li>• Cancel at least 2 hours in advance</li>
                    <li>• Ensure room is left clean and organized</li>
                  </ul>
                </div>
                
                <div className="flex gap-4">
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1 hover-scale"
                    onClick={() => navigate(`/classroom/${id}`)}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    className="flex-1 hover-scale"
                    disabled={isLoading}
                  >
                    {isLoading ? "Submitting..." : "Submit Request"}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}