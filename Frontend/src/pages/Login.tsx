/* 
 * Login.tsx
 * The login page for StudyBuddy
 */

import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { BookOpen, Eye, EyeOff, AlertCircle, Mail } from "lucide-react";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const validateDomain = (e: string) => {
    return e.endsWith("@my.yorku.ca") || e.endsWith("@yorku.ca");
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    if (!validateDomain(email)) {
      setError("Only @my.yorku.ca or @yorku.ca email addresses are allowed.");
      return;
    }
    if (password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      navigate("/2fa");
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-950 via-blue-900 to-blue-800 flex items-center justify-center p-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"></div>
      </div>

      <div className="w-full max-w-sm relative">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-orange-500 flex items-center justify-center mb-3 shadow-lg">
            <BookOpen size={32} className="text-white" />
          </div>
          <h1 className="text-white" style={{ fontSize: "1.75rem", fontWeight: 700, lineHeight: 1.2 }}>
            StudyBuddy
          </h1>
          <p className="text-blue-300 text-sm mt-1">York University Study Sessions</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-slate-800 mb-1" style={{ fontSize: "1.25rem", fontWeight: 700 }}>Welcome back</h2>
          <p className="text-slate-500 text-sm mb-6">Sign in to your account</p>

          {error && (
            <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-3 py-2 mb-4">
              <AlertCircle size={15} className="text-red-500 shrink-0 mt-0.5" />
              <p className="text-xs text-red-600">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                York University Email
              </label>
              <div className="relative">
                <Mail size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@my.yorku.ca"
                  className="w-full pl-9 pr-4 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-slate-50"
                  required
                />
              </div>
              <p className="text-xs text-slate-400 mt-1">Must be a @my.yorku.ca or @yorku.ca address</p>
            </div>

            <div>
              <label className="block text-sm text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                Password
              </label>
              <div className="relative">
                <input
                  type={showPass ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  className="w-full px-4 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-slate-50 pr-10"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" className="rounded border-slate-300 accent-blue-600" />
                <span className="text-xs text-slate-500">Remember me</span>
              </label>
              <button type="button" className="text-xs text-blue-600 hover:underline">Forgot password?</button>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors disabled:opacity-60"
              style={{ fontWeight: 600 }}
            >
              {loading ? "Signing in..." : "Sign In"}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-4">
            Don't have an account?{" "}
            <Link to="/register" className="text-orange-500 hover:underline" style={{ fontWeight: 600 }}>
              Sign up
            </Link>
          </p>
        </div>

        <p className="text-center text-xs text-blue-300 mt-4">
          © 2026 StudyBuddy · York University
        </p>
      </div>
    </div>
  );
}