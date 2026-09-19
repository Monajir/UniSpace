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
import { findBookingSlot } from "@/lib/bookingSlots";

export default function BookingForm() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { classrooms } = useClassrooms();
  const { createBooking } = useBookings(id);
  const { user, profile, loading: authLoading } = useAuth();
  const { toast } = useToast();
  const canBook = user?.roles?.some((role) => role.toUpperCase() === "CR") ?? false;
  
  const [isLoading, setIsLoading] = useState(false);
  const formRef = useRef<HTMLDivElement>(null);
  
  const day = searchParams.get("day");
  const start_time = searchParams.get("start_time");
  const end_time = searchParams.get("end_time");
  const date = searchParams.get("slotDate");
  const selectedSlot = findBookingSlot(start_time, end_time);
  const classroom = classrooms.find(c => c.id === id);

  useEffect(() => {
    if (authLoading) {
      return;
    }
    if (!user) {
      navigate("/auth");
      return;
    }
  }, [user, authLoading, navigate]);

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
    if (!user || !canBook || !id) return;

    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const facultyEmail = formData.get("facultyEmail") as string;
    const courseCode = formData.get("courseCode") as string;
    const reason = formData.get("reason") as string;

    if (!selectedSlot || !date || !day) {
      toast({
        title: "Invalid slot",
        description: "Please select an available slot from the classroom schedule.",
        variant: "destructive",
      });
      setIsLoading(false);
      return;
    }

    const bookingData = {
      classroom_id: id,
      faculty_email: facultyEmail,
      course_code: courseCode.trim(),
      day,
      reason,
      booking_date: date,
      start_time: selectedSlot.start,
      end_time: selectedSlot.end,
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

  if (authLoading) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 text-center text-muted-foreground">Checking booking access...</div>
      </div>
    );
  }

  if (user && !canBook) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 px-4">
          <Card className="glass shadow-elegant max-w-xl mx-auto text-center">
            <CardHeader>
              <CardTitle>Booking access restricted</CardTitle>
            </CardHeader>
            <CardContent className="space-y-5">
              <p className="text-muted-foreground">
                Only class representatives can submit classroom booking requests.
              </p>
              <Button onClick={() => navigate(`/classroom/${id}`)}>
                Return to schedule
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 text-center text-muted-foreground">Redirecting to sign in...</div>
      </div>
    );
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

  if (!selectedSlot || !date || !day) {
    return (
      <div className="min-h-screen gradient-surface">
        <Navbar />
        <div className="pt-32 px-4">
          <Card className="glass shadow-elegant max-w-xl mx-auto text-center">
            <CardHeader>
              <CardTitle>Select a valid booking slot</CardTitle>
            </CardHeader>
            <CardContent className="space-y-5">
              <p className="text-muted-foreground">
                Booking requests must begin from an available slot in the classroom schedule.
              </p>
              <Button onClick={() => navigate(`/classroom/${id}`)}>Return to schedule</Button>
            </CardContent>
          </Card>
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
                <div className="rounded-lg border border-primary/20 bg-primary/5 p-4">
                  <p className="mb-3 text-sm font-medium text-muted-foreground">Selected schedule slot</p>
                  <div className="grid gap-3 text-sm sm:grid-cols-3">
                    <div><span className="block text-xs text-muted-foreground">Date</span>{date}</div>
                    <div><span className="block text-xs text-muted-foreground">Day</span>{day}</div>
                    <div><span className="block text-xs text-muted-foreground">Time</span>{selectedSlot.start}–{selectedSlot.end}</div>
                  </div>
                  <p className="mt-3 text-xs text-muted-foreground">
                    The date and time are fixed by the slot selected on the schedule.
                  </p>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="courseCode">Course</Label>
                    <Input
                      id="courseCode"
                      name="courseCode"
                      type="text"
                      required
                      maxLength={100}
                      placeholder="e.g., CSE 451"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="facultyEmail">Faculty Email</Label>
                    <Input
                      id="facultyEmail"
                      name="facultyEmail"
                      type="email"
                      placeholder="faculty@iut-dhaka.edu"
                      required
                    />
                    <p className="text-xs text-muted-foreground">
                      Enter the email of a registered UniSpace faculty account.
                    </p>
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
                    <li>• Booking time must match one published 1 hour 15 minute slot</li>
                    <li>• A course code is required for every request</li>
                    <li>• The faculty email must belong to a registered faculty account</li>
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
