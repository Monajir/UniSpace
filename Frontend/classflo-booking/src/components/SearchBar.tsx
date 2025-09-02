import { useState, useEffect, useRef } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search, X } from "lucide-react";
import { gsap } from "gsap";

interface SearchBarProps {
  onSearch: (query: string) => void;
  placeholder?: string;
}

export function SearchBar({ onSearch, placeholder = "Search classrooms by room number..." }: SearchBarProps) {
  const [query, setQuery] = useState("");
  const searchRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (searchRef.current) {
      gsap.fromTo(searchRef.current,
        { scale: 0.95, opacity: 0 },
        { scale: 1, opacity: 1, duration: 0.4, ease: "power2.out" }
      );
    }
  }, []);

  const handleSearch = () => {
    onSearch(query);
  };

  const handleClear = () => {
    setQuery("");
    onSearch("");
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div ref={searchRef} className="relative max-w-md mx-auto">
      <div className="relative flex items-center">
        <Input
          type="text"
          placeholder={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyPress={handleKeyPress}
          className="pr-20 pl-10 h-12 text-lg glass"
        />
        <Search className="absolute left-3 h-5 w-5 text-muted-foreground" />
        
        <div className="absolute right-2 flex items-center gap-1">
          {query && (
            <Button
              size="sm"
              variant="ghost"
              onClick={handleClear}
              className="h-8 w-8 p-0 hover-scale"
            >
              <X className="h-4 w-4" />
            </Button>
          )}
          <Button
            size="sm"
            onClick={handleSearch}
            className="h-8 hover-scale"
          >
            <Search className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}