import { useEffect, useState, useCallback } from 'react';
import { fetchAllEvaluations, markException, reevaluate } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { RuleEvaluationResponse } from '../../api/types';
import { ResultCodeBadge } from '../../components/badges';
import { IconHistory } from '../../components/icons';

/** SCR-08: 판정 이력 — 적용 규칙·버전·입력값·판정 시각을 근거와 함께 보여준다 (FR-RULE-005/006). */
export function AdminEvaluationsPage() {
  const [evaluations, setEvaluations] = useState<RuleEvaluationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exceptionTarget, setExceptionTarget] = useState<RuleEvaluationResponse | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setEvaluations(await fetchAllEvaluations());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '판정 이력을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleReevaluate = async (evaluation: RuleEvaluationResponse) => {
    try {
      await reevaluate(evaluation.targetId);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '재판정 중 오류가 발생했습니다.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-icon">
          <IconHistory />
        </div>
        <h1>판정 이력</h1>
      </div>
      <p className="page-subtitle">FR-RULE-005/006 판정 근거(입력값·규칙 버전) 조회, 재판정 및 예외 처리</p>

      <div className="card">
        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : evaluations.length === 0 ? (
          <p className="empty-state">판정 이력이 없습니다. 시나리오를 먼저 실행해 주세요.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>대상자</th>
                  <th>적용 규칙</th>
                  <th>규칙 버전</th>
                  <th>입력값</th>
                  <th>판정 결과</th>
                  <th>판정 일시</th>
                  <th>처리자</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {evaluations.map((e) => (
                  <tr key={e.evaluationId}>
                    <td>{e.reservistName}</td>
                    <td>{e.ruleName}</td>
                    <td>{e.ruleVersion}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{e.inputData}</td>
                    <td>
                      <ResultCodeBadge code={e.resultCode} />
                    </td>
                    <td>{e.evaluatedAt ? e.evaluatedAt.replace('T', ' ').slice(0, 19) : '-'}</td>
                    <td>{e.evaluatedBy ?? '-'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.35rem' }}>
                        <button className="btn btn-sm btn-outline" onClick={() => handleReevaluate(e)}>
                          재판정
                        </button>
                        <button className="btn btn-sm btn-outline" onClick={() => setExceptionTarget(e)}>
                          예외 처리
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {exceptionTarget && (
        <ExceptionModal
          evaluation={exceptionTarget}
          onClose={() => setExceptionTarget(null)}
          onSaved={() => {
            setExceptionTarget(null);
            load();
          }}
        />
      )}
    </div>
  );
}

function ExceptionModal({
  evaluation,
  onClose,
  onSaved,
}: {
  evaluation: RuleEvaluationResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    if (!reason.trim()) {
      setError('예외 처리 사유를 입력해 주세요.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await markException(evaluation.targetId, reason.trim());
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
        <h3>{evaluation.reservistName} 예외 처리</h3>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          규칙 적용이 불가능하거나 추가 확인이 필요한 경우 담당자가 직접 예외로 분류합니다. (FR-RULE-004)
        </p>
        <div className="field">
          <label htmlFor="reason">예외 처리 사유</label>
          <textarea id="reason" value={reason} onChange={(e) => setReason(e.target.value)} placeholder="예: 거리 데이터 누락 - 현장 확인 필요" />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={submit} disabled={saving}>
            {saving ? '저장 중...' : '예외 처리'}
          </button>
        </div>
      </div>
    </div>
  );
}
