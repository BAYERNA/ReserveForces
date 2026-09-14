import { useEffect, useState, useCallback } from 'react';
import {
  fetchDashboard,
  fetchMobilizations,
  fetchTargets,
  recordAttendance,
  markAbsent,
  completeTarget,
  fetchUnits,
  fetchLocations,
  createMobilization,
} from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { DashboardResponse, LocationResponse, MobilizationResponse, TargetResponse, TargetStatus, UnitResponse } from '../../api/types';
import { AttendanceStatusBadge, TargetStatusBadge } from '../../components/badges';
import { IconTrendingUp, IconUsers } from '../../components/icons';
import { Gauge } from '../../components/Gauge';
import { StatusBarChart } from '../../components/StatusBarChart';

const STATUS_OPTIONS: { value: TargetStatus | ''; label: string }[] = [
  { value: '', label: '전체 상태' },
  { value: 'EXPECTED', label: '소집전' },
  { value: 'ARRIVED', label: '입영완료' },
  { value: 'DELAYED', label: '지연' },
  { value: 'ABSENT', label: '미입영' },
  { value: 'EXCEPTION', label: '예외' },
  { value: 'COMPLETED', label: '완료' },
];

function toDatetimeLocalValue(iso: string | null): string {
  if (!iso) return '';
  return iso.slice(0, 16);
}

