import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useInactivityTimer } from '../hooks/useInactivityTimer';
import * as auth from '../lib/auth';

// Mock useNavigate at module level so no Router wrapper is needed
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return { ...actual, useNavigate: () => mockNavigate };
});

// Mock clearAuthState
vi.mock('../lib/auth', () => ({
  clearAuthState: vi.fn(),
  getAuthToken: vi.fn(),
  setAuthToken: vi.fn(),
}));

// Silence the logout fetch (no token in localStorage, so fetch isn't actually called,
// but mock it globally just in case)
global.fetch = vi.fn().mockResolvedValue({ ok: true } as Response);

describe('useInactivityTimer', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    mockNavigate.mockClear();
    vi.mocked(auth.clearAuthState).mockClear();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('navigates to /inactive and clears auth state after the timeout elapses', async () => {
    renderHook(() => useInactivityTimer(1)); // 1-minute timeout

    await act(async () => {
      vi.advanceTimersByTime(60 * 1000 + 100);
    });

    expect(auth.clearAuthState).toHaveBeenCalledOnce();
    expect(mockNavigate).toHaveBeenCalledWith('/inactive', { replace: true });
  });

  it('does nothing when timeoutMinutes is 0', async () => {
    renderHook(() => useInactivityTimer(0));

    await act(async () => {
      vi.advanceTimersByTime(300 * 1000);
    });

    expect(mockNavigate).not.toHaveBeenCalled();
    expect(auth.clearAuthState).not.toHaveBeenCalled();
  });

  it('resets the timer when a user click event fires midway through the timeout', async () => {
    renderHook(() => useInactivityTimer(1)); // 60s timeout

    await act(async () => {
      // Advance to just before halfway
      vi.advanceTimersByTime(30 * 1000);
      // User activity resets the timer back to 60s
      window.dispatchEvent(new Event('click'));
      // Advance another 30s — if the timer had NOT reset, it would have fired already
      vi.advanceTimersByTime(30 * 1000);
    });

    // Timer was reset, so 60s has NOT elapsed since last activity
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('fires the logout call with token when one exists in localStorage', async () => {
    localStorage.setItem('studyBuddyToken', 'test-token-123');

    renderHook(() => useInactivityTimer(1));

    await act(async () => {
      vi.advanceTimersByTime(60 * 1000 + 100);
      // Flush the fetch promise
      await Promise.resolve();
    });

    expect(global.fetch).toHaveBeenCalledWith(
      '/api/auth/logout',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ Authorization: 'Bearer test-token-123' }),
      })
    );
  });

  it('cleans up event listeners and timer on unmount', async () => {
    const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener');
    const { unmount } = renderHook(() => useInactivityTimer(1));

    unmount();

    // Cleanup should have deregistered the 4 activity event types
    const removedEventTypes = removeEventListenerSpy.mock.calls.map(([type]) => type);
    expect(removedEventTypes).toContain('mousemove');
    expect(removedEventTypes).toContain('keydown');
    expect(removedEventTypes).toContain('click');
    expect(removedEventTypes).toContain('scroll');
  });
});
