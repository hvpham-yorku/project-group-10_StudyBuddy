import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { BookOpen, Shield, MailCheck, AlertCircle } from "lucide-react";
import { setAuthToken } from "../lib/auth";

export default function TwoFA() {
  const navigate = useNavigate();
  const location = useLocation();

  const registeredEmail = location.state?.email || "your email";
  // mode: "otp" = 2FA code entry after login, "verify" = post-registration email verification
  const mode: "otp" | "verify" = location.state?.mode ?? "verify";

  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleCheckVerification = () => {
    navigate("/", { replace: true });
  };

  const handleVerifyOTP = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await fetch("/api/auth/verify-2fa", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: registeredEmail, code }),
      });
      if (response.ok) {
        const token = await response.text();
        setAuthToken(token);
        window.location.assign("/dashboard");
      } else {
        const msg = await response.text();
        setError(msg || "Incorrect code. Please try again.");
      }
    } catch {
      setError("Could not connect to the server.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-950 via-blue-900 to-blue-800 flex items-center justify-center p-4">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"></div>
      </div>

      <div className="w-full max-w-sm relative">
        <div className="flex flex-col items-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-orange-500 flex items-center justify-center mb-3 shadow-lg">
            <BookOpen size={32} className="text-white" />
          </div>
          <h1 className="text-white" style={{ fontSize: "1.75rem", fontWeight: 700 }}>StudyBuddy</h1>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8 text-center">
          <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center mx-auto mb-4">
            <Shield size={28} className="text-blue-700" />
          </div>

          {mode === "otp" ? (
            <>
              <h2 className="text-slate-800 mb-2" style={{ fontSize: "1.25rem", fontWeight: 700 }}>
                Two-Factor Authentication
              </h2>
              <p className="text-slate-500 text-sm mb-1">
                A 6-digit code was sent to:
              </p>
              <p className="text-blue-700 text-sm mb-6 bg-blue-50 py-2 rounded-md border border-blue-100" style={{ fontWeight: 600 }}>
                {registeredEmail}
              </p>

              {error && (
                <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-3 py-3 mb-4 text-left">
                  <AlertCircle size={18} className="text-red-500 shrink-0 mt-0.5" />
                  <p className="text-xs text-red-700 font-medium">{error}</p>
                </div>
              )}

              <form onSubmit={handleVerifyOTP} className="space-y-4">
                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  value={code}
                  onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))}
                  placeholder="Enter 6-digit code"
                  className="w-full text-center tracking-widest text-xl px-4 py-3 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                  required
                />
                <button
                  type="submit"
                  disabled={loading || code.length !== 6}
                  className="w-full py-3 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors flex items-center justify-center gap-2 disabled:opacity-70"
                  style={{ fontWeight: 600 }}
                >
                  <MailCheck size={18} />
                  {loading ? "Verifying..." : "Verify Code"}
                </button>
              </form>

              <div className="pt-4 mt-4 border-t border-slate-100">
                <p className="text-xs text-slate-400">
                  Code expires in 5 minutes.{" "}
                  <button onClick={() => navigate("/", { replace: true })} className="text-blue-600 hover:underline font-medium">
                    Back to login
                  </button>
                </p>
              </div>
            </>
          ) : (
            <>
              <h2 className="text-slate-800 mb-2" style={{ fontSize: "1.25rem", fontWeight: 700 }}>
                Verify Your Email
              </h2>
              <p className="text-slate-500 text-sm mb-1">
                We sent a verification link to:
              </p>
              <p className="text-blue-700 text-sm mb-6 bg-blue-50 py-2 rounded-md border border-blue-100" style={{ fontWeight: 600 }}>
                {registeredEmail}
              </p>

              <div className="bg-slate-50 rounded-lg p-4 mb-6 text-left border border-slate-100">
                <p className="text-sm text-slate-600 mb-2"><strong>Step 1:</strong> Open your YorkU email inbox.</p>
                <p className="text-sm text-slate-600 mb-2"><strong>Step 2:</strong> Click the secure link we sent you.</p>
                <p className="text-sm text-slate-600"><strong>Step 3:</strong> Come back here and click the button below!</p>
              </div>

              <button
                onClick={handleCheckVerification}
                className="w-full py-3 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors flex items-center justify-center gap-2 mb-4"
                style={{ fontWeight: 600 }}
              >
                <MailCheck size={18} />
                Take me back to Login
              </button>

              <div className="pt-4 border-t border-slate-100">
                <p className="text-xs text-slate-400">
                  Didn't get the email?{" "}
                  <button className="text-blue-600 hover:underline font-medium">Check spam folder</button>
                </p>
              </div>
            </>
          )}
        </div>

        <p className="text-center text-xs text-blue-300 mt-4">© 2026 StudyBuddy · York University</p>
      </div>
    </div>
  );
}