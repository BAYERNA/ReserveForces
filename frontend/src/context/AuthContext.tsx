import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { getToken, setToken } from '../api/client';
import { login as loginRequest } from '../api/endpoints';
import type { Role } from '../api/types';

interface AuthUser {
  displayName: string;
  role: Role;
  reservistId: number | null;
  unitId: number | null;
}

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (loginId: string, password: string) => Promise<AuthUser>;
  logout: () => void;
}

const USER_STORAGE_KEY = 'reserveforces.user';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!raw || !getToken()) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      async login(loginId: string, password: string) {
        const response = await loginRequest(loginId, password);
        const authUser: AuthUser = {
          displayName: response.displayName,
          role: response.role,
          reservistId: response.reservistId,
          unitId: response.unitId,
        };
        setToken(response.token);
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(authUser));
        setUser(authUser);
        return authUser;
      },
      logout() {
        setToken(null);
        localStorage.removeItem(USER_STORAGE_KEY);
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.');
  }
  return ctx;
}
