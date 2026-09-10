import { useEffect, useState } from 'react';
import { fetchAuditLogs } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { AuditLogResponse } from '../../api/types';

const ACTION_LABEL: Record<string, string> = {
  SCENARIO_RUN: '시나리오 실행',
  ATTENDANCE_RECORDED: '입영 기록',
  TARGET_MARKED_ABSENT: '미입영 처리',
  TARGET_MARKED_EXCEPTION: '예외 처리',
  TARGET_COMPLETED: '처리 완료',
  DEMO_RESET: '시연 초기화',
};

/** FR-AUD-001: 상태 변경 이력을 시간순으로 기록·조회한다. */
export function AdminAuditLogPage() {
  const [logs, setLogs] = useState<AuditLogResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAuditLogs()
      .then(setLogs)
      .catch((err) => setError(err instanceof ApiError ? err.message : '감사 로그를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1>처리 이력</h1>
      <p className="page-subtitle">FR-AUD-001 주요 상태 변경을 시각·처리자와 함께 기록·조회</p>

      <div className="card">
        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : logs.length === 0 ? (
          <p className="empty-state">아직 처리 이력이 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>처리자</th>
                  <th>행위</th>
                  <th>대상 유형</th>
                  <th>변경 전</th>
                  <th>변경 후</th>
                  <th>처리 일시</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.logId}>
                    <td>{log.actor ?? '-'}</td>
                    <td>{ACTION_LABEL[log.action] ?? log.action}</td>
                    <td>{log.targetType}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{log.beforeData ?? '-'}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{log.afterData ?? '-'}</td>
                    <td>{log.createdAt ? log.createdAt.replace('T', ' ').slice(0, 19) : '-'}</td>
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
