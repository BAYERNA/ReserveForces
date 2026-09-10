import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminJudgmentsPage } from './pages/admin/AdminJudgmentsPage';
import { AdminProcessLogPage } from './pages/admin/AdminProcessLogPage';
import { ReservistNoticePage } from './pages/reservist/ReservistNoticePage';
import { ReservistJudgmentPage } from './pages/reservist/ReservistJudgmentPage';

const ADMIN_NAV = [
  { to: '/admin', label: '대시보드', end: true },
  { to: '/admin/judgments', label: '판정 처리' },
  { to: '/admin/process-logs', label: '처리 이력' },
];

const RESERVIST_NAV = [
  { to: '/me', label: '소집통지', end: true },
  { to: '/me/judgments', label: '판정 결과' },
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
        path="/admin/judgments"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminJudgmentsPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/process-logs"
        element={
          <ProtectedRoute role="ADMIN">
            <Layout title="예비군 동원소집 통합 관제 시스템" subtitle="관리자 · 부대 동원 담당자" navItems={ADMIN_NAV}>
              <AdminProcessLogPage />
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
        path="/me/judgments"
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
