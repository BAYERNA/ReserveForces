import { useEffect, useState } from 'react';
import { fetchProcessLogs } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { ProcessLogResponse } from '../../api/types';
import { JudgmentTypeLabel } from '../../components/badges';

export function AdminProcessLogPage() {
  const [logs, setLogs] = useState<ProcessLogResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProcessLogs()
      .then(setLogs)
      .catch((err) => setError(err instanceof ApiError ? err.message : '처리 이력을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1>처리 이력</h1>
      <p className="page-subtitle">FR-10 판정·보정 처리 내역을 시각·처리자와 함께 기록·조회</p>

      <div className="card">
        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : logs.length === 0 ? (
          <p className="empty-state">아직 처리 이력이 없습니다. 판정 처리 화면에서 수동 보정을 수행하면 여기에 기록됩니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>대상자</th>
                  <th>판정 유형</th>
                  <th>처리자</th>
                  <th>처리 일시</th>
                  <th>처리 내용</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.logId}>
                    <td>{log.reservistName}</td>
                    <td>
                      <JudgmentTypeLabel type={log.judgmentType} />
                    </td>
                    <td>{log.processedBy}</td>
                    <td>{log.processedAt.replace('T', ' ').slice(0, 19)}</td>
                    <td>{log.content}</td>
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
