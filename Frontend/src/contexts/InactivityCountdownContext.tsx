import { createContext, useContext, useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";

interface InactivityCountdownContextType {
  secondsRemaining: number;
  totalSeconds: number;
  resetActivity: () => void;
}

const InactivityCountdownContext = createContext<InactivityCountdownContextType | null>(null);

export function InactivityCountdownProvider({
  children,
  timeoutMinutes,
}: {
  children: React.ReactNode;
  timeoutMinutes: number;
}) {
  const navigate = useNavigate();
  const totalSeconds = timeoutMinutes * 60;

  const [secondsRemaining, setSecondsRemaining] = useState(totalSeconds);

  const resetActivity = () => {
    setSecondsRemaining(totalSeconds);
  };

  useEffect(() => {
    if (timeoutMinutes <= 0) return;

    setSecondsRemaining(totalSeconds);

    const intervalId = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          const token = localStorage.getItem("studyBuddyToken");
          if (token) {
            fetch("/api/auth/logout", {
              method: "POST",
              headers: { "Authorization": "Bearer " + token },
            }).catch((err) => console.warn("Logout API failed", err));
          }
          localStorage.removeItem("studyBuddyToken");
          navigate("/inactive");
          clearInterval(intervalId);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      clearInterval(intervalId);
    };
  }, [totalSeconds, timeoutMinutes, navigate]);

  useEffect(() => {
    const events = ["keydown", "click", "scroll"];

    const onUserAction = () => {
      resetActivity();
    };

    events.forEach((event) => {
      window.addEventListener(event, onUserAction);
    });

    return () => {
      events.forEach((event) => {
        window.removeEventListener(event, onUserAction);
      });
    };
  }, [resetActivity]);

  const contextValue = useMemo(
    () => ({ secondsRemaining, totalSeconds, resetActivity }),
    [secondsRemaining, totalSeconds]
  );

  return (
    <InactivityCountdownContext.Provider value={contextValue}>
      {children}
    </InactivityCountdownContext.Provider>
  );
}

export function useInactivityCountdown() {
  const context = useContext(InactivityCountdownContext);
  if (!context) {
    throw new Error("useInactivityCountdown must be used within InactivityCountdownProvider");
  }
  return context;
}
