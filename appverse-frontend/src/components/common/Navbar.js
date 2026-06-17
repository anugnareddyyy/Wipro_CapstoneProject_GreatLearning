import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, Package, LayoutDashboard, LogOut, User, ChevronDown, Zap, Shield } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

/**
 * Persistent navigation bar with search, role-based nav links, and user menu.
 */
export default function Navbar() {
  const { currentUser, isAuthenticated, isAdmin, isDeveloper, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchQuery, setSearchQuery] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/marketplace?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const isActive = (path) => location.pathname.startsWith(path);

  const initials = currentUser
    ? (currentUser.fullName || currentUser.username).substring(0, 2).toUpperCase()
    : 'AV';

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          ⬡ AppVerse AI
        </Link>

        {/* Nav links */}
        <div className="navbar-nav">
          <Link
            to="/marketplace"
            className={`nav-link ${isActive('/marketplace') ? 'active' : ''}`}
          >
            Marketplace
          </Link>
          {isAuthenticated && (
            <Link
              to="/recommendations"
              className={`nav-link ${isActive('/recommendations') ? 'active' : ''}`}
            >
              For You
            </Link>
          )}
          {isDeveloper && (
            <Link
              to="/developer"
              className={`nav-link ${isActive('/developer') ? 'active' : ''}`}
            >
              Console
            </Link>
          )}
          {isAdmin && (
            <Link
              to="/admin"
              className={`nav-link ${isActive('/admin') ? 'active' : ''}`}
            >
              Admin
            </Link>
          )}

          {isAdmin && (
            <Link
              to="/admin/reviews"
              className="nav-link">Reviews</Link> 
          )}
              

        </div>

        {/* Search */}
        <div className="navbar-search">
          <form onSubmit={handleSearch}>
            <div className="search-bar">
              <Search size={15} className="search-icon" />
              <input
                type="text"
                placeholder="Search apps..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </form>
        </div>

        {/* Actions */}
        <div className="navbar-actions">
          {isAuthenticated ? (
            <div className="dropdown" ref={menuRef}>
              <div className="user-menu" onClick={() => setMenuOpen(!menuOpen)}>
                <div className="avatar">{initials}</div>
                <span className="text-sm" style={{ maxWidth: 90, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {currentUser.username}
                </span>
                <ChevronDown size={14} color="var(--text-muted)" />
              </div>

              {menuOpen && (
                <div className="dropdown-menu">
                  <div style={{ padding: '8px 12px 6px', borderBottom: '1px solid var(--border)', marginBottom: 4 }}>
                    <div className="text-xs text-muted">{currentUser.email}</div>
                    <div className="text-xs" style={{ color: 'var(--accent)', marginTop: 2, textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                      {currentUser.role}
                    </div>
                  </div>

                  <button className="dropdown-item" onClick={() => { navigate('/profile'); setMenuOpen(false); }}>
                    <User size={14} /> My Profile
                  </button>

                  {isDeveloper && (
                    <button className="dropdown-item" onClick={() => { navigate('/developer'); setMenuOpen(false); }}>
                      <Package size={14} /> Developer Console
                    </button>
                  )}

                  {isAdmin && (
                    <button className="dropdown-item" onClick={() => { navigate('/admin'); setMenuOpen(false); }}>
                      <Shield size={14} /> Admin Dashboard
                    </button>
                  )}

                  <div className="dropdown-divider" />
                  <button className="dropdown-item danger" onClick={() => { logout(); setMenuOpen(false); }}>
                    <LogOut size={14} /> Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/login">
                <button className="btn btn-ghost btn-sm">Sign In</button>
              </Link>
              <Link to="/register">
                <button className="btn btn-primary btn-sm">Get Started</button>
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
