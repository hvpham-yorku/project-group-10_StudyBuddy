import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { InactivityCountdown } from '../components/InactivityCountdown';

// Mock the context hook so we control secondsRemaining in each test
vi.mock('../contexts/InactivityCountdownContext', () => ({
  useInactivityCountdown: vi.fn(),
}));

import { useInactivityCountdown } from '../contexts/InactivityCountdownContext';

const mockUseCountdown = vi.mocked(useInactivityCountdown);

describe('InactivityCountdown', () => {
  const noop = vi.fn();

  beforeEach(() => {
    noop.mockClear();
  });

  it('renders nothing when more than 120 seconds remain', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 300, totalSeconds: 3600, resetActivity: noop });

    const { container } = render(<InactivityCountdown />);
    expect(container.firstChild).toBeNull();
  });

  it('renders nothing at exactly 121 seconds remaining', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 121, totalSeconds: 3600, resetActivity: noop });

    const { container } = render(<InactivityCountdown />);
    expect(container.firstChild).toBeNull();
  });

  it('renders the countdown banner at exactly 120 seconds remaining', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 120, totalSeconds: 3600, resetActivity: noop });

    render(<InactivityCountdown />);
    expect(screen.getByText(/Session expires in/i)).toBeInTheDocument();
  });

  it('renders the countdown banner when under 120 seconds remain', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 90, totalSeconds: 3600, resetActivity: noop });

    render(<InactivityCountdown />);
    expect(screen.getByText(/Session expires in/i)).toBeInTheDocument();
  });

  it('formats time as MM:SS correctly (65 seconds → 01:05)', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 65, totalSeconds: 3600, resetActivity: noop });

    render(<InactivityCountdown />);
    expect(screen.getByText('01:05')).toBeInTheDocument();
  });

  it('formats time as MM:SS correctly (0 seconds → 00:00)', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 0, totalSeconds: 3600, resetActivity: noop });

    render(<InactivityCountdown />);
    expect(screen.getByText('00:00')).toBeInTheDocument();
  });

  it('applies bg-red-600 class when 60 or fewer seconds remain', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 45, totalSeconds: 3600, resetActivity: noop });

    const { container } = render(<InactivityCountdown />);
    expect(container.firstChild).toHaveClass('bg-red-600');
  });

  it('applies bg-red-500 class when between 61 and 120 seconds remain', () => {
    mockUseCountdown.mockReturnValue({ secondsRemaining: 90, totalSeconds: 3600, resetActivity: noop });

    const { container } = render(<InactivityCountdown />);
    expect(container.firstChild).toHaveClass('bg-red-500');
    expect(container.firstChild).not.toHaveClass('bg-red-600');
  });
});
