/* 
 * Default error page
*/

import { useNavigate } from "react-router-dom";
import { BookOpen, ArrowLeft } from "lucide-react";

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="text-center">
        <div className="w-16 h-16 rounded-2xl bg-blue-100 flex items-center justify-center mx-auto mb-4">
          <BookOpen size={28} className="text-blue-600" />
        </div>
        <h1 className="text-slate-800 mb-2" style={{ fontSize: "3rem", fontWeight: 700, lineHeight: 1 }}>404</h1>
        <p className="text-slate-500 mb-4">Oops! This page doesn't exist.</p>
        <button
          onClick={() => navigate("/dashboard")}
          className="flex items-center gap-2 mx-auto px-5 py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-xl text-sm transition-colors"
          style={{ fontWeight: 600 }}
        >
          <ArrowLeft size={15} />
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}
