import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminEvaluationsPage } from './pages/admin/AdminEvaluationsPage';
import { AdminAuditLogPage } from './pages/admin/AdminAuditLogPage';
import { AdminScenarioPage } from './pages/admin/AdminScenarioPage';
import { AdminRulePage } from './pages/admin/AdminRulePage';
import { ReservistNoticePage } from './pages/reservist/ReservistNoticePage';
import { ReservistJudgmentPage } from './pages/reservist/ReservistJudgmentPage';

const ADMIN_NAV = [
  { to: '/admin', label: '대시보드', end: true },
  { to: '/admin/evaluations', label: '판정 이력' },
  { to: '/admin/audit-logs', label: '처리 이력' },
  { to: '/admin/scenarios', label: '시연 제어' },
  { to: '/admin/rules', label: '규정 관리' },
];

const RESERVIST_NAV = [
  { to: '/me', label: '소집통지', end: true },
  { to: '/me/status', label: '판정 결과' },
];

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/me'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/admin"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminDashboardPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/evaluations"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminEvaluationsPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/audit-logs"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminAuditLogPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/scenarios"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminScenarioPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/rules"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminRulePage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/me"
        element={
          <ProtectedRoute role="RESERVIST">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="예비군 개인 화면" navItems={RESERVIST_NAV}>
              <ReservistNoticePage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/me/status"
        element={
          <ProtectedRoute role="RESERVIST">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="예비군 개인 화면" navItems={RESERVIST_NAV}>
              <ReservistJudgmentPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