export function AdminDashboardPage() {
  const [mobilizations, setMobilizations] = useState<MobilizationResponse[]>([]);
  const [mobilizationId, setMobilizationId] = useState('');
  const [status, setStatus] = useState<TargetStatus | ''>('');
  const [queryInput, setQueryInput] = useState('');
  const [query, setQuery] = useState('');
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [targets, setTargets] = useState<TargetResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingTarget, setEditingTarget] = useState<TargetResponse | null>(null);
  const [creatingMobilization, setCreatingMobilization] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [dashboardData, targetData] = await Promise.all([
        fetchDashboard(mobilizationId || undefined),
        fetchTargets({ mobilizationId: mobilizationId || undefined, status: status || undefined, query: query || undefined }),
      ]);
      setDashboard(dashboardData);
      setTargets(targetData);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '현황 정보를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [mobilizationId, status, query]);

  const loadMobilizations = useCallback(() => {
    fetchMobilizations().then(setMobilizations).catch(() => setMobilizations([]));
  }, []);

  useEffect(() => {
    loadMobilizations();
  }, [loadMobilizations]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    const timer = setTimeout(() => setQuery(queryInput.trim()), 300);
    return () => clearTimeout(timer);
  }, [queryInput]);

  const handleMarkAbsent = async (target: TargetResponse) => {
    try {
      await markAbsent(target.targetId);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '처리 중 오류가 발생했습니다.');
    }
  };

  const handleComplete = async (target: TargetResponse) => {
    try {
      await completeTarget(target.targetId);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '처리 중 오류가 발생했습니다.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-icon">
          <IconTrendingUp />
        </div>
        <h1>통합 관제 대시보드</h1>
      </div>
      <p className="page-subtitle">FR-DASH-001/002 전체 대상자·입영 현황 및 진행률</p>

      {dashboard && (
        <>
          <div className="hero-card" style={{ marginBottom: '1.1rem' }}>
            <div className="stat-icon">
              <IconUsers />
            </div>
            <div className="stat-body">
              <div className="stat-label">전체 소집대상</div>
              <div className="stat-value">{dashboard.totalTargets}명</div>
            </div>
          </div>

          <div className="gauge-grid">
            <div className="gauge-card">
              <Gauge value={dashboard.entryCompletionRate} valueLabel={`${dashboard.entryCompletionRate.toFixed(0)}%`} color="var(--color-accent)" />
              <div className="gauge-card-label">입영 진행률</div>
              <div className="gauge-card-caption">{dashboard.arrivedCount}명 입영완료</div>
            </div>
            <div className="gauge-card">
              <Gauge
                value={dashboard.totalTargets > 0 ? ((dashboard.statusCounts.DELAYED ?? 0) / dashboard.totalTargets) * 100 : 0}
                valueLabel={`${dashboard.statusCounts.DELAYED ?? 0}명`}
                color="var(--color-warning)"
              />
              <div className="gauge-card-label">지연</div>
              <div className="gauge-card-caption">전체 {dashboard.totalTargets}명 중</div>
            </div>
            <div className="gauge-card">
              <Gauge
                value={dashboard.totalTargets > 0 ? ((dashboard.statusCounts.ABSENT ?? 0) / dashboard.totalTargets) * 100 : 0}
                valueLabel={`${dashboard.statusCounts.ABSENT ?? 0}명`}
                color="var(--color-text-muted)"
              />
              <div className="gauge-card-label">미입영</div>
              <div className="gauge-card-caption">전체 {dashboard.totalTargets}명 중</div>
            </div>
            <div className="gauge-card">
              <Gauge
                value={dashboard.totalTargets > 0 ? ((dashboard.statusCounts.EXCEPTION ?? 0) / dashboard.totalTargets) * 100 : 0}
                valueLabel={`${dashboard.statusCounts.EXCEPTION ?? 0}명`}
                color="var(--color-danger)"
              />
              <div className="gauge-card-label">예외</div>
              <div className="gauge-card-caption">전체 {dashboard.totalTargets}명 중</div>
            </div>
          </div>

          <div className="card">
            <h2>상태별 현황</h2>
            <StatusBarChart statusCounts={dashboard.statusCounts} total={dashboard.totalTargets} />
          </div>
        </>
      )}

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.9rem' }}>
          <h2 style={{ margin: 0 }}>소집대상자 명단</h2>
          <button className="btn btn-sm" onClick={() => setCreatingMobilization(true)}>
            + 새 소집 회차 등록
          </button>
        </div>
        <div className="filter-bar">
          <input
            type="text"
            placeholder="이름 또는 식별번호 검색"
            value={queryInput}
            onChange={(e) => setQueryInput(e.target.value)}
            style={{ minWidth: '200px' }}
          />
          <select value={mobilizationId} onChange={(e) => setMobilizationId(e.target.value)}>
            <option value="">전체 소집회차</option>
            {mobilizations.map((m) => (
              <option key={m.mobilizationId} value={m.mobilizationId}>
                {m.name}
              </option>
            ))}
          </select>
          <select value={status} onChange={(e) => setStatus(e.target.value as TargetStatus | '')}>
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : targets.length === 0 ? (
          <p className="empty-state">
            조건에 맞는 소집대상자가 없습니다. 검색·필터 조건을 확인하시거나, 시연 제어에서 시나리오를 실행해 대상자를 생성해 주세요.
          </p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>성명</th>
                  <th>식별번호</th>
                  <th>소속부대</th>
                  <th>거주지 거리</th>
                  <th>상태</th>
                  <th>입영 판정</th>
                  <th>실제 입영 일시</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {targets.map((t) => (
                  <tr key={t.targetId}>
                    <td>{t.reservistName}</td>
                    <td>{t.demoIdentifier}</td>
                    <td>{t.unitName}</td>
                    <td>{t.distanceKm != null ? `${t.distanceKm}km` : '-'}</td>
                    <td>
                      <TargetStatusBadge status={t.targetStatus} />
                    </td>
                    <td>
                      <AttendanceStatusBadge status={t.attendanceStatus} />
                    </td>
                    <td>{t.arrivedAt ? t.arrivedAt.replace('T', ' ').slice(0, 16) : '-'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.35rem' }}>
                        <button className="btn btn-sm btn-outline" onClick={() => setEditingTarget(t)}>
                          입영 처리
                        </button>
                        <button className="btn btn-sm btn-outline" onClick={() => handleMarkAbsent(t)}>
                          미입영
                        </button>
                        {(t.targetStatus === 'ARRIVED' || t.targetStatus === 'DELAYED') && (
                          <button className="btn btn-sm btn-outline" onClick={() => handleComplete(t)}>
                            완료 처리
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editingTarget && (
        <EntryModal
          target={editingTarget}
          onClose={() => setEditingTarget(null)}
          onSaved={() => {
            setEditingTarget(null);
            load();
          }}
        />
      )}

      {creatingMobilization && (
        <CreateMobilizationModal
          onClose={() => setCreatingMobilization(false)}
          onCreated={(mobilization) => {
            setCreatingMobilization(false);
            loadMobilizations();
            setMobilizationId(mobilization.mobilizationId);
          }}
        />
      )}
    </div>
  );
}

function CreateMobilizationModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: (mobilization: MobilizationResponse) => void;
}) {
  const [units, setUnits] = useState<UnitResponse[]>([]);
  const [locations, setLocations] = useState<LocationResponse[]>([]);
  const [unitId, setUnitId] = useState('');
  const [locationId, setLocationId] = useState('');
  const [name, setName] = useState('');
  const [scheduledStartAt, setScheduledStartAt] = useState('');
  const [scheduledEndAt, setScheduledEndAt] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUnits().then(setUnits).catch(() => setUnits([]));
    fetchLocations().then(setLocations).catch(() => setLocations([]));
  }, []);

  const submit = async () => {
    if (!unitId || !locationId || !name.trim() || !scheduledStartAt) {
      setError('부대, 장소, 회차명, 예정 일시를 모두 입력해 주세요.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const mobilization = await createMobilization({
        unitId,
        locationId,
        name: name.trim(),
        scheduledStartAt: `${scheduledStartAt}:00Z`,
        scheduledEndAt: scheduledEndAt ? `${scheduledEndAt}:00Z` : undefined,
      });
      onCreated(mobilization);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '소집 회차 등록 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>새 소집 회차 등록</h3>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          FR-MOB-001 소집부대·장소·일정을 등록하면 대상자를 배정할 새 소집 회차가 생성됩니다.
        </p>
        <div className="field">
          <label htmlFor="mob-unit">소집부대</label>
          <select id="mob-unit" value={unitId} onChange={(e) => setUnitId(e.target.value)}>
            <option value="">선택해 주세요</option>
            {units.map((u) => (
              <option key={u.unitId} value={u.unitId}>
                {u.name}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="mob-location">소집 장소</label>
          <select id="mob-location" value={locationId} onChange={(e) => setLocationId(e.target.value)}>
            <option value="">선택해 주세요</option>
            {locations.map((l) => (
              <option key={l.locationId} value={l.locationId}>
                {l.name}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="mob-name">소집 회차명</label>
          <input id="mob-name" type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="예: 2026년 1차 정기 소집훈련" />
        </div>
        <div className="field">
          <label htmlFor="mob-start">소집 예정 일시 (UTC)</label>
          <input id="mob-start" type="datetime-local" value={scheduledStartAt} onChange={(e) => setScheduledStartAt(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="mob-end">소집 종료 일시 (선택, UTC)</label>
          <input id="mob-end" type="datetime-local" value={scheduledEndAt} onChange={(e) => setScheduledEndAt(e.target.value)} />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={submit} disabled={saving}>
            {saving ? '등록 중...' : '등록'}
          </button>
        </div>
      </div>
    </div>
  );
}

function EntryModal({
  target,
  onClose,
  onSaved,
}: {
  target: TargetResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [value, setValue] = useState(toDatetimeLocalValue(target.arrivedAt));
  const [distance, setDistance] = useState(target.distanceKm != null ? String(target.distanceKm) : '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    setSaving(true);
    setError(null);
    try {
      await recordAttendance(
        target.targetId,
        value ? `${value}:00Z` : null,
        distance ? Number(distance) : null,
      );
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '처리 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>{target.reservistName} 입영 처리</h3>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          실제 입영 일시와 거리를 입력하면 Rule Engine이 즉시 재실행되어 지연입소·조기퇴소 여부가 자동판정됩니다. (FR-RULE-001~003)
        </p>
        <div className="field">
          <label htmlFor="actual-entry">실제 입영 일시 (UTC)</label>
          <input id="actual-entry" type="datetime-local" value={value} onChange={(e) => setValue(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="distance">거주지-소집부대 거리 (km)</label>
          <input id="distance" type="number" step="0.1" value={distance} onChange={(e) => setDistance(e.target.value)} />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={submit} disabled={saving}>
            {saving ? '저장 중...' : '저장 및 자동판정'}
          </button>
        </div>
      </div>
    </div>
  );
}
