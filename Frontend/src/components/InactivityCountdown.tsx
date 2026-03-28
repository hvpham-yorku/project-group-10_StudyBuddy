import { Clock } from "lucide-react";
import { useInactivityCountdown } from "../contexts/InactivityCountdownContext";

export function InactivityCountdown() {
  const { secondsRemaining } = useInactivityCountdown();
  const warningWindowSeconds = 120;

  // Show timer only when 2 minutes (120 seconds) or less remain
  if (secondsRemaining > warningWindowSeconds) {
    return null;
  }

  const minutes = Math.floor(secondsRemaining / 60);
  const seconds = secondsRemaining % 60;
  const percentageRemaining =
    (Math.min(secondsRemaining, warningWindowSeconds) / warningWindowSeconds) * 100;

  // Red color — urgency always present in the 2-minute window
  const bgColor = secondsRemaining <= 60 ? "bg-red-600" : "bg-red-500";
  const borderColor = secondsRemaining <= 60 ? "border-red-700" : "border-red-600";

  return (
    <div
      className={`fixed top-0 left-1/2 -translate-x-1/2 z-50 ${bgColor} ${borderColor} border-2 rounded-b-xl shadow-2xl px-6 py-3 flex items-center gap-3 animate-pulse`}
    >
      <Clock size={24} className="text-white flex-shrink-0" />
      <div>
        <p className="text-white text-sm font-bold">
          Session expires in:{" "}
          <span className="text-lg font-mono">
            {String(minutes).padStart(2, "0")}:{String(seconds).padStart(2, "0")}
          </span>
        </p>
        <div className="w-48 h-1 bg-white/30 rounded-full mt-1">
          <div
            className="h-full bg-white rounded-full transition-all"
            style={{ width: `${percentageRemaining}%` }}
          />
        </div>
      </div>
    </div>
  );
}
