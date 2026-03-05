import { useEffect, useState } from "react";
import { onAuthStateChanged, User } from "firebase/auth";
import { auth } from "..//firebase"; // adjust path to your firebase init

export function useAuthUid() {
  const [user, setUser] = useState<User | null>(null);
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, (u) => {
      setUser(u ?? null);
      setAuthReady(true);
    });
    return () => unsub();
  }, []);

  return {
    uid: user?.uid ?? null,
    user,
    authReady,
  };
}