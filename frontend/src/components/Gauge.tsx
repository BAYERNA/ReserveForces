interface GaugeProps {
  value: number;
  valueLabel: string;
  color: string;
  size?: number;
}

export function Gauge({ value, valueLabel, color, size = 104 }: GaugeProps) {
  const stroke = 9;
  const radius = (size - stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, value));
  const dash = (clamped / 100) * circumference;
  const center = size / 2;
  const angle = -90 + (clamped / 100) * 360;
  const rad = (angle * Math.PI) / 180;
  const dotX = center + radius * Math.cos(rad);
  const dotY = center + radius * Math.sin(rad);

  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="gauge-svg">
      <circle cx={center} cy={center} r={radius} fill="none" stroke="var(--color-neutral-bg)" strokeWidth={stroke} />
      {clamped > 0 && (
        <>
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke={color}
            strokeWidth={stroke}
            strokeDasharray={`${dash} ${circumference - dash}`}
            strokeLinecap="round"
            transform={`rotate(-90 ${center} ${center})`}
          />
          <circle cx={dotX} cy={dotY} r={stroke / 2 - 0.5} fill={color} stroke="var(--color-surface)" strokeWidth={2} />
        </>
      )}
      <text x={center} y={center} textAnchor="middle" dominantBaseline="central" fill="var(--color-text)" style={{ fontSize: size * 0.19, fontWeight: 700 }}>
        {valueLabel}
      </text>
    </svg>
  );
}
