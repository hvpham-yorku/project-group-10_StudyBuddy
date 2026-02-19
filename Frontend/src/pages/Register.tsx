import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { BookOpen, Eye, EyeOff, AlertCircle, Mail, User, GraduationCap } from "lucide-react";

const majors = [
  "Computer Science (EECS)",
  "Software Engineering",
  "Mathematics",
  "Physics",
  "Biology",
  "Chemistry",
  "Psychology",
  "Business Administration",
  "Economics",
  "Political Science",
  "Philosophy",
  "English",
  "History",
  "Sociology",
  "Other",
];

const years = ["1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"];

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    major: "",
    year: "",
  });
  const [showPass, setShowPass] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [agreed, setAgreed] = useState(false);

  const set = (k: string, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.firstName.trim()) e.firstName = "First name is required";
    if (!form.lastName.trim()) e.lastName = "Last name is required";
    if (!form.email.endsWith("@my.yorku.ca") && !form.email.endsWith("@yorku.ca"))
      e.email = "Must use a @my.yorku.ca or @yorku.ca address";
    if (form.password.length < 8) e.password = "Password must be at least 8 characters";
    if (form.password !== form.confirmPassword) e.confirmPassword = "Passwords do not match";
    if (!form.major) e.major = "Please select your major";
    if (!form.year) e.year = "Please select your year";
    if (!agreed) e.agreed = "You must agree to the terms";
    return e;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const e2 = validate();
    setErrors(e2);
    if (Object.keys(e2).length > 0) return;
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      navigate("/2fa");
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-950 via-blue-900 to-blue-800 flex items-center justify-center p-4">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"></div>
      </div>

      <div className="w-full max-w-md relative">
        {/* Logo */}
        <div className="flex flex-col items-center mb-6">
          <div className="w-14 h-14 rounded-2xl bg-orange-500 flex items-center justify-center mb-2 shadow-lg">
            <BookOpen size={28} className="text-white" />
          </div>
          <h1 className="text-white" style={{ fontSize: "1.5rem", fontWeight: 700 }}>StudyBuddy</h1>
          <p className="text-blue-300 text-sm">York University Study Sessions</p>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-slate-800 mb-1" style={{ fontSize: "1.2rem", fontWeight: 700 }}>Create your account</h2>
          <p className="text-slate-500 text-sm mb-6">Join York University's study community</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>First Name</label>
                <div className="relative">
                  <User size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    value={form.firstName}
                    onChange={(e) => set("firstName", e.target.value)}
                    placeholder="Alex"
                    className="w-full pl-8 pr-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                  />
                </div>
                {errors.firstName && <p className="text-xs text-red-500 mt-0.5">{errors.firstName}</p>}
              </div>
              <div>
                <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Last Name</label>
                <input
                  value={form.lastName}
                  onChange={(e) => set("lastName", e.target.value)}
                  placeholder="Johnson"
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                />
                {errors.lastName && <p className="text-xs text-red-500 mt-0.5">{errors.lastName}</p>}
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>York University Email</label>
              <div className="relative">
                <Mail size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => set("email", e.target.value)}
                  placeholder="you@my.yorku.ca"
                  className="w-full pl-8 pr-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                />
              </div>
              {errors.email ? (
                <div className="flex items-center gap-1 mt-0.5">
                  <AlertCircle size={11} className="text-red-500" />
                  <p className="text-xs text-red-500">{errors.email}</p>
                </div>
              ) : (
                <p className="text-xs text-slate-400 mt-0.5">Must be @my.yorku.ca or @yorku.ca</p>
              )}
            </div>

            {/* Major & Year */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Major</label>
                <div className="relative">
                  <GraduationCap size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <select
                    value={form.major}
                    onChange={(e) => set("major", e.target.value)}
                    className="w-full pl-8 pr-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 appearance-none"
                  >
                    <option value="">Select...</option>
                    {majors.map((m) => <option key={m} value={m}>{m}</option>)}
                  </select>
                </div>
                {errors.major && <p className="text-xs text-red-500 mt-0.5">{errors.major}</p>}
              </div>
              <div>
                <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Year</label>
                <select
                  value={form.year}
                  onChange={(e) => set("year", e.target.value)}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 appearance-none"
                >
                  <option value="">Select...</option>
                  {years.map((y) => <option key={y} value={y}>{y}</option>)}
                </select>
                {errors.year && <p className="text-xs text-red-500 mt-0.5">{errors.year}</p>}
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Password</label>
              <div className="relative">
                <input
                  type={showPass ? "text" : "password"}
                  value={form.password}
                  onChange={(e) => set("password", e.target.value)}
                  placeholder="Min. 8 characters"
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 pr-10"
                />
                <button type="button" onClick={() => setShowPass(!showPass)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
                  {showPass ? <EyeOff size={14} /> : <Eye size={14} />}
                </button>
              </div>
              {errors.password && <p className="text-xs text-red-500 mt-0.5">{errors.password}</p>}
            </div>

            <div>
              <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Confirm Password</label>
              <input
                type="password"
                value={form.confirmPassword}
                onChange={(e) => set("confirmPassword", e.target.value)}
                placeholder="Repeat password"
                className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
              />
              {errors.confirmPassword && <p className="text-xs text-red-500 mt-0.5">{errors.confirmPassword}</p>}
            </div>

            {/* Terms */}
            <div>
              <label className="flex items-start gap-2 cursor-pointer">
                <input type="checkbox" checked={agreed} onChange={(e) => setAgreed(e.target.checked)} className="mt-0.5 accent-blue-600" />
                <span className="text-xs text-slate-500 leading-relaxed">
                  I agree to StudyBuddy's{" "}
                  <button type="button" className="text-blue-600 hover:underline">Terms of Service</button> and{" "}
                  <button type="button" className="text-blue-600 hover:underline">Privacy Policy</button>
                </span>
              </label>
              {errors.agreed && <p className="text-xs text-red-500 mt-0.5">{errors.agreed}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-sm transition-colors disabled:opacity-60"
              style={{ fontWeight: 600 }}
            >
              {loading ? "Creating account..." : "Create Account"}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-4">
            Already have an account?{" "}
            <Link to="/" className="text-orange-500 hover:underline" style={{ fontWeight: 600 }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}