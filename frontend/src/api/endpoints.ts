import { api } from './client';
import type {
  AuditLogResponse,
  DashboardResponse,
  LoginResponse,
  MobilizationResponse,
  MyMobilizationResponse,
  MyStatusResponse,
  RuleEvaluationResponse,
  RuleResponse,
  ScenarioResponse,
  ScenarioRunResponse,
  TargetResponse,
  TargetStatus,
  UnitResponse,
} from './types';

export function login(loginId: string, password: string) {
  return api.post<LoginResponse>('/auth/login', { loginId, password });
}

export function fetchDashboard(mobilizationId?: string) {
  const qs = mobilizationId ? `?mobilizationId=${mobilizationId}` : '';
  return api.get<DashboardResponse>(`/admin/dashboard${qs}`);
}

export function fetchMobilizations() {
  return api.get<MobilizationResponse[]>('/admin/mobilizations');
}

export function fetchUnits() {
  return api.get<UnitResponse[]>('/admin/units');
}

export function fetchTargets(params: { mobilizationId?: string; status?: TargetStatus; query?: string } = {}) {
  const qs = new URLSearchParams();
  if (params.mobilizationId) qs.set('mobilizationId', params.mobilizationId);
  if (params.status) qs.set('status', params.status);
  if (params.query) qs.set('query', params.query);
  const s = qs.toString();
  return api.get<TargetResponse[]>(`/admin/targets${s ? `?${s}` : ''}`);
}

export function fetchTarget(targetId: string) {
  return api.get<TargetResponse>(`/admin/targets/${targetId}`);
}

export function recordAttendance(targetId: string, arrivedAt: string | null, distanceKm: number | null) {
  return api.post<TargetResponse>(`/admin/attendance/${targetId}`, { arrivedAt, distanceKm });
}

export function markAbsent(targetId: string) {
  return api.post<TargetResponse>(`/admin/targets/${targetId}/absent`);
}

export function markException(targetId: string, reason: string) {
  return api.post<TargetResponse>(`/admin/targets/${targetId}/exception`, { reason });
}

export function completeTarget(targetId: string) {
  return api.post<TargetResponse>(`/admin/targets/${targetId}/complete`);
}

export function reevaluate(targetId: string) {
  return api.post<TargetResponse>(`/admin/rules/evaluate/${targetId}`);
}

export function fetchAllEvaluations() {
  return api.get<RuleEvaluationResponse[]>('/admin/evaluations');
}

export function fetchEvaluationsForTarget(targetId: string) {
  return api.get<RuleEvaluationResponse[]>(`/admin/evaluations/${targetId}`);
}

export function fetchAuditLogs() {
  return api.get<AuditLogResponse[]>('/admin/audit-logs');
}

export function fetchScenarios() {
  return api.get<ScenarioResponse[]>('/demo/scenarios');
}

export function runScenario(code: string) {
  return api.post<ScenarioRunResponse>(`/demo/scenarios/${code}/run`);
}

export function resetDemo() {
  return api.post<void>('/demo/reset');
}

export function fetchMyMobilization() {
  return api.get<MyMobilizationResponse>('/me/mobilization');
}

export function fetchMyStatus() {
  return api.get<MyStatusResponse>('/me/status');
}

export function confirmMobilization() {
  return api.post<MyMobilizationResponse>('/me/mobilization/confirm');
}

export function fetchRules() {
  return api.get<RuleResponse[]>('/admin/rules');
}

export function updateRule(ruleId: string, value?: number, enabled?: boolean) {
  return api.patch<RuleResponse>(`/admin/rules/${ruleId}`, { value, enabled });
}
