import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

/**
 * Login page with email/password form and validation.
 * Redirects to previous page after successful login.
 */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/marketplace';

  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const e = {};
    if (!form.email) e.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Must be a valid email';
    if (!form.password) e.password = 'Password is required';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    setServerError('');
    try {
      await login(form);
      navigate(from, { replace: true });
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed. Check your credentials.';
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--bg-base)', padding: '20px'
    }}>
      {/* Background glow */}
      <div style={{
        position: 'fixed', top: '20%', left: '50%', transform: 'translateX(-50%)',
        width: 600, height: 400, background: 'radial-gradient(circle, rgba(99,102,241,0.08) 0%, transparent 70%)',
        pointerEvents: 'none'
      }} />

      <div style={{ width: '100%', maxWidth: 420 }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ fontSize: '2rem', marginBottom: 8 }}>⬡</div>
          <h1 style={{
            fontFamily: 'var(--font-display)', fontSize: '1.75rem', fontWeight: 800,
            background: 'linear-gradient(135deg, var(--primary-light), var(--accent))',
            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
          }}>
            AppVerse AI
          </h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 6, fontSize: '0.9rem' }}>
            Sign in to your account
          </p>
        </div>

        <div className="card">
          {serverError && (
            <div className="alert alert-error" style={{ marginBottom: 20 }}>
              {serverError}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label required">Email address</label>
              <div className="input-group">
                <Mail size={16} className="input-icon" />
                <input
                  type="email"
                  className={`form-input ${errors.email ? 'border-danger' : ''}`}
                  placeholder="you@example.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  style={errors.email ? { borderColor: 'var(--danger)' } : {}}
                />
              </div>
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label className="form-label required">Password</label>
              <div className="input-group" style={{ position: 'relative' }}>
                <Lock size={16} className="input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  className={`form-input ${errors.password ? 'border-danger' : ''}`}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  style={{ paddingRight: 42, ...(errors.password ? { borderColor: 'var(--danger)' } : {}) }}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-dim)',
                    display: 'flex', alignItems: 'center',
                  }}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <div className="form-error">{errors.password}</div>}
            </div>

            <button
              type="submit"
              className={`btn btn-primary btn-block btn-lg ${loading ? 'btn-loading' : ''}`}
              disabled={loading}
              style={{ marginTop: 8 }}
            >
              {!loading && 'Sign In'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: 20, color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: 'var(--primary-light)', fontWeight: 600 }}>
              Create one
            </Link>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginTop: 16 }}>
          <Link to="/marketplace" style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
            Continue browsing without signing in →
          </Link>
        </div>
      </div>
    </div>
  );
}
