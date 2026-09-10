export type Role = 'ADMIN' | 'RESERVIST' | 'DEMO';

export type TargetStatus = 'EXPECTED' | 'ARRIVED' | 'DELAYED' | 'ABSENT' | 'EXCEPTION' | 'COMPLETED';

export type AttendanceStatus = 'PENDING' | 'NORMAL' | 'DELAY' | 'EXCEPTION';

export interface LoginResponse {
  token: string;
  role: Role;
  displayName: string;
  reservistId: string | null;
}

export interface DashboardResponse {
  totalTargets: number;
  arrivedCount: number;
  entryCompletionRate: number;
  statusCounts: Record<TargetStatus, number>;
}

export interface TargetResponse {
  targetId: string;
  mobilizationId: string;
  mobilizationName: string;
  unitName: string;
  locationName: string;
  reservistId: string;
  reservistName: string;
  demoIdentifier: string;
  addressRegion: string | null;
  targetStatus: TargetStatus;
  scheduledAt: string | null;
  arrivedAt: string | null;
  distanceKm: number | null;
  attendanceStatus: AttendanceStatus | null;
}

export interface RuleEvaluationResponse {
  evaluationId: string;
  targetId: string;
  reservistName: string;
  ruleCode: string;
  ruleName: string;
  ruleVersion: string;
  inputData: string;
  resultCode: string;
  resultMessage: string | null;
  evaluatedAt: string | null;
  evaluatedBy: string | null;
}

export interface AuditLogResponse {
  logId: string;
  actor: string | null;
  action: string;
  targetType: string;
  targetId: string | null;
  beforeData: string | null;
  afterData: string | null;
  createdAt: string | null;
}

export interface MobilizationResponse {
  mobilizationId: string;
  name: string;
  unitName: string;
  locationName: string;
  scheduledStartAt: string;
  status: string;
}

export interface UnitResponse {
  unitId: string;
  code: string;
  name: string;
  regionCode: string | null;
}

export interface ScenarioResponse {
  scenarioId: string;
  code: string;
  name: string;
  description: string | null;
}

export interface ScenarioRunResponse {
  mobilizationId: string;
  mobilizationName: string;
  targetCount: number;
}

export interface MyMobilizationResponse {
  targetId: string;
  mobilizationName: string;
  unitName: string;
  locationName: string;
  locationAddress: string | null;
  scheduledStartAt: string;
  noticeConfirmedAt: string | null;
}

export interface MyStatusResponse {
  targetStatus: TargetStatus;
  attendanceStatus: AttendanceStatus | null;
  arrivedAt: string | null;
  evaluations: RuleEvaluationResponse[];
}
