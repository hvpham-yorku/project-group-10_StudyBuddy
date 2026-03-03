import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { BookOpen, Shield, MailCheck, AlertCircle } from "lucide-react";

export default function TwoFA() {
  const navigate = useNavigate();
  const location = useLocation();
  
  // Grab the email passed from the Register page (fallback if accessed directly)
  const registeredEmail = location.state?.email || "your email";

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleCheckVerification = () => {
  // We don't have the password here to generate a token, 
  // so we route them back to the login page to sign in!
  navigate("/", { replace: true });
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
            <p className="text-sm text-slate-600 mb-2">
              <strong>Step 1:</strong> Open your YorkU email inbox.
            </p>
            <p className="text-sm text-slate-600 mb-2">
              <strong>Step 2:</strong> Click the secure link we sent you.
            </p>
            <p className="text-sm text-slate-600">
              <strong>Step 3:</strong> Come back here and click the button below!
            </p>
          </div>

          {error && (
            <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-3 py-3 mb-6 text-left">
              <AlertCircle size={18} className="text-red-500 shrink-0 mt-0.5" />
              <p className="text-xs text-red-700 font-medium">{error}</p>
            </div>
          )}

          <button
            onClick={handleCheckVerification}
            className="w-full py-3 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors flex items-center justify-center gap-2 disabled:opacity-70 mb-4"
            style={{ fontWeight: 600 }}
          >
            <MailCheck size={18} />
            {"Take me back to Login"}
          </button>

          <div className="pt-4 border-t border-slate-100">
            <p className="text-xs text-slate-400">
              Didn't get the email? <button className="text-blue-600 hover:underline font-medium">Check spam folder</button>
            </p>
          </div>
        </div>

        <p className="text-center text-xs text-blue-300 mt-4">© 2026 StudyBuddy · York University</p>
      </div>
    </div>
  );
}