interface IconProps {
  className?: string;
}

const base = {
  width: 20,
  height: 20,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

export function IconShield({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <path d="M12 2.5 L19.5 5.5 V11.5 C19.5 16.5 16.4 20 12 21.5 C7.6 20 4.5 16.5 4.5 11.5 V5.5 Z" />
      <path d="M9 12 L11 14 L15.5 9.2" />
    </svg>
  );
}

export function IconUsers({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <circle cx="9" cy="8" r="3.2" />
      <path d="M3.5 20c0-3.6 2.6-6 5.5-6s5.5 2.4 5.5 6" />
      <path d="M15.5 8.4a3 3 0 1 1 0-4.8" />
      <path d="M16 14.3c2.5 0.4 4.5 2.6 4.5 5.7" />
    </svg>
  );
}

export function IconTrendingUp({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <polyline points="3 17 9.5 10.5 13.5 14.5 21 6" />
      <polyline points="14.5 6 21 6 21 12.5" />
    </svg>
  );
}

export function IconClock({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5.2l3.2 1.8" />
    </svg>
  );
}

export function IconUserX({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <circle cx="9" cy="8" r="3.2" />
      <path d="M2.5 20.5c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6" />
      <line x1="16" y1="8" x2="21" y2="13" />
      <line x1="21" y1="8" x2="16" y2="13" />
    </svg>
  );
}

export function IconAlertTriangle({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <path d="M12 3.2 L21.5 20 H2.5 Z" />
      <line x1="12" y1="9.5" x2="12" y2="14" />
      <circle cx="12" cy="17.2" r="0.9" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconHistory({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <path d="M3.5 12a8.5 8.5 0 1 0 2.7-6.2" />
      <polyline points="3.2 4.5 3.5 9 8 8.4" />
      <path d="M12 8v4.3l3 1.7" />
    </svg>
  );
}

export function IconClipboard({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <rect x="5.5" y="4.5" width="13" height="16" rx="2" />
      <path d="M9 4.5V3.8a1.5 1.5 0 0 1 1.5-1.5h3a1.5 1.5 0 0 1 1.5 1.5v0.7" />
      <line x1="8.5" y1="11" x2="15.5" y2="11" />
      <line x1="8.5" y1="14.5" x2="15.5" y2="14.5" />
      <line x1="8.5" y1="18" x2="13" y2="18" />
    </svg>
  );
}

export function IconPlayCircle({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <circle cx="12" cy="12" r="9" />
      <path d="M10 8.3 L16 12 L10 15.7 Z" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconSliders({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <line x1="4" y1="6" x2="20" y2="6" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <line x1="4" y1="18" x2="20" y2="18" />
      <circle cx="9" cy="6" r="2" fill="currentColor" stroke="none" />
      <circle cx="16" cy="12" r="2" fill="currentColor" stroke="none" />
      <circle cx="10" cy="18" r="2" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconMail({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <rect x="3.5" y="5.5" width="17" height="13" rx="2" />
      <path d="M4.5 7 L12 12.5 L19.5 7" />
    </svg>
  );
}

export function IconCheckCircle({ className }: IconProps) {
  return (
    <svg {...base} className={className}>
      <circle cx="12" cy="12" r="9" />
      <path d="M8.3 12.3 L10.7 14.7 L15.7 9.3" />
    </svg>
  );
}
