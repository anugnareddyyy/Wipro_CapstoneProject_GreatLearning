import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Eye, EyeOff, Code2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '', email: '', password: '', confirmPassword: '',
    fullName: '', role: 'USER'
  });
  const [errors, setErrors] = useState({});
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const e = {};
    if (!form.username) e.username = 'Username is required';
    else if (form.username.length < 3) e.username = 'Must be at least 3 characters';
    if (!form.email) e.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Must be a valid email';
    if (!form.password) e.password = 'Password is required';
    else if (form.password.length < 8) e.password = 'Must be at least 8 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    setServerError('');
    try {
      const { confirmPassword, ...payload } = form;
      await register(payload);
      navigate('/marketplace');
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors;
      if (fieldErrors) { setErrors(fieldErrors); }
      else { setServerError(err.response?.data?.message || 'Registration failed'); }
    } finally {
      setLoading(false);
    }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--bg-base)', padding: '20px'
    }}>
      <div style={{
        position: 'fixed', top: '10%', left: '50%', transform: 'translateX(-50%)',
        width: 700, height: 500,
        background: 'radial-gradient(circle, rgba(99,102,241,0.07) 0%, transparent 70%)',
        pointerEvents: 'none'
      }} />

      <div style={{ width: '100%', maxWidth: 460 }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{ fontSize: '2rem', marginBottom: 8 }}>⬡</div>
          <h1 style={{
            fontFamily: 'var(--font-display)', fontSize: '1.75rem', fontWeight: 800,
            background: 'linear-gradient(135deg, var(--primary-light), var(--accent))',
            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
          }}>
            Join AppVerse
          </h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 6, fontSize: '0.9rem' }}>
            Create your account to get started
          </p>
        </div>

        <div className="card">
          {serverError && <div className="alert alert-error">{serverError}</div>}

          {/* Role selector */}
          <div style={{
            display: 'flex', gap: 10, marginBottom: 20, padding: 4,
            background: 'var(--bg-surface2)', borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border)'
          }}>
            {['USER', 'DEVELOPER'].map(r => (
              <button
                key={r}
                type="button"
                onClick={() => setForm({ ...form, role: r })}
                style={{
                  flex: 1, padding: '8px',
                  background: form.role === r ? 'var(--bg-surface3)' : 'transparent',
                  border: form.role === r ? '1px solid var(--primary)' : '1px solid transparent',
                  borderRadius: 'var(--radius-sm)',
                  color: form.role === r ? 'var(--primary-light)' : 'var(--text-muted)',
                  cursor: 'pointer', fontSize: '0.85rem', fontWeight: 600,
                  transition: 'var(--transition)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
                }}
              >
                {r === 'USER' ? <User size={14} /> : <Code2 size={14} />}
                {r === 'USER' ? 'User' : 'Developer'}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit}>
            <div className="grid grid-2" style={{ gap: 14 }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label required">Username</label>
                <div className="input-group">
                  <User size={15} className="input-icon" />
                  <input className="form-input" placeholder="cooldev" value={form.username}
                    onChange={set('username')} style={errors.username ? { borderColor: 'var(--danger)' } : {}} />
                </div>
                {errors.username && <div className="form-error">{errors.username}</div>}
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Full Name</label>
                <div className="input-group">
                  <User size={15} className="input-icon" />
                  <input className="form-input" placeholder="Alex Smith" value={form.fullName} onChange={set('fullName')} />
                </div>
              </div>
            </div>

            <div className="form-group" style={{ marginTop: 14 }}>
              <label className="form-label required">Email</label>
              <div className="input-group">
                <Mail size={15} className="input-icon" />
                <input type="email" className="form-input" placeholder="you@example.com"
                  value={form.email} onChange={set('email')}
                  style={errors.email ? { borderColor: 'var(--danger)' } : {}} />
              </div>
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="grid grid-2" style={{ gap: 14 }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label required">Password</label>
                <div className="input-group" style={{ position: 'relative' }}>
                  <Lock size={15} className="input-icon" />
                  <input type={showPass ? 'text' : 'password'} className="form-input"
                    placeholder="Min 8 chars" value={form.password} onChange={set('password')}
                    style={{ paddingRight: 38, ...(errors.password ? { borderColor: 'var(--danger)' } : {}) }} />
                  <button type="button" onClick={() => setShowPass(!showPass)} style={{
                    position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-dim)'
                  }}>
                    {showPass ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
                {errors.password && <div className="form-error">{errors.password}</div>}
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label required">Confirm</label>
                <div className="input-group">
                  <Lock size={15} className="input-icon" />
                  <input type="password" className="form-input" placeholder="Repeat password"
                    value={form.confirmPassword} onChange={set('confirmPassword')}
                    style={errors.confirmPassword ? { borderColor: 'var(--danger)' } : {}} />
                </div>
                {errors.confirmPassword && <div className="form-error">{errors.confirmPassword}</div>}
              </div>
            </div>

            <button type="submit"
              className={`btn btn-primary btn-block btn-lg ${loading ? 'btn-loading' : ''}`}
              disabled={loading} style={{ marginTop: 20 }}>
              {!loading && 'Create Account'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: 16, color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--primary-light)', fontWeight: 600 }}>Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
