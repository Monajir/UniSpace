import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Navbar } from "@/components/Navbar";
import { SearchBar } from "@/components/SearchBar";
import { ClassroomCard } from "@/components/ClassroomCard";
import { useClassrooms, Classroom } from "@/hooks/useClassrooms";
import { gsap } from "gsap";
import { Button } from "@/components/ui/button";
import { ArrowRight, Calendar, Shield, Users } from "lucide-react";

const Index = () => {
  const { classrooms, loading } = useClassrooms();
  const classroomsRef = useRef(null);
  const [filteredClassrooms, setFilteredClassrooms] = useState<Classroom[]>([]);
  const navigate = useNavigate();
  const heroRef = useRef<HTMLDivElement>(null);
  const featuresRef = useRef<HTMLDivElement>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);

  useEffect(() => {
    setFilteredClassrooms(classrooms);
  }, [classrooms]);

  useEffect(() => {
    // Reset iframe scroll position when component mounts
    if (iframeRef.current && iframeRef.current.contentWindow) {
      iframeRef.current.contentWindow.scrollTo(0, 0);
    }

    // Hero section animation
    if (heroRef.current) {
      gsap
        .timeline()
        .fromTo(
          ".hero-title",
          { y: 50, opacity: 0 },
          { y: 0, opacity: 1, duration: 1, ease: "power3.out" }
        )
        .fromTo(
          ".hero-subtitle",
          { y: 30, opacity: 0 },
          { y: 0, opacity: 1, duration: 0.8, ease: "power3.out" },
          "-=0.5"
        )
        .fromTo(
          ".hero-cta",
          { y: 20, opacity: 0 },
          { y: 0, opacity: 1, duration: 0.6, ease: "power3.out" },
          "-=0.3"
        );
    }

    // Features animation on scroll
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            gsap.fromTo(
              ".feature-card",
              { y: 50, opacity: 0 },
              {
                y: 0,
                opacity: 1,
                duration: 0.6,
                stagger: 0.2,
                ease: "power2.out",
              }
            );
          }
        });
      },
      { threshold: 0.1 }
    );

    if (featuresRef.current) {
      observer.observe(featuresRef.current);
    }

    return () => observer.disconnect();
  }, []);

  // useEffect to handle scroll reset when navigating away
  useEffect(() => {
    return () => {
      // Reset main window scroll position when component unmounts
      window.scrollTo(0, 0);
    };
  }, []);

  const handleSearch = (query: string) => {
    if (!query.trim()) {
      setFilteredClassrooms(classrooms);
      return;
    }

    const filtered = classrooms.filter(
      (classroom) =>
        classroom.room_number.toLowerCase().includes(query.toLowerCase()) ||
        classroom.building.toLowerCase().includes(query.toLowerCase())
    );
    setFilteredClassrooms(filtered);
  };

  const handleClassroomSelect = (classroom: Classroom) => {
    navigate(`/classroom/${String(classroom.id)}`);
  };

  return (
    <div className="min-h-screen gradient-surface relative">
      {/* Spline Background */}
      <div className="fixed inset-0 z-0">
        <iframe 
          ref={iframeRef}
          src='https://my.spline.design/chips-z8TfXuBK7Yj7R4IF0uXOmlzZ/'
          width='100%' 
          height='100%'
          className="opacity-50"
          style={{ 
            filter: "brightness(0.8) contrast(1.2)",
            pointerEvents: "none" 
          }}
        />
        <div className="absolute inset-0 bg-gradient-to-b from-background/80 via-background/50 to-background"></div>
      </div>

      {/* Content */}
      <div className="relative z-10">
        <Navbar />

        {/* Hero Section */}
        <section ref={heroRef} className="pt-32 pb-20 px-4">
          <div className="container mx-auto text-center">
            <h1 className="hero-title text-6xl md:text-7xl font-bold mb-6">
              <span className="gradient-primary bg-clip-text text-transparent">
                UniSpace
              </span>
            </h1>
            <p className="hero-subtitle text-xl md:text-2xl text-muted-foreground mb-12 max-w-3xl mx-auto">
              Smart classroom booking system for modern universities. Reserve
              spaces, manage schedules, and optimize learning environments.
            </p>

            <div className="hero-cta space-y-8">
              <SearchBar onSearch={handleSearch} />

              <div className="flex flex-wrap gap-4 justify-center">
                <Button
                  variant="outline"
                  size="lg"
                  className="hover-scale shadow-elegant bg-background/90 backdrop-blur-sm"
                  onClick={() => {
                    classroomsRef.current?.scrollIntoView({ behavior: "smooth" });
                  }}
                >
                  Browse Classrooms
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Button>
                <Button
                  variant="outline"
                  size="lg"
                  className="hover-scale bg-background/70 backdrop-blur-sm"
                  onClick={() => {
                    featuresRef.current?.scrollIntoView({ behavior: "smooth" });
                  }}
                >
                  Learn More
                </Button>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section ref={featuresRef} className="py-20 px-4">
          <div className="container mx-auto">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold mb-4">Why Choose UniSpace?</h2>
              <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
                Experience seamless classroom management with our advanced booking
                system
              </p>
            </div>

            <div className="grid md:grid-cols-3 gap-8">
              <div className="feature-card bg-background/80 backdrop-blur-sm p-8 rounded-2xl hover-lift border border-border/50">
                <Calendar className="h-12 w-12 text-primary mb-6" />
                <h3 className="text-xl font-bold mb-4">Smart Scheduling</h3>
                <p className="text-muted-foreground">
                  Real-time availability, conflict detection, and automated
                  scheduling
                </p>
              </div>

              <div className="feature-card bg-background/80 backdrop-blur-sm p-8 rounded-2xl hover-lift border border-border/50">
                <Shield className="h-12 w-12 text-primary mb-6" />
                <h3 className="text-xl font-bold mb-4">Role-Based Access</h3>
                <p className="text-muted-foreground">
                  Secure access control for students, faculty, and administrators
                </p>
              </div>

              <div className="feature-card bg-background/80 backdrop-blur-sm p-8 rounded-2xl hover-lift border border-border/50">
                <Users className="h-12 w-12 text-primary mb-6" />
                <h3 className="text-xl font-bold mb-4">Collaborative Platform</h3>
                <p className="text-muted-foreground">
                  Seamless coordination between all university stakeholders
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Classrooms Grid */}
        <section ref={classroomsRef} className="py-20 px-4">
          <div className="container mx-auto">
            <h2 className="text-4xl font-bold text-center mb-12">
              Available Classrooms
            </h2>

            {loading ? (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <div key={i} className="bg-background/80 backdrop-blur-sm rounded-lg h-64 animate-pulse border border-border/50" />
                ))}
              </div>
            ) : (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredClassrooms.map((classroom) => (
                  <ClassroomCard
                    key={classroom.id}
                    classroom={classroom}
                    onSelect={handleClassroomSelect}
                  />
                ))}
              </div>
            )}

            {!loading && filteredClassrooms.length === 0 && (
              <div className="text-center py-16">
                <p className="text-xl text-muted-foreground">
                  No classrooms found matching your search.
                </p>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Index;