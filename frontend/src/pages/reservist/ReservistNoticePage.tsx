import { useEffect, useState } from 'react';
import { fetchMyNotices, fetchMyUnit } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { NoticeResponse, UnitResponse } from '../../api/types';

export function ReservistNoticePage() {
  const [notices, setNotices] = useState<NoticeResponse[]>([]);
  const [unit, setUnit] = useState<UnitResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([fetchMyNotices(), fetchMyUnit()])
      .then(([n, u]) => {
        setNotices(n);
        setUnit(u);
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : '정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1>소집통지 확인</h1>
      <p className="page-subtitle">FR-07 본인 소집통지 확인 · FR-08 입영 부대·훈련장 정보 조회</p>

      {error && <p className="error-text">{error}</p>}
      {loading ? (
        <p className="empty-state">불러오는 중...</p>
      ) : (
        <>
          <div className="card">
            <h2>소집통지</h2>
            {notices.length === 0 ? (
              <p className="empty-state">발송된 소집통지가 없습니다.</p>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>소집 예정 일시</th>
                      <th>부대</th>
                      <th>통지 상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {notices.map((notice) => (
                      <tr key={notice.noticeId}>
                        <td>{notice.scheduledDatetime.replace('T', ' ')}</td>
                        <td>{notice.unitName}</td>
                        <td>{notice.noticeStatus}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {unit && (
            <div className="card">
              <h2>입영 부대·훈련장 정보</h2>
              <div className="info-grid">
                <div className="info-item">
                  <div className="info-label">부대명</div>
                  <div className="info-value">{unit.unitName}</div>
                </div>
                <div className="info-item">
                  <div className="info-label">위치</div>
                  <div className="info-value">{unit.location}</div>
                </div>
                <div className="info-item">
                  <div className="info-label">지정 입영시각</div>
                  <div className="info-value">{unit.entryDeadlineTime.slice(0, 5)}</div>
                </div>
                <div className="info-item">
                  <div className="info-label">연락처</div>
                  <div className="info-value">{unit.contact ?? '-'}</div>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
