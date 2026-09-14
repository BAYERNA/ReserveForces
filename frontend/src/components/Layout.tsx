import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { IconShield } from './icons';

interface NavItem {
  to: string;
  label: string;
  end?: boolean;
}

export function Layout({
  title,
  subtitle,
  navItems,
  children,
}: {
  title: string;
  subtitle: string;
  navItems: NavItem[];
  children: ReactNode;
}) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">
            <IconShield />
          </div>
          <div className="topbar-title">
            {title}
            <small>{subtitle}</small>
          </div>
        </div>
        <nav className="nav">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className={({ isActive }) => (isActive ? 'active' : '')}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="topbar-user">
          <span>{user?.displayName}</span>
          <button className="btn-ghost" onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      </header>
      <main className="page">{children}</main>
    </div>
  );
}
