import type { EntryStatus, JudgmentOutcome, JudgmentType, ReservistStatus } from '../api/types';

const RESERVIST_STATUS_LABEL: Record<ReservistStatus, string> = {
  BEFORE_CALLUP: '소집전',
  ENTERED: '입영',
  LATE: '지연',
  EARLY_DISCHARGE: '조기퇴소',
  COMPLETED: '완료',
};

const RESERVIST_STATUS_CLASS: Record<ReservistStatus, string> = {
  BEFORE_CALLUP: 'badge-neutral',
  ENTERED: 'badge-success',
  LATE: 'badge-warning',
  EARLY_DISCHARGE: 'badge-warning',
  COMPLETED: 'badge-success',
};

const ENTRY_STATUS_LABEL: Record<EntryStatus, string> = {
  WAITING: '대기',
  COMPLETED: '완료',
  LATE: '지연',
  NO_SHOW: '미입영',
};

const JUDGMENT_TYPE_LABEL: Record<JudgmentType, string> = {
  LATE_ENTRY: '지연입소',
  EARLY_DISCHARGE: '조기퇴소',
};

const JUDGMENT_OUTCOME_LABEL: Record<JudgmentOutcome, string> = {
  ALLOWED: '허용',
  DENIED: '불허',
};

export function ReservistStatusBadge({ status }: { status: ReservistStatus }) {
  return <span className={`badge ${RESERVIST_STATUS_CLASS[status]}`}>{RESERVIST_STATUS_LABEL[status]}</span>;
}

export function EntryStatusBadge({ status }: { status: EntryStatus | null }) {
  if (!status) return <span className="badge badge-neutral">-</span>;
  const cls = status === 'LATE' ? 'badge-warning' : status === 'NO_SHOW' ? 'badge-danger' : 'badge-success';
  return <span className={`badge ${cls}`}>{ENTRY_STATUS_LABEL[status]}</span>;
}

export function JudgmentTypeLabel({ type }: { type: JudgmentType }) {
  return <>{JUDGMENT_TYPE_LABEL[type]}</>;
}

export function JudgmentOutcomeBadge({ outcome }: { outcome: JudgmentOutcome }) {
  return (
    <span className={`badge ${outcome === 'ALLOWED' ? 'badge-success' : 'badge-danger'}`}>
      {JUDGMENT_OUTCOME_LABEL[outcome]}
    </span>
  );
}

export const judgmentOutcomeLabel = (outcome: JudgmentOutcome) => JUDGMENT_OUTCOME_LABEL[outcome];
export const judgmentTypeLabel = (type: JudgmentType) => JUDGMENT_TYPE_LABEL[type];
