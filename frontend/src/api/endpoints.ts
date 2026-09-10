import { api } from './client';
import type {
  DashboardResponse,
  JudgmentOutcome,
  JudgmentResponse,
  LoginResponse,
  NoticeResponse,
  ProcessLogResponse,
  ReservistResponse,
  ReservistStatus,
  UnitResponse,
} from './types';

export function login(loginId: string, password: string) {
  return api.post<LoginResponse>('/auth/login', { loginId, password });
}

export function fetchUnits() {
  return api.get<UnitResponse[]>('/admin/units');
}

export function fetchReservists(params: { unitId?: number; status?: ReservistStatus } = {}) {
  const query = new URLSearchParams();
  if (params.unitId != null) query.set('unitId', String(params.unitId));
  if (params.status) query.set('status', params.status);
  const qs = query.toString();
  return api.get<ReservistResponse[]>(`/admin/reservists${qs ? `?${qs}` : ''}`);
}

export function recordEntry(reservistId: number, actualEntryDatetime: string | null) {
  return api.post<ReservistResponse>(`/admin/reservists/${reservistId}/entry`, { actualEntryDatetime });
}

export function fetchDashboard(unitId?: number) {
  const qs = unitId != null ? `?unitId=${unitId}` : '';
  return api.get<DashboardResponse>(`/admin/dashboard${qs}`);
}

export function fetchJudgments() {
  return api.get<JudgmentResponse[]>('/admin/judgments');
}

export function correctJudgment(judgmentId: number, result: JudgmentOutcome, reason: string) {
  return api.put<JudgmentResponse>(`/admin/judgments/${judgmentId}/correct`, { result, reason });
}

export function fetchProcessLogs() {
  return api.get<ProcessLogResponse[]>('/admin/process-logs');
}

export function fetchMyNotices() {
  return api.get<NoticeResponse[]>('/me/notices');
}

export function fetchMyUnit() {
  return api.get<UnitResponse>('/me/unit');
}

export function fetchMyJudgments() {
  return api.get<JudgmentResponse[]>('/me/judgments');
}
