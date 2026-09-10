import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../api/types';

export function ProtectedRoute({ role, children }: { role: Role; children: ReactNode }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (user.role !== role) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/me'} replace />;
  }
  return <>{children}</>;
}
