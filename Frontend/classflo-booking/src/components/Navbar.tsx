import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useAuth } from "@/hooks/useAuth";
import { LogOut, User, Bell } from "lucide-react";
import { gsap } from "gsap";

export function Navbar() {
  const { user, profile, signOut } = useAuth();
  const navigate = useNavigate();
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    // Animate navbar on mount
    gsap.fromTo(".navbar-item", 
      { y: -20, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: "power2.out" }
    );
  }, []);

  const handleSignOut = async () => {
    await signOut();
    navigate("/");
  };

  const getDashboardPath = () => {
    if (profile?.role.toLowerCase() === 'admin') return '/admin';
    return '/profile';
  };

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
      isScrolled ? 'glass shadow-soft' : 'bg-transparent'
    }`}>
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link 
            to="/" 
            className="navbar-item text-2xl font-bold gradient-primary bg-clip-text text-transparent hover-scale"
          >
            UniSpace
          </Link>

          <div className="flex items-center gap-4">
            <ThemeToggle />
            
            {user ? (
              <>
                {/* <Button
                  variant="ghost"
                  size="icon"
                  className="navbar-item hover-scale"
                  onClick={() => navigate('/notifications')}
                >
                  <Bell className="h-4 w-4" />
                </Button> */}
                
                <Button
                  variant="ghost"
                  className="navbar-item hover-scale"
                  onClick={() => navigate(getDashboardPath())}
                >
                  <User className="h-4 w-4 mr-2" />
                  {profile?.full_name || 'Profile'}
                </Button>
                
                <Button
                  variant="outline"
                  className="navbar-item hover-scale"
                  onClick={handleSignOut}
                >
                  <LogOut className="h-4 w-4 mr-2" />
                  Sign Out
                </Button>
              </>
            ) : (
              <Button
                className="navbar-item hover-scale"
                onClick={() => navigate('/auth')}
              >
                Sign In
              </Button>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}