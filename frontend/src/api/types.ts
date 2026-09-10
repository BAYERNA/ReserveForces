export type Role = 'ADMIN' | 'RESERVIST';

export type ReservistStatus = 'BEFORE_CALLUP' | 'ENTERED' | 'LATE' | 'EARLY_DISCHARGE' | 'COMPLETED';

export type EntryStatus = 'WAITING' | 'COMPLETED' | 'LATE' | 'NO_SHOW';

export type JudgmentType = 'LATE_ENTRY' | 'EARLY_DISCHARGE';

export type JudgmentOutcome = 'ALLOWED' | 'DENIED';

export interface LoginResponse {
  token: string;
  role: Role;
  displayName: string;
  reservistId: number | null;
  unitId: number | null;
}

export interface UnitResponse {
  unitId: number;
  unitName: string;
  location: string;
  entryDeadlineTime: string;
  contact: string | null;
}

export interface ReservistResponse {
  reservistId: number;
  displayName: string;
  residenceDistanceKm: number;
  unitId: number;
  unitName: string;
  status: ReservistStatus;
  actualEntryDatetime: string | null;
  entryStatus: EntryStatus | null;
}

export interface DashboardResponse {
  totalReservists: number;
  enteredCount: number;
  entryCompletionRate: number;
  statusCounts: Record<ReservistStatus, number>;
  lateEntryCount: number;
  noShowCount: number;
}

export interface JudgmentResponse {
  judgmentId: number;
  reservistId: number;
  reservistName: string;
  judgmentType: JudgmentType;
  auto: boolean;
  result: JudgmentOutcome;
  reason: string | null;
  judgedAt: string;
}

export interface ProcessLogResponse {
  logId: number;
  judgmentId: number;
  reservistName: string;
  judgmentType: JudgmentType;
  processedBy: string;
  processedAt: string;
  content: string;
}

export interface NoticeResponse {
  noticeId: number;
  unitId: number;
  unitName: string;
  location: string;
  contact: string | null;
  scheduledDatetime: string;
  noticeStatus: string;
}
