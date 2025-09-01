import { useEffect, useRef } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Classroom } from "@/hooks/useClassrooms";
import { MapPin, Users, Monitor } from "lucide-react";
import { gsap } from "gsap";

interface ClassroomCardProps {
  classroom: Classroom;
  onSelect: (classroom: Classroom) => void;
}

export function ClassroomCard({ classroom, onSelect }: ClassroomCardProps) {
  const cardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (cardRef.current) {
      gsap.fromTo(cardRef.current,
        { y: 20, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.5, ease: "power2.out" }
      );
    }
  }, []);

  const handleHover = () => {
    if (cardRef.current) {
      gsap.to(cardRef.current, {
        y: -8,
        duration: 0.3,
        ease: "power2.out"
      });
    }
  };

  const handleLeave = () => {
    if (cardRef.current) {
      gsap.to(cardRef.current, {
        y: 0,
        duration: 0.3,
        ease: "power2.out"
      });
    }
  };

  return (
    <Card 
      ref={cardRef}
      className="cursor-pointer hover-lift shadow-soft"
      onMouseEnter={handleHover}
      onMouseLeave={handleLeave}
      onClick={() => onSelect(classroom)}
    >
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl font-bold text-primary">
            Room {classroom.room_number}
          </CardTitle>
          <Badge variant="secondary" className="glass">
            Available
          </Badge>
        </div>
        <CardDescription className="flex items-center gap-2">
          <MapPin className="h-4 w-4" />
          {classroom.building}
        </CardDescription>
      </CardHeader>
      
      <CardContent className="space-y-4">
        <div className="flex items-center gap-2 text-muted-foreground">
          <Users className="h-4 w-4" />
          <span>Capacity: {classroom.capacity} students</span>
        </div>
        
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-muted-foreground">
            <Monitor className="h-4 w-4" />
            <span className="text-sm font-medium">Equipment:</span>
          </div>
          <div className="flex flex-wrap gap-1">
            {classroom.equipment.map((item, index) => (
              <Badge key={index} variant="outline" className="text-xs">
                {item}
              </Badge>
            ))}
          </div>
        </div>
        
        <Button className="w-full mt-4 hover-scale">
          View Schedule
        </Button>
      </CardContent>
    </Card>
  );
}