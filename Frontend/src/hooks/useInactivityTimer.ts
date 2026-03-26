import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { clearAuthState } from "../lib/auth";

export function useInactivityTimer(timeoutMinutes: number) {
  const navigate = useNavigate();
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  const resetTimer = () => {
    if (timerRef.current) clearTimeout(timerRef.current);

    timerRef.current = setTimeout(async () => {
      // Invalidate session on backend and clear client auth
      const token = localStorage.getItem("studyBuddyToken");
      if (token) {
        try {
          await fetch("/api/auth/logout", {
            method: "POST",
            headers: {
              "Authorization": "Bearer " + token,
            },
          });
        } catch (err) {
          console.warn("Failed to call backend logout", err);
        }
      }
      
      clearAuthState();
      navigate("/inactive", { replace: true });
    }, timeoutMinutes * 60 * 1000);
  };

  useEffect(() => {
    if (!timeoutMinutes) return;

    const events = ["mousemove", "keydown", "click", "scroll"];

    events.forEach((event) => {
      window.addEventListener(event, resetTimer);
    });

    resetTimer();

    return () => {
      events.forEach((event) => {
        window.removeEventListener(event, resetTimer);
      });
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [timeoutMinutes]);
}
