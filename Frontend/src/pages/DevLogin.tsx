import { useState } from "react";
import { signInWithEmailAndPassword } from "firebase/auth";
import { auth } from "../firebase";
import { useNavigate } from "react-router-dom";

export default function DevLogin() {
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState<string | null>(null);

  return (
    <div style={{ padding: 24 }}>
      <h2>Dev Login</h2>

      <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      <br />
      <input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <br />

      <button
        onClick={async () => {
          setMsg(null);
          try {
            await signInWithEmailAndPassword(auth, email.trim(), password);
            nav("/network");
          } catch (e: any) {
            setMsg(e?.message ?? "failed");
          }
        }}
      >
        Sign in
      </button>

      {msg && <pre>{msg}</pre>}
    </div>
  );
}