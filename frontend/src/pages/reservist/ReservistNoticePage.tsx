import { useEffect, useState } from 'react';
import { confirmMobilization, fetchMyMobilization } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { MyMobilizationResponse } from '../../api/types';

/** SCR-05/06: 본인 소집통지 및 입영 부대·훈련장 정보 조회 (FR-MOB-004). */
export function ReservistNoticePage() {
  const [mobilization, setMobilization] = useState<MyMobilizationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [confirming, setConfirming] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMyMobilization()
      .then(setMobilization)
      .catch((err) => setError(err instanceof ApiError ? err.message : '정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  const handleConfirm = async () => {
    setConfirming(true);
    setError(null);
    try {
      setMobilization(await confirmMobilization());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '확인 처리 중 오류가 발생했습니다.');
    } finally {
      setConfirming(false);
    }
  };

  return (
    <div>
      <h1>소집통지 확인</h1>
      <p className="page-subtitle">FR-MOB-004 본인 소집 일시·장소·부대 정보 조회</p>

      {error && <p className="error-text">{error}</p>}
      {loading ? (
        <p className="empty-state">불러오는 중...</p>
      ) : mobilization ? (
        <div className="card">
          <h2>{mobilization.mobilizationName}</h2>
          <div className="info-grid">
            <div className="info-item">
              <div className="info-label">소집 부대</div>
              <div className="info-value">{mobilization.unitName}</div>
            </div>
            <div className="info-item">
              <div className="info-label">입영 장소</div>
              <div className="info-value">{mobilization.locationName}</div>
            </div>
            <div className="info-item">
              <div className="info-label">주소</div>
              <div className="info-value">{mobilization.locationAddress ?? '-'}</div>
            </div>
            <div className="info-item">
              <div className="info-label">소집 예정 일시</div>
              <div className="info-value">{mobilization.scheduledStartAt.replace('T', ' ').slice(0, 16)}</div>
            </div>
          </div>
          <div style={{ marginTop: '1rem' }}>
            {mobilization.noticeConfirmedAt ? (
              <span className="badge badge-success">
                {mobilization.noticeConfirmedAt.replace('T', ' ').slice(0, 16)}에 확인 완료
              </span>
            ) : (
              <button className="btn" onClick={handleConfirm} disabled={confirming}>
                {confirming ? '처리 중...' : '소집통지 확인'}
              </button>
            )}
          </div>
        </div>
      ) : (
        <p className="empty-state">발송된 소집통지가 없습니다.</p>
      )}
    </div>
  );
}
