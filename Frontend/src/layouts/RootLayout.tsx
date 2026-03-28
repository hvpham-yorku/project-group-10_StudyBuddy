import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";
import { InactivityCountdownProvider } from "../contexts/InactivityCountdownContext";

export function RootLayout() {
  const [autoTimeout, setAutoTimeout] = useState<number>(5);

  useEffect(() => {
    async function fetchProfile() {
      const token = localStorage.getItem("studyBuddyToken");
      if (!token) return;

      try {
        const res = await fetch("/api/studentcontroller/profile", {
          headers: { "Authorization": "Bearer " + token },
        });

        if (!res.ok) {
          localStorage.removeItem("studyBuddyToken");
          return;
        }

        const data = await res.json();
        const timeout = Number(data?.autoTimeout ?? 5);
        if (timeout > 0) {
          setAutoTimeout(timeout);
        }
      } catch (err) {
        console.error("Failed to load profile for timeout", err);
      }
    }

    fetchProfile();
  }, []);

  useEffect(() => {
    const onTimeoutUpdated = (event: Event) => {
      const value = Number((event as CustomEvent<number>).detail);
      if (Number.isFinite(value) && value > 0) {
        setAutoTimeout(value);
      }
    };

    window.addEventListener("studybuddy:auto-timeout-updated", onTimeoutUpdated);

    return () => {
      window.removeEventListener("studybuddy:auto-timeout-updated", onTimeoutUpdated);
    };
  }, []);

  return (
    <InactivityCountdownProvider timeoutMinutes={autoTimeout}>
      <Outlet />
    </InactivityCountdownProvider>
  );
}
