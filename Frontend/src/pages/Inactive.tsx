export default function Inactive() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-slate-100 p-8">
      <h1 className="text-4xl font-bold mb-2">Session Timed Out</h1>
      <p className="text-slate-600 mb-6">You were inactive for too long.</p>

      <a
        href="/"
        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
      >
        Return to Login
      </a>
    </div>
  );
}
