import { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { BookOpen, Shield, RefreshCw, CheckCircle } from "lucide-react";

export default function TwoFA() {
  const navigate = useNavigate();
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [resent, setResent] = useState(false);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleChange = (idx: number, val: string) => {
    if (!/^\d*$/.test(val)) return;
    const newCode = [...code];
    newCode[idx] = val.slice(-1);
    setCode(newCode);
    if (val && idx < 5) inputRefs.current[idx + 1]?.focus();
  };

  const handleKeyDown = (idx: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace" && !code[idx] && idx > 0) {
      inputRefs.current[idx - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (pasted.length === 6) {
      setCode(pasted.split(""));
      inputRefs.current[5]?.focus();
    }
  };

  const handleVerify = () => {
    const fullCode = code.join("");
    if (fullCode.length < 6) {
      setError("Please enter all 6 digits");
      return;
    }
    setLoading(true);
    setError("");
    setTimeout(() => {
      setLoading(false);
      // Any 6-digit code works in mock mode
      navigate("/dashboard");
    }, 1200);
  };

  const handleResend = () => {
    setResent(true);
    setTimeout(() => setResent(false), 3000);
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
          <h2 className="text-slate-800 mb-2" style={{ fontSize: "1.25rem", fontWeight: 700 }}>Two-Factor Authentication</h2>
          <p className="text-slate-500 text-sm mb-1">
            We sent a 6-digit verification code to
          </p>
          <p className="text-blue-700 text-sm mb-6" style={{ fontWeight: 600 }}>
            a***@my.yorku.ca
          </p>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 mb-4 text-xs text-red-600 text-left">
              {error}
            </div>
          )}

          {/* OTP Inputs */}
          <div className="flex gap-2 justify-center mb-6" onPaste={handlePaste}>
            {code.map((digit, idx) => (
              <input
                key={idx}
                ref={(el) => { inputRefs.current[idx] = el; }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(idx, e.target.value)}
                onKeyDown={(e) => handleKeyDown(idx, e)}
                className="w-10 h-12 text-center border-2 border-slate-200 rounded-lg focus:outline-none focus:border-blue-500 transition-colors text-slate-800 bg-slate-50"
                style={{ fontSize: "1.25rem", fontWeight: 700 }}
              />
            ))}
          </div>

          <button
            onClick={handleVerify}
            disabled={loading}
            className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors disabled:opacity-60 mb-3"
            style={{ fontWeight: 600 }}
          >
            {loading ? "Verifying..." : "Verify Code"}
          </button>

          <div className="flex items-center justify-center gap-2">
            <p className="text-sm text-slate-500">Didn't receive it?</p>
            {resent ? (
              <div className="flex items-center gap-1 text-green-600">
                <CheckCircle size={14} />
                <span className="text-xs">Sent!</span>
              </div>
            ) : (
              <button
                onClick={handleResend}
                className="text-sm text-orange-500 hover:underline flex items-center gap-1"
                style={{ fontWeight: 600 }}
              >
                <RefreshCw size={13} />
                Resend
              </button>
            )}
          </div>

          <div className="mt-4 pt-4 border-t border-slate-100">
            <p className="text-xs text-slate-400">
              Having trouble? <button className="text-blue-600 hover:underline">Contact support</button>
            </p>
          </div>
        </div>

        <p className="text-center text-xs text-blue-300 mt-4">© 2026 StudyBuddy · York University</p>
      </div>
    </div>
  );
}