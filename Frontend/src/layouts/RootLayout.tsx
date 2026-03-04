import { Outlet } from "react-router-dom";
import { useInactivityTimer } from "../hooks/useInactivityTimer";

export function RootLayout() {
  // TEMP: hardcode a timeout until real auth is ready
  const autoTimeout = 5; // minutes

  useInactivityTimer(autoTimeout); 

  return <Outlet />;
}
