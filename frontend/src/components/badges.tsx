import type { AttendanceStatus, TargetStatus } from '../api/types';

const TARGET_STATUS_LABEL: Record<TargetStatus, string> = {
  EXPECTED: '소집전',
  ARRIVED: '입영완료',
  DELAYED: '지연',
  ABSENT: '미입영',
  EXCEPTION: '예외',
  COMPLETED: '완료',
};

const TARGET_STATUS_CLASS: Record<TargetStatus, string> = {
  EXPECTED: 'badge-neutral',
  ARRIVED: 'badge-success',
  DELAYED: 'badge-warning',
  ABSENT: 'badge-danger',
  EXCEPTION: 'badge-danger',
  COMPLETED: 'badge-success',
};

const ATTENDANCE_STATUS_LABEL: Record<AttendanceStatus, string> = {
  PENDING: '대기',
  NORMAL: '정상',
  DELAY: '지연',
  EXCEPTION: '예외',
};

const RESULT_CODE_LABEL: Record<string, string> = {
  NORMAL: '정상',
  DELAY: '지연',
  EXCEPTION: '예외',
  EARLY_DEPARTURE_ELIGIBLE: '조기퇴소 허용',
  EARLY_DEPARTURE_NOT_ELIGIBLE: '조기퇴소 비대상',
};

export function TargetStatusBadge({ status }: { status: TargetStatus }) {
  return <span className={`badge ${TARGET_STATUS_CLASS[status]}`}>{TARGET_STATUS_LABEL[status]}</span>;
}

export function AttendanceStatusBadge({ status }: { status: AttendanceStatus | null }) {
  if (!status) return <span className="badge badge-neutral">-</span>;
  const cls = status === 'DELAY' ? 'badge-warning' : status === 'EXCEPTION' ? 'badge-danger' : status === 'NORMAL' ? 'badge-success' : 'badge-neutral';
  return <span className={`badge ${cls}`}>{ATTENDANCE_STATUS_LABEL[status]}</span>;
}

export function ResultCodeBadge({ code }: { code: string }) {
  const cls = code === 'DELAY' || code === 'EXCEPTION' || code === 'EARLY_DEPARTURE_NOT_ELIGIBLE'
    ? code === 'EARLY_DEPARTURE_NOT_ELIGIBLE' ? 'badge-neutral' : code === 'EXCEPTION' ? 'badge-danger' : 'badge-warning'
    : 'badge-success';
  return <span className={`badge ${cls}`}>{RESULT_CODE_LABEL[code] ?? code}</span>;
}

export const targetStatusLabel = (status: TargetStatus) => TARGET_STATUS_LABEL[status];
export const resultCodeLabel = (code: string) => RESULT_CODE_LABEL[code] ?? code;
