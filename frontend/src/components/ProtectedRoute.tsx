import { useEffect, type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../api/types';

export function ProtectedRoute({ role, children }: { role: Role; children: ReactNode }) {
  const { user, logout } = useAuth();
  const isUnsupportedRole = !!user && user.role !== 'ADMIN' && user.role !== 'RESERVIST' && user.role !== role;

  useEffect(() => {
    if (isUnsupportedRole) {
      logout();
    }
  }, [isUnsupportedRole, logout]);

  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (isUnsupportedRole) {
    return <Navigate to="/login" replace />;
  }
  if (user.role !== role) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/me'} replace />;
  }
  return <>{children}</>;
}
