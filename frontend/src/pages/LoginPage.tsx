import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../api/client';

export function LoginPage() {
  const { login, logout } = useAuth();
  const navigate = useNavigate();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await login(loginId, password);
      if (user.role === 'DEMO') {
        logout();
        setError('이 계정은 오프라인 시연용 API 전용 계정입니다. 관리자(admin) 또는 예비군 계정으로 로그인해 주세요.');
        return;
      }
      navigate(user.role === 'ADMIN' ? '/admin' : '/me', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '로그인에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>예비군 동원소집 통합 관제 시스템</h1>
        <p>규정 기반 자동판정으로 여는 무중단 동원소집 관제</p>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="loginId">아이디</label>
            <input
              id="loginId"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">비밀번호</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          {error && <p className="error-text">{error}</p>}
          <button type="submit" className="btn" style={{ width: '100%' }} disabled={submitting}>
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <div className="demo-accounts">
          <strong>오프라인 시연용 데모 계정</strong>
          <br />
          관리자: admin / reserve1234!
          <br />
          예비군: reservist01, reservist02, reservist03 / reserve1234!
        </div>
      </div>
    </div>
  );
}
