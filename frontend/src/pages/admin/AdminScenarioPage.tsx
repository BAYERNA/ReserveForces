import { useEffect, useState } from 'react';
import { fetchScenarios, resetDemo, runScenario } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { ScenarioResponse } from '../../api/types';

/** SCR-07: 발표용 시나리오 선택·재생·초기화 (FR-SCN-001~003). 외부 네트워크 없이 로컬 DB만으로 동작한다. */
export function AdminScenarioPage() {
  const [scenarios, setScenarios] = useState<ScenarioResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [runningCode, setRunningCode] = useState<string | null>(null);
  const [resetting, setResetting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchScenarios()
      .then(setScenarios)
      .catch((err) => setError(err instanceof ApiError ? err.message : '시나리오 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  const handleRun = async (code: string) => {
    setRunningCode(code);
    setError(null);
    setMessage(null);
    try {
      const result = await runScenario(code);
      setMessage(`"${result.mobilizationName}" 시연 데이터 ${result.targetCount}건이 생성되었습니다. 대시보드에서 확인하세요.`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '시나리오 실행 중 오류가 발생했습니다.');
    } finally {
      setRunningCode(null);
    }
  };

  const handleReset = async () => {
    setResetting(true);
    setError(null);
    setMessage(null);
    try {
      await resetDemo();
      setMessage('시연 데이터가 초기화되었습니다. 마스터 데이터(부대·장소·규칙·시나리오·예비군 풀)는 유지됩니다.');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '초기화 중 오류가 발생했습니다.');
    } finally {
      setResetting(false);
    }
  };

  return (
    <div>
      <h1>시연 제어</h1>
      <p className="page-subtitle">FR-SCN-001~003 네트워크 연결 없이 발표용 시나리오를 전환·초기화합니다.</p>

      {message && (
        <div className="card" style={{ borderColor: 'var(--color-success)', background: 'var(--color-success-bg)' }}>
          {message}
        </div>
      )}
      {error && <p className="error-text">{error}</p>}

      <div className="card">
        <h2>시나리오 선택</h2>
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>코드</th>
                  <th>이름</th>
                  <th>설명</th>
                  <th>실행</th>
                </tr>
              </thead>
              <tbody>
                {scenarios.map((s) => (
                  <tr key={s.scenarioId}>
                    <td>{s.code}</td>
                    <td>{s.name}</td>
                    <td>{s.description}</td>
                    <td>
                      <button className="btn btn-sm" onClick={() => handleRun(s.code)} disabled={runningCode !== null}>
                        {runningCode === s.code ? '실행 중...' : '실행'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card">
        <h2>초기화</h2>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          진행 중인 소집·대상·입영·판정 데이터를 모두 지우고 처음 상태로 되돌립니다. 발표 시연 종료 후 사용하세요.
        </p>
        <button className="btn btn-outline" onClick={handleReset} disabled={resetting}>
          {resetting ? '초기화 중...' : '1-Click 초기화'}
        </button>
      </div>
    </div>
  );
}
