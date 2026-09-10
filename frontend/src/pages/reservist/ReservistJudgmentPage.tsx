import { useEffect, useState } from 'react';
import { fetchMyJudgments } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { JudgmentResponse } from '../../api/types';
import { JudgmentOutcomeBadge, JudgmentTypeLabel } from '../../components/badges';

export function ReservistJudgmentPage() {
  const [judgments, setJudgments] = useState<JudgmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMyJudgments()
      .then(setJudgments)
      .catch((err) => setError(err instanceof ApiError ? err.message : '판정 결과를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1>본인 판정 결과</h1>
      <p className="page-subtitle">FR-09 본인 지연입소·조기퇴소 판정 결과 확인</p>

      <div className="card">
        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : judgments.length === 0 ? (
          <p className="empty-state">아직 산출된 판정 결과가 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>판정 유형</th>
                  <th>판정 결과</th>
                  <th>자동/수동</th>
                  <th>사유</th>
                  <th>판정 일시</th>
                </tr>
              </thead>
              <tbody>
                {judgments.map((j) => (
                  <tr key={j.judgmentId}>
                    <td>
                      <JudgmentTypeLabel type={j.judgmentType} />
                    </td>
                    <td>
                      <JudgmentOutcomeBadge outcome={j.result} />
                    </td>
                    <td>{j.auto ? '자동' : '수동 보정'}</td>
                    <td>{j.reason ?? '-'}</td>
                    <td>{j.judgedAt.replace('T', ' ').slice(0, 19)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
