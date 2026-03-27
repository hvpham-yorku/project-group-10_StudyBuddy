export const STUDY_BUDDY_TOKEN_KEY = "studyBuddyToken";
export const DEV_ACTOR_STORAGE_KEY = "studybuddy.dev.actorId";

export function getAuthToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(STUDY_BUDDY_TOKEN_KEY);
}

export function setAuthToken(token: string): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(STUDY_BUDDY_TOKEN_KEY, token);
}

export function clearAuthState(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(STUDY_BUDDY_TOKEN_KEY);
  window.sessionStorage.removeItem(DEV_ACTOR_STORAGE_KEY);
}
