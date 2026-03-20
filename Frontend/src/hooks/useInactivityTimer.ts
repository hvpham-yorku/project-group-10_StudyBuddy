import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { clearAuthState } from "../lib/auth";

export function useInactivityTimer(timeoutMinutes: number) {
  const navigate = useNavigate();
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  const resetTimer = () => {
    if (timerRef.current) clearTimeout(timerRef.current);

    timerRef.current = setTimeout(() => {
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
