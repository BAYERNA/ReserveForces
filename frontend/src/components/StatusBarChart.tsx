import type { TargetStatus } from '../api/types';

const STATUS_ORDER: TargetStatus[] = ['EXPECTED', 'ARRIVED', 'DELAYED', 'ABSENT', 'EXCEPTION', 'COMPLETED'];

const STATUS_LABEL: Record<TargetStatus, string> = {
  EXPECTED: '소집전',
  ARRIVED: '입영완료',
  DELAYED: '지연',
  ABSENT: '미입영',
  EXCEPTION: '예외',
  COMPLETED: '완료',
};

const STATUS_COLOR: Record<TargetStatus, string> = {
  EXPECTED: 'var(--color-text-muted)',
  ARRIVED: 'var(--color-success)',
  DELAYED: 'var(--color-warning)',
  ABSENT: 'var(--color-danger)',
  EXCEPTION: 'var(--color-danger)',
  COMPLETED: 'var(--color-accent)',
};

export function StatusBarChart({ statusCounts, total }: { statusCounts: Record<TargetStatus, number>; total: number }) {
  const max = Math.max(1, ...STATUS_ORDER.map((s) => statusCounts[s] ?? 0));

  return (
    <div className="bar-chart">
      {STATUS_ORDER.map((status) => {
        const count = statusCounts[status] ?? 0;
        const widthPct = (count / max) * 100;
        return (
          <div className="bar-row" key={status}>
            <div className="bar-row-label">{STATUS_LABEL[status]}</div>
            <div className="bar-track">
              <div className="bar-fill" style={{ width: `${widthPct}%`, background: STATUS_COLOR[status] }} />
            </div>
            <div className="bar-row-value">
              {count}명{total > 0 && <span className="bar-row-pct"> ({((count / total) * 100).toFixed(0)}%)</span>}
            </div>
          </div>
        );
      })}
    </div>
  );
}
