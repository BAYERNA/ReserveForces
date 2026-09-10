import { useEffect, useState, useCallback } from 'react';
import { correctJudgment, fetchJudgments } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { JudgmentOutcome, JudgmentResponse, JudgmentType } from '../../api/types';
import { JudgmentOutcomeBadge, JudgmentTypeLabel } from '../../components/badges';

const TYPE_OPTIONS: { value: JudgmentType | ''; label: string }[] = [
  { value: '', label: '전체 유형' },
  { value: 'LATE_ENTRY', label: '지연입소' },
  { value: 'EARLY_DISCHARGE', label: '조기퇴소' },
];

export function AdminJudgmentsPage() {
  const [judgments, setJudgments] = useState<JudgmentResponse[]>([]);
  const [typeFilter, setTypeFilter] = useState<JudgmentType | ''>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<JudgmentResponse | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setJudgments(await fetchJudgments());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '판정 결과를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const filtered = typeFilter ? judgments.filter((j) => j.judgmentType === typeFilter) : judgments;

  return (
    <div>
      <h1>판정 처리</h1>
      <p className="page-subtitle">FR-04/FR-05 지연입소·조기퇴소 자동판정 결과 확인 · FR-06 수동 보정</p>

      <div className="card">
        <div className="filter-bar">
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value as JudgmentType | '')}>
            {TYPE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : filtered.length === 0 ? (
          <p className="empty-state">판정 결과가 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>대상자</th>
                  <th>판정 유형</th>
                  <th>판정 결과</th>
                  <th>자동/수동</th>
                  <th>사유</th>
                  <th>판정 일시</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((j) => (
                  <tr key={j.judgmentId}>
                    <td>{j.reservistName}</td>
                    <td>
                      <JudgmentTypeLabel type={j.judgmentType} />
                    </td>
                    <td>
                      <JudgmentOutcomeBadge outcome={j.result} />
                    </td>
                    <td>{j.auto ? '자동' : '수동 보정'}</td>
                    <td>{j.reason ?? '-'}</td>
                    <td>{j.judgedAt.replace('T', ' ').slice(0, 19)}</td>
                    <td>
                      <button className="btn btn-sm btn-outline" onClick={() => setEditing(j)}>
                        수동 보정
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && (
        <CorrectionModal
          judgment={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </div>
  );
}

function CorrectionModal({
  judgment,
  onClose,
  onSaved,
}: {
  judgment: JudgmentResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [result, setResult] = useState<JudgmentOutcome>(judgment.result);
  const [reason, setReason] = useState(judgment.reason ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    if (!reason.trim()) {
      setError('보정 사유를 입력해 주세요.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await correctJudgment(judgment.judgmentId, result, reason.trim());
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '보정 처리 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>
          {judgment.reservistName} · <JudgmentTypeLabel type={judgment.judgmentType} /> 수동 보정
        </h3>
        <div className="field">
          <label htmlFor="result">보정 결과</label>
          <select id="result" value={result} onChange={(e) => setResult(e.target.value as JudgmentOutcome)}>
            <option value="ALLOWED">허용</option>
            <option value="DENIED">불허</option>
          </select>
        </div>
        <div className="field">
          <label htmlFor="reason">보정 사유</label>
          <textarea id="reason" value={reason} onChange={(e) => setReason(e.target.value)} placeholder="예: 교통사고 등 불가피한 사유 확인" />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={submit} disabled={saving}>
            {saving ? '저장 중...' : '보정 저장'}
          </button>
        </div>
      </div>
    </div>
  );
}
