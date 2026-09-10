import { useEffect, useState, useCallback } from 'react';
import { fetchDashboard, fetchReservists, fetchUnits, recordEntry } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { DashboardResponse, ReservistResponse, ReservistStatus, UnitResponse } from '../../api/types';
import { EntryStatusBadge, ReservistStatusBadge } from '../../components/badges';

const STATUS_OPTIONS: { value: ReservistStatus | ''; label: string }[] = [
  { value: '', label: '전체 상태' },
  { value: 'BEFORE_CALLUP', label: '소집전' },
  { value: 'ENTERED', label: '입영' },
  { value: 'LATE', label: '지연' },
  { value: 'EARLY_DISCHARGE', label: '조기퇴소' },
  { value: 'COMPLETED', label: '완료' },
];

function toDatetimeLocalValue(iso: string | null): string {
  if (!iso) return '';
  return iso.slice(0, 16);
}

export function AdminDashboardPage() {
  const [units, setUnits] = useState<UnitResponse[]>([]);
  const [unitId, setUnitId] = useState<number | ''>('');
  const [status, setStatus] = useState<ReservistStatus | ''>('');
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [reservists, setReservists] = useState<ReservistResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingReservist, setEditingReservist] = useState<ReservistResponse | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [dashboardData, reservistData] = await Promise.all([
        fetchDashboard(unitId === '' ? undefined : unitId),
        fetchReservists({ unitId: unitId === '' ? undefined : unitId, status: status === '' ? undefined : status }),
      ]);
      setDashboard(dashboardData);
      setReservists(reservistData);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '현황 정보를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [unitId, status]);

  useEffect(() => {
    fetchUnits().then(setUnits).catch(() => setUnits([]));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div>
      <h1>소집 현황 대시보드</h1>
      <p className="page-subtitle">FR-02 소집대상자 현황 조회 · FR-03 입영 진행률 대시보드</p>

      {dashboard && (
        <>
          <div className="stat-grid">
            <div className="stat-card">
              <div className="stat-label">전체 소집대상자</div>
              <div className="stat-value">{dashboard.totalReservists}명</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">입영 진행률</div>
              <div className="stat-value">{dashboard.entryCompletionRate.toFixed(1)}%</div>
              <div className="progress-bar">
                <div className="progress-bar-fill" style={{ width: `${Math.min(dashboard.entryCompletionRate, 100)}%` }} />
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-label">지연입소 건수</div>
              <div className="stat-value small">{dashboard.lateEntryCount}건</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">미입영 건수</div>
              <div className="stat-value small">{dashboard.noShowCount}건</div>
            </div>
          </div>
        </>
      )}

      <div className="card">
        <h2>소집대상자 명단</h2>
        <div className="filter-bar">
          <select value={unitId} onChange={(e) => setUnitId(e.target.value === '' ? '' : Number(e.target.value))}>
            <option value="">전체 부대</option>
            {units.map((unit) => (
              <option key={unit.unitId} value={unit.unitId}>
                {unit.unitName}
              </option>
            ))}
          </select>
          <select value={status} onChange={(e) => setStatus(e.target.value as ReservistStatus | '')}>
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
        ) : reservists.length === 0 ? (
          <p className="empty-state">조건에 맞는 소집대상자가 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>성명</th>
                  <th>소속부대</th>
                  <th>거주지 거리</th>
                  <th>상태</th>
                  <th>입영 상태</th>
                  <th>실제 입영 일시</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {reservists.map((r) => (
                  <tr key={r.reservistId}>
                    <td>{r.displayName}</td>
                    <td>{r.unitName}</td>
                    <td>{r.residenceDistanceKm}km</td>
                    <td>
                      <ReservistStatusBadge status={r.status} />
                    </td>
                    <td>
                      <EntryStatusBadge status={r.entryStatus} />
                    </td>
                    <td>{r.actualEntryDatetime ? r.actualEntryDatetime.replace('T', ' ') : '-'}</td>
                    <td>
                      <button className="btn btn-sm btn-outline" onClick={() => setEditingReservist(r)}>
                        입영 처리
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editingReservist && (
        <EntryModal
          reservist={editingReservist}
          onClose={() => setEditingReservist(null)}
          onSaved={() => {
            setEditingReservist(null);
            load();
          }}
        />
      )}
    </div>
  );
}

function EntryModal({
  reservist,
  onClose,
  onSaved,
}: {
  reservist: ReservistResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [value, setValue] = useState(toDatetimeLocalValue(reservist.actualEntryDatetime));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (noShow: boolean) => {
    setSaving(true);
    setError(null);
    try {
      await recordEntry(reservist.reservistId, noShow ? null : value ? `${value}:00` : null);
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
        <h3>{reservist.displayName} 입영 처리</h3>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          실제 입영 일시를 입력하면 지정 입영시간과 비교하여 지연입소 여부가 자동판정됩니다. (FR-04)
        </p>
        <div className="field">
          <label htmlFor="actual-entry">실제 입영 일시</label>
          <input id="actual-entry" type="datetime-local" value={value} onChange={(e) => setValue(e.target.value)} />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn btn-outline" onClick={() => submit(true)} disabled={saving}>
            미입영으로 처리
          </button>
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={() => submit(false)} disabled={saving || !value}>
            {saving ? '저장 중...' : '입영 처리 저장'}
          </button>
        </div>
      </div>
    </div>
  );
}
