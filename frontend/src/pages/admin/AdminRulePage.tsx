import { useEffect, useState, useCallback } from 'react';
import { fetchRules, updateRule } from '../../api/endpoints';
import { ApiError } from '../../api/client';
import type { RuleResponse } from '../../api/types';
import { IconSliders } from '../../components/icons';

const RULE_TYPE_LABEL: Record<string, string> = {
  TIME: '시간 기준',
  DISTANCE: '거리 기준',
  COMPOSITE: '복합 기준',
  OTHER: '기타',
};

function parseThreshold(conditions: string): { value: number; unit: string | null } | null {
  try {
    const parsed = JSON.parse(conditions) as { value?: number; unit?: string | null };
    if (typeof parsed.value === 'number') {
      return { value: parsed.value, unit: parsed.unit ?? null };
    }
    return null;
  } catch {
    return null;
  }
}

/** SCR-09: 규정 관리 — 코드 재배포 없이 판정 임계값을 직접 조정한다 (FR-RULE-002/003, 데이터 기반 규칙 엔진). */
export function AdminRulePage() {
  const [rules, setRules] = useState<RuleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingRule, setEditingRule] = useState<RuleResponse | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setRules(await fetchRules());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '규정 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleToggle = async (rule: RuleResponse) => {
    try {
      await updateRule(rule.ruleId, undefined, !rule.enabled);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '처리 중 오류가 발생했습니다.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-icon">
          <IconSliders />
        </div>
        <h1>규정 관리</h1>
      </div>
      <p className="page-subtitle">FR-RULE-002/003 판정 임계값·버전 관리 — 값 변경 시 코드 재배포 없이 즉시 반영되며 버전이 자동 증가합니다.</p>

      <div className="card">
        {error && <p className="error-text">{error}</p>}
        {loading ? (
          <p className="empty-state">불러오는 중...</p>
        ) : rules.length === 0 ? (
          <p className="empty-state">등록된 규정이 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>규정명</th>
                  <th>유형</th>
                  <th>버전</th>
                  <th>현재 임계값</th>
                  <th>우선순위</th>
                  <th>사용 여부</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {rules.map((r) => {
                  const threshold = parseThreshold(r.conditions);
                  return (
                    <tr key={r.ruleId}>
                      <td>
                        <div>{r.name}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{r.description}</div>
                      </td>
                      <td>{RULE_TYPE_LABEL[r.ruleType] ?? r.ruleType}</td>
                      <td>{r.version}</td>
                      <td>{threshold ? `${threshold.value}${threshold.unit ?? ''}` : '-'}</td>
                      <td>{r.priority}</td>
                      <td>
                        <span className={`badge ${r.enabled ? 'badge-success' : 'badge-neutral'}`}>
                          {r.enabled ? '사용' : '미사용'}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.35rem' }}>
                          <button className="btn btn-sm btn-outline" onClick={() => setEditingRule(r)}>
                            임계값 수정
                          </button>
                          <button className="btn btn-sm btn-outline" onClick={() => handleToggle(r)}>
                            {r.enabled ? '비활성화' : '활성화'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editingRule && (
        <ThresholdModal
          rule={editingRule}
          onClose={() => setEditingRule(null)}
          onSaved={() => {
            setEditingRule(null);
            load();
          }}
        />
      )}
    </div>
  );
}

function ThresholdModal({
  rule,
  onClose,
  onSaved,
}: {
  rule: RuleResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const threshold = parseThreshold(rule.conditions);
  const [value, setValue] = useState(threshold ? String(threshold.value) : '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    if (value === '' || Number.isNaN(Number(value))) {
      setError('올바른 숫자를 입력해 주세요.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await updateRule(rule.ruleId, Number(value));
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
        <h3>{rule.name} 임계값 수정</h3>
        <p className="page-subtitle" style={{ marginBottom: '0.8rem' }}>
          현재 버전 {rule.version}. 값을 변경하면 버전이 자동으로 증가하고 이후 판정부터 새 기준이 적용됩니다.
        </p>
        <div className="field">
          <label htmlFor="threshold-value">임계값{threshold?.unit ? ` (${threshold.unit})` : ''}</label>
          <input id="threshold-value" type="number" step="0.1" value={value} onChange={(e) => setValue(e.target.value)} />
        </div>
        {error && <p className="error-text">{error}</p>}
        <div className="modal-actions">
          <button className="btn-ghost" style={{ color: 'var(--color-text-muted)', border: '1px solid var(--color-border)' }} onClick={onClose}>
            취소
          </button>
          <button className="btn" onClick={submit} disabled={saving}>
            {saving ? '저장 중...' : '저장'}
          </button>
        </div>
      </div>
    </div>
  );
}
