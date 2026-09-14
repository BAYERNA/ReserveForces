import { useEffect, useState } from 'react';
import { fetchMyStatus } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { MyStatusResponse } from '../../api/types';
import { AttendanceStatusBadge, ResultCodeBadge, TargetStatusBadge } from '../../components/badges';
import { IconCheckCircle } from '../../components/icons';

/** SCR-06: 본인 입영 처리 상태 및 판정 결과 조회 (FR-MOB-005). */
export function ReservistJudgmentPage() {
  const [status, setStatus] = useState<MyStatusResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMyStatus()
      .then(setStatus)
      .catch((err) => setError(err instanceof ApiError ? err.message : '판정 결과를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <div className="page-header">
        <div className="page-header-icon">
          <IconCheckCircle />
        </div>
        <h1>본인 판정 결과</h1>
      </div>
      <p className="page-subtitle">FR-MOB-005 본인 입영 처리 상태 및 지연입소·조기퇴소 판정 결과 확인</p>

      {error && <p className="error-text">{error}</p>}
      {loading ? (
        <p className="empty-state">불러오는 중...</p>
      ) : status ? (
        <>
          <div className="card">
            <div className="info-grid">
              <div className="info-item">
                <div className="info-label">현재 상태</div>
                <div className="info-value">
                  <TargetStatusBadge status={status.targetStatus} />
                </div>
              </div>
              <div className="info-item">
                <div className="info-label">입영 판정</div>
                <div className="info-value">
                  <AttendanceStatusBadge status={status.attendanceStatus} />
                </div>
              </div>
              <div className="info-item">
                <div className="info-label">실제 입영 일시</div>
                <div className="info-value">{status.arrivedAt ? status.arrivedAt.replace('T', ' ').slice(0, 16) : '-'}</div>
              </div>
            </div>
          </div>

          <div className="card">
            <h2>판정 근거</h2>
            {status.evaluations.length === 0 ? (
              <p className="empty-state">아직 산출된 판정 결과가 없습니다.</p>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>적용 규칙</th>
                      <th>판정 결과</th>
                      <th>판정 일시</th>
                    </tr>
                  </thead>
                  <tbody>
                    {status.evaluations.map((e) => (
                      <tr key={e.evaluationId}>
                        <td>{e.ruleName}</td>
                        <td>
                          <ResultCodeBadge code={e.resultCode} />
                        </td>
                        <td>{e.evaluatedAt ? e.evaluatedAt.replace('T', ' ').slice(0, 19) : '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      ) : (
        <p className="empty-state">조회할 정보가 없습니다.</p>
      )}
    </div>
  );
}
