// import React, { useEffect, useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import { Plus, Edit2, Trash2, TrendingUp, BarChart2, Eye, X } from 'lucide-react';
// import { appService, recommendationService } from '../../services/api';
// import { useAuth } from '../../context/AuthContext';
// import toast from 'react-hot-toast';
// import api from '../../service/api';

// const CATEGORIES = [
//   'PRODUCTIVITY','ENTERTAINMENT','EDUCATION','SOCIAL','UTILITIES',
//   'GAMING','FINANCE','HEALTH_FITNESS','TRAVEL','PHOTOGRAPHY',
//   'MUSIC','NEWS','SHOPPING','FOOD_DRINK','SPORTS','OTHER'
// ];

// const STATUS_LABELS = {
//   PENDING: { label: 'Pending Review', cls: 'badge-pending' },
//   APPROVED: { label: 'Live', cls: 'badge-approved' },
//   REJECTED: { label: 'Rejected', cls: 'badge-rejected' },
//   SUSPENDED: { label: 'Suspended', cls: 'badge-suspended' },
// };

// function AppForm({ initial, onSave, onClose, loading }) {
//   const [form, setForm] = useState(initial || {
//     name: '', description: '', tagline: '', category: 'PRODUCTIVITY',
//     price: '0', tags: '', currentVersion: '1.0.0', releaseNotes: ''
//   });
//   const [errors, setErrors] = useState({});

//   const validate = () => {
//     const e = {};
//     if (!form.name?.trim()) e.name = 'Required';
//     if (!form.description?.trim() || form.description.length < 20) e.description = 'Min 20 characters';
//     if (!form.category) e.category = 'Required';
//     return e;
//   };

//   const handleSubmit = (e) => {
//     e.preventDefault();
//     const errs = validate();
//     if (Object.keys(errs).length) { setErrors(errs); return; }
//     onSave({ ...form, price: parseFloat(form.price) || 0 });
//   };

//   const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

//   return (
//     <div className="modal-overlay" onClick={onClose}>
//       <div className="modal" style={{ maxWidth: 600 }} onClick={(e) => e.stopPropagation()}>
//         <div className="modal-header">
//           <span className="modal-title">{initial ? 'Edit App' : 'Submit New App'}</span>
//           <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
//         </div>

//         <form onSubmit={handleSubmit}>
//           <div className="grid grid-2" style={{ gap: 14 }}>
//             <div className="form-group" style={{ marginBottom: 0 }}>
//               <label className="form-label required">App Name</label>
//               <input className="form-input" value={form.name} onChange={set('name')}
//                 placeholder="My Amazing App"
//                 style={errors.name ? { borderColor: 'var(--danger)' } : {}} />
//               {errors.name && <div className="form-error">{errors.name}</div>}
//             </div>
//             <div className="form-group" style={{ marginBottom: 0 }}>
//               <label className="form-label required">Category</label>
//               <select className="form-select" value={form.category} onChange={set('category')}>
//                 {CATEGORIES.map(c => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
//               </select>
//             </div>
//           </div>

//           <div className="form-group" style={{ marginTop: 14 }}>
//             <label className="form-label">Tagline</label>
//             <input className="form-input" value={form.tagline} onChange={set('tagline')} placeholder="One-line pitch" />
//           </div>

//           <div className="form-group">
//             <label className="form-label required">Description</label>
//             <textarea className="form-textarea" rows={4} value={form.description} onChange={set('description')}
//               placeholder="Describe your app (min 20 characters)..."
//               style={errors.description ? { borderColor: 'var(--danger)' } : {}} />
//             {errors.description && <div className="form-error">{errors.description}</div>}
//           </div>

//           <div className="grid grid-3" style={{ gap: 14 }}>
//             <div className="form-group" style={{ marginBottom: 0 }}>
//               <label className="form-label">Price (USD)</label>
//               <input type="number" min="0" step="0.01" className="form-input"
//                 value={form.price} onChange={set('price')} placeholder="0 = Free" />
//             </div>
//             <div className="form-group" style={{ marginBottom: 0 }}>
//               <label className="form-label">Version</label>
//               <input className="form-input" value={form.currentVersion} onChange={set('currentVersion')} />
//             </div>
//             <div className="form-group" style={{ marginBottom: 0 }}>
//               <label className="form-label">Tags</label>
//               <input className="form-input" value={form.tags} onChange={set('tags')} placeholder="ai, fast, tools" />
//             </div>
//           </div>

//           <div className="form-group" style={{ marginTop: 14 }}>
//             <label className="form-label">Release Notes</label>
//             <textarea className="form-textarea" rows={2} value={form.releaseNotes} onChange={set('releaseNotes')}
//               placeholder="What's new in this version?" />
//           </div>

//           <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
//             <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
//             <button type="submit" className={`btn btn-primary ${loading ? 'btn-loading' : ''}`} disabled={loading}>
//               {!loading && (initial ? 'Save Changes' : 'Submit for Review')}
//             </button>
//           </div>
//         </form>
//       </div>
//     </div>
//   );
// }

// import { BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell } from 'recharts';
 
// function AnalyticsTab({ analytics, loading }) {
//   if (loading) {
//     return <div className="loading-center"><div className="spinner" /></div>;
//   }
//   if (!analytics) {
//     return (
//       <div className="empty-state">
//         <div className="empty-state-icon">📊</div>
//         <h3>No analytics yet</h3>
//         <p>Publish apps and get downloads to see analytics</p>
//       </div>
//     );
//   }
 
//   const { downloadsAnalytics, revenueInsights, engagementReports } = analytics;
 
//   const dailyTrendData = downloadsAnalytics?.dailyTrend
//     ? Object.entries(downloadsAnalytics.dailyTrend).map(([day, count]) => ({ day, downloads: count }))
//     : [];
 
//   const pieData = [
//     { name: 'Free Apps', value: revenueInsights?.freeAppsCount || 0 },
//     { name: 'Paid Apps', value: revenueInsights?.paidAppsCount || 0 },
//   ];
//   const PIE_COLORS = ['#10b981', '#a78bfa'];
 
//   return (
//     <div>
//       {/* Summary stats */}
//       <div className="grid grid-4" style={{ marginBottom: 28 }}>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>⬇️</div>
//           <div className="stat-value">{downloadsAnalytics?.totalDownloads?.toLocaleString() || 0}</div>
//           <div className="stat-label">Total Downloads</div>
//         </div>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>💰</div>
//           <div className="stat-value">${parseFloat(revenueInsights?.totalRevenue || 0).toFixed(2)}</div>
//           <div className="stat-label">Total Revenue</div>
//         </div>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>💬</div>
//           <div className="stat-value">{engagementReports?.totalReviews || 0}</div>
//           <div className="stat-label">Total Reviews</div>
//         </div>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>⭐</div>
//           <div className="stat-value">{engagementReports?.averageRatingAcrossApps || 0}</div>
//           <div className="stat-label">Avg Rating</div>
//         </div>
//       </div>
 
//       {/* Downloads trend chart */}
//       {dailyTrendData.length > 0 && (
//         <div className="card" style={{ marginBottom: 24 }}>
//           <div className="card-header"><span className="card-title">📈 Downloads Trend (Last 7 Days)</span></div>
//           <ResponsiveContainer width="100%" height={220}>
//             <LineChart data={dailyTrendData}>
//               <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
//               <XAxis dataKey="day" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
//               <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
//               <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
//               <Line type="monotone" dataKey="downloads" stroke="var(--primary)" strokeWidth={2} dot={{ fill: 'var(--accent)' }} />
//             </LineChart>
//           </ResponsiveContainer>
//         </div>
//       )}
 
//       <div className="grid grid-2" style={{ marginBottom: 24 }}>
//         {/* Revenue per app */}
//         <div className="card">
//           <div className="card-header"><span className="card-title">💰 Revenue by App</span></div>
//           <ResponsiveContainer width="100%" height={220}>
//             <BarChart data={revenueInsights?.perApp || []}>
//               <XAxis dataKey="appName" tick={{ fill: 'var(--text-muted)', fontSize: 10 }} />
//               <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
//               <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
//               <Bar dataKey="revenue" fill="var(--accent)" radius={[4, 4, 0, 0]} />
//             </BarChart>
//           </ResponsiveContainer>
//         </div>
 
//         {/* Free vs Paid split */}
//         <div className="card">
//           <div className="card-header"><span className="card-title">🆓 Free vs Paid Apps</span></div>
//           <ResponsiveContainer width="100%" height={220}>
//             <PieChart>
//               <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70} label>
//                 {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i]} />)}
//               </Pie>
//               <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
//             </PieChart>
//           </ResponsiveContainer>
//         </div>
//       </div>
 
//       {/* Engagement table */}
//       <div className="card">
//         <div className="card-header"><span className="card-title">👥 User Engagement Report</span></div>
//         <div className="table-wrapper">
//           <table>
//             <thead>
//               <tr><th>App</th><th>Reviews</th><th>Rating</th><th>Engagement Score</th></tr>
//             </thead>
//             <tbody>
//               {(engagementReports?.perApp || []).map(row => (
//                 <tr key={row.appName}>
//                   <td style={{ fontWeight: 600 }}>{row.appName}</td>
//                   <td>{row.reviewCount}</td>
//                   <td style={{ color: 'var(--warning)' }}>⭐ {row.averageRating?.toFixed(1)}</td>
//                   <td>{row.engagementScore}</td>
//                 </tr>
//               ))}
//             </tbody>
//           </table>
//         </div>
//       </div>
//     </div>
//   );
// }
 

// export default function DeveloperConsolePage() {
//   const { currentUser } = useAuth();
//   const navigate = useNavigate();
//   const [apps, setApps] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const [showForm, setShowForm] = useState(false);
//   const [editApp, setEditApp] = useState(null);
//   const [saving, setSaving] = useState(false);
//   const [predictions, setPredictions] = useState({});
//   const [activeTab, setActiveTab] = useState('apps');
//   const [analytics, setAnalytics] = useState(null);
//   const [analyticsLoading, setAnalyticsLoading] = useState(false);

//   useEffect(() => {
//   loadApps();
// }, []);
 
// useEffect(() => {
//   if (activeTab === 'analytics' && !analytics) {
//     loadAnalytics();
//   }
// }, [activeTab]);
 
// const loadAnalytics = async () => {
//   setAnalyticsLoading(true);
//   try {
//     const res = await api.get('/analytics/developer');
//     setAnalytics(res.data);
//   } catch {
//     toast.error('Failed to load analytics');
//   } finally {
//     setAnalyticsLoading(false);
//   }
// };

//   const loadApps = async () => {
//     setLoading(true);
//     try {
//       const res = await appService.getMyApps();
//       setApps(res.data);
//     } catch { toast.error('Failed to load apps'); }
//     finally { setLoading(false); }
//   };

//   const handleSave = async (formData) => {
//     setSaving(true);
//     try {
//       if (editApp) {
//         const res = await appService.update(editApp.appId, formData);
//         setApps(prev => prev.map(a => a.appId === editApp.appId ? res.data : a));
//         toast.success('App updated!');
//       } else {
//         const res = await appService.create(formData);
//         setApps(prev => [...prev, res.data]);
//         toast.success('App submitted for review!');
//       }
//       setShowForm(false); setEditApp(null);
//     } catch (err) {
//       toast.error(err.response?.data?.message || 'Save failed');
//     } finally { setSaving(false); }
//   };

//   const handleDelete = async (appId) => {
//     if (!window.confirm('Delete this app permanently?')) return;
//     try {
//       await appService.delete(appId);
//       setApps(prev => prev.filter(a => a.appId !== appId));
//       toast.success('App deleted');
//     } catch { toast.error('Delete failed'); }
//   };

//   const loadPrediction = async (appId) => {
//     try {
//       const res = await recommendationService.predictDownloads(appId);
//       setPredictions(p => ({ ...p, [appId]: res.data }));
//     } catch { toast.error('Prediction unavailable'); }
//   };

//   const stats = {
//     total: apps.length,
//     live: apps.filter(a => a.status === 'APPROVED').length,
//     pending: apps.filter(a => a.status === 'PENDING').length,
//     totalDownloads: apps.reduce((s, a) => s + (a.downloadCount || 0), 0),
//   };

//   return (
//     <div>
//       <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
//         <div className="page-header" style={{ marginBottom: 0 }}>
//           <h1 className="page-title">Developer Console</h1>
//           <p className="page-subtitle">Manage your apps and track performance</p>
//         </div>
//         <button className="btn btn-primary" onClick={() => { setEditApp(null); setShowForm(true); }}>
//           <Plus size={16} /> New App
//         </button>
//       </div>

      

      

//       {/* Stats */}
//       <div className="grid grid-4" style={{ marginBottom: 28 }}>
//         {[
//           { label: 'Total Apps', value: stats.total, icon: '📦' },
//           { label: 'Live', value: stats.live, icon: '🟢' },
//           { label: 'Pending', value: stats.pending, icon: '⏳' },
//           { label: 'Total Downloads', value: stats.totalDownloads.toLocaleString(), icon: '⬇️' },
//         ].map(s => (
//           <div key={s.label} className="stat-card">
//             <div style={{ fontSize: '1.5rem' }}>{s.icon}</div>
//             <div className="stat-value">{s.value}</div>
//             <div className="stat-label">{s.label}</div>
//           </div>
//         ))}
//       </div>

      

//       {/* App list */}
//       {loading ? (
//         <div className="loading-center"><div className="spinner" /></div>
//       ) : apps.length === 0 ? (
//         <div className="empty-state">
//           <div className="empty-state-icon">📦</div>
//           <h3>No apps yet</h3>
//           <p>Submit your first app to the marketplace</p>
//           <button className="btn btn-primary" onClick={() => setShowForm(true)}>
//             <Plus size={15} /> Submit App
//           </button>
//         </div>
//       ) : (
//         <div className="table-wrapper">
//           <table>
//             <thead>
//               <tr>
//                 <th>App</th><th>Category</th><th>Status</th>
//                 <th>Downloads</th><th>Rating</th><th>Actions</th>
//               </tr>
//             </thead>
//             <tbody>
//               {apps.map(app => {
//                 const st = STATUS_LABELS[app.status] || {};
//                 const pred = predictions[app.appId];
//                 return (
//                   <React.Fragment key={app.appId}>
//                     <tr>
//                       <td>
//                         <div style={{ fontWeight: 600 }}>{app.name}</div>
//                         <div className="text-xs text-muted">v{app.currentVersion || '—'}</div>
//                       </td>
//                       <td><span className="badge badge-category text-xs">{app.category?.replace('_', ' ')}</span></td>
//                       <td><span className={`badge ${st.cls}`}>{st.label}</span></td>
//                       <td style={{ fontWeight: 600 }}>{(app.downloadCount || 0).toLocaleString()}</td>
//                       <td>
//                         <span style={{ color: 'var(--warning)', fontWeight: 600 }}>
//                           ⭐ {app.averageRating > 0 ? parseFloat(app.averageRating).toFixed(1) : '—'}
//                         </span>
//                       </td>
//                       <td>
//                         <div style={{ display: 'flex', gap: 6 }}>
//                           <button className="btn btn-ghost btn-sm" title="View"
//                             onClick={() => navigate(`/apps/${app.appId}`)}>
//                             <Eye size={13} />
//                           </button>
//                           <button className="btn btn-secondary btn-sm" title="Edit"
//                             onClick={() => { setEditApp(app); setShowForm(true); }}>
//                             <Edit2 size={13} />
//                           </button>
//                           <button className="btn btn-secondary btn-sm" title="AI Prediction"
//                             onClick={() => loadPrediction(app.appId)}>
//                             <TrendingUp size={13} />
//                           </button>
//                           <button className="btn btn-danger btn-sm" title="Delete"
//                             onClick={() => handleDelete(app.appId)}>
//                             <Trash2 size={13} />
//                           </button>
//                         </div>
//                       </td>
//                     </tr>
//                     {pred && (
//                       <tr>
//                         <td colSpan={6}>
//                           <div style={{
//                             padding: 12, background: 'rgba(99,102,241,0.08)',
//                             borderRadius: 'var(--radius-md)', border: '1px solid rgba(99,102,241,0.2)',
//                             display: 'flex', gap: 24, flexWrap: 'wrap', fontSize: '0.85rem'
//                           }}>
//                             <span>🧠 <strong>AI 30-Day Forecast:</strong> {pred.predicted30DayDownloads?.toLocaleString()} downloads</span>
//                             <span>📈 Growth: +{pred.growthRatePercent}%</span>
//                             <span>🎯 Confidence: {Math.round((pred.confidenceScore || 0) * 100)}%</span>
//                             <button style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-dim)', fontSize: '0.8rem' }}
//                               onClick={() => setPredictions(p => { const n = {...p}; delete n[app.appId]; return n; })}>
//                               ✕ Dismiss
//                             </button>
//                           </div>
//                         </td>
//                       </tr>
//                     )}
//                   </React.Fragment>
//                 );
//               })}
//             </tbody>
//           </table>
//         </div>
//       )}

//       {showForm && (
//         <AppForm
//           initial={editApp}
//           onSave={handleSave}
//           onClose={() => { setShowForm(false); setEditApp(null); }}
//           loading={saving}
//         />
//       )}
//     </div>
//   );
// }
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Edit2, Trash2, TrendingUp, Eye, X } from 'lucide-react';
import { appService, recommendationService } from '../../services/api';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell
} from 'recharts';
 
const CATEGORIES = [
  'PRODUCTIVITY','ENTERTAINMENT','EDUCATION','SOCIAL','UTILITIES',
  'GAMING','FINANCE','HEALTH_FITNESS','TRAVEL','PHOTOGRAPHY',
  'MUSIC','NEWS','SHOPPING','FOOD_DRINK','SPORTS','OTHER'
];
 
const STATUS_LABELS = {
  PENDING: { label: 'Pending Review', cls: 'badge-pending' },
  APPROVED: { label: 'Live', cls: 'badge-approved' },
  REJECTED: { label: 'Rejected', cls: 'badge-rejected' },
  SUSPENDED: { label: 'Suspended', cls: 'badge-suspended' },
};
 
/* ============================================================
   APP FORM MODAL — create/edit app
   ============================================================ */
function AppForm({ initial, onSave, onClose, loading }) {
  const [form, setForm] = useState(initial || {
    name: '', description: '', tagline: '', category: 'PRODUCTIVITY',
    price: '0', tags: '', currentVersion: '1.0.0', releaseNotes: ''
  });
  const [errors, setErrors] = useState({});
 
  const validate = () => {
    const e = {};
    if (!form.name?.trim()) e.name = 'Required';
    if (!form.description?.trim() || form.description.length < 20) e.description = 'Min 20 characters';
    if (!form.category) e.category = 'Required';
    return e;
  };
 
  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    onSave({ ...form, price: parseFloat(form.price) || 0 });
  };
 
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
 
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 600 }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <span className="modal-title">{initial ? 'Edit App' : 'Submit New App'}</span>
          <button className="btn btn-ghost btn-sm" onClick={onClose}><X size={16} /></button>
        </div>
 
        <form onSubmit={handleSubmit}>
          <div className="grid grid-2" style={{ gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label required">App Name</label>
              <input className="form-input" value={form.name} onChange={set('name')}
                placeholder="My Amazing App"
                style={errors.name ? { borderColor: 'var(--danger)' } : {}} />
              {errors.name && <div className="form-error">{errors.name}</div>}
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label required">Category</label>
              <select className="form-select" value={form.category} onChange={set('category')}>
                {CATEGORIES.map(c => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
              </select>
            </div>
          </div>
 
          <div className="form-group" style={{ marginTop: 14 }}>
            <label className="form-label">Tagline</label>
            <input className="form-input" value={form.tagline} onChange={set('tagline')} placeholder="One-line pitch" />
          </div>
 
          <div className="form-group">
            <label className="form-label required">Description</label>
            <textarea className="form-textarea" rows={4} value={form.description} onChange={set('description')}
              placeholder="Describe your app (min 20 characters)..."
              style={errors.description ? { borderColor: 'var(--danger)' } : {}} />
            {errors.description && <div className="form-error">{errors.description}</div>}
          </div>
 
          <div className="grid grid-3" style={{ gap: 14 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Price (USD)</label>
              <input type="number" min="0" step="0.01" className="form-input"
                value={form.price} onChange={set('price')} placeholder="0 = Free" />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Version</label>
              <input className="form-input" value={form.currentVersion} onChange={set('currentVersion')} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Tags</label>
              <input className="form-input" value={form.tags} onChange={set('tags')} placeholder="ai, fast, tools" />
            </div>
          </div>
 
          <div className="form-group" style={{ marginTop: 14 }}>
            <label className="form-label">Release Notes</label>
            <textarea className="form-textarea" rows={2} value={form.releaseNotes} onChange={set('releaseNotes')}
              placeholder="What's new in this version?" />
          </div>
 
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className={`btn btn-primary ${loading ? 'btn-loading' : ''}`} disabled={loading}>
              {!loading && (initial ? 'Save Changes' : 'Submit for Review')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
 
/* ============================================================
   ANALYTICS TAB — Module 6: Downloads, Revenue, Engagement
   ============================================================ */
function AnalyticsTab({ analytics, loading }) {
  if (loading) {
    return <div className="loading-center"><div className="spinner" /></div>;
  }
  if (!analytics) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">📊</div>
        <h3>No analytics yet</h3>
        <p>Publish apps and get downloads to see analytics</p>
      </div>
    );
  }
 
  const { downloadsAnalytics, revenueInsights, engagementReports } = analytics;
 
  const dailyTrendData = downloadsAnalytics?.dailyTrend
    ? Object.entries(downloadsAnalytics.dailyTrend).map(([day, count]) => ({ day, downloads: count }))
    : [];
 
  const pieData = [
    { name: 'Free Apps', value: revenueInsights?.freeAppsCount || 0 },
    { name: 'Paid Apps', value: revenueInsights?.paidAppsCount || 0 },
  ];
  const PIE_COLORS = ['#10b981', '#a78bfa'];
 
  return (
    <div>
      {/* Summary stats */}
      <div className="grid grid-4" style={{ marginBottom: 28 }}>
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>⬇️</div>
          <div className="stat-value">{downloadsAnalytics?.totalDownloads?.toLocaleString() || 0}</div>
          <div className="stat-label">Total Downloads</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>💰</div>
          <div className="stat-value">${parseFloat(revenueInsights?.totalRevenue || 0).toFixed(2)}</div>
          <div className="stat-label">Total Revenue</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>💬</div>
          <div className="stat-value">{engagementReports?.totalReviews || 0}</div>
          <div className="stat-label">Total Reviews</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>⭐</div>
          <div className="stat-value">{engagementReports?.averageRatingAcrossApps || 0}</div>
          <div className="stat-label">Avg Rating</div>
        </div>
      </div>
 
      {/* Downloads trend chart */}
      {dailyTrendData.length > 0 && (
        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-header"><span className="card-title">📈 Downloads Trend (Last 7 Days)</span></div>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={dailyTrendData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="day" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
              <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
              <Line type="monotone" dataKey="downloads" stroke="var(--primary)" strokeWidth={2} dot={{ fill: 'var(--accent)' }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
 
      <div className="grid grid-2" style={{ marginBottom: 24 }}>
        {/* Revenue per app */}
        <div className="card">
          <div className="card-header"><span className="card-title">💰 Revenue by App</span></div>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={revenueInsights?.perApp || []}>
              <XAxis dataKey="appName" tick={{ fill: 'var(--text-muted)', fontSize: 10 }} />
              <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
              <Bar dataKey="revenue" fill="var(--accent)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
 
        {/* Free vs Paid split */}
        <div className="card">
          <div className="card-header"><span className="card-title">🆓 Free vs Paid Apps</span></div>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70} label>
                {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i]} />)}
              </Pie>
              <Tooltip contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
 
      {/* Engagement table */}
      <div className="card">
        <div className="card-header"><span className="card-title">👥 User Engagement Report</span></div>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr><th>App</th><th>Reviews</th><th>Rating</th><th>Engagement Score</th></tr>
            </thead>
            <tbody>
              {(engagementReports?.perApp || []).map(row => (
                <tr key={row.appName}>
                  <td style={{ fontWeight: 600 }}>{row.appName}</td>
                  <td>{row.reviewCount}</td>
                  <td style={{ color: 'var(--warning)' }}>⭐ {row.averageRating?.toFixed(1)}</td>
                  <td>{row.engagementScore}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
  }
 
/* ============================================================
   MAIN PAGE — Developer Console
   ============================================================ */
export default function DeveloperConsolePage() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
 
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editApp, setEditApp] = useState(null);
  const [saving, setSaving] = useState(false);
  const [predictions, setPredictions] = useState({});
  const [activeTab, setActiveTab] = useState('apps');
 
  const [analytics, setAnalytics] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
 
  useEffect(() => {
    loadApps();
  }, []);
 
  useEffect(() => {
    if (activeTab === 'analytics' && !analytics) {
      loadAnalytics();
    }
  }, [activeTab]);
 
  const loadApps = async () => {
    setLoading(true);
    try {
      const res = await appService.getMyApps();
      setApps(res.data);
    } catch { toast.error('Failed to load apps'); }
    finally { setLoading(false); }
  };
 
  const loadAnalytics = async () => {
    setAnalyticsLoading(true);
    try {
      const res = await api.get('/analytics/developer');
      setAnalytics(res.data);
    } catch {
      toast.error('Failed to load analytics');
    } finally {
      setAnalyticsLoading(false);
    }
  };
 
  const handleSave = async (formData) => {
    setSaving(true);
    try {
      if (editApp) {
        const res = await appService.update(editApp.appId, formData);
        setApps(prev => prev.map(a => a.appId === editApp.appId ? res.data : a));
        toast.success('App updated!');
      } else {
        const res = await appService.create(formData);
        setApps(prev => [...prev, res.data]);
        toast.success('App submitted for review!');
      }
      setShowForm(false); setEditApp(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Save failed');
    } finally { setSaving(false); }
  };
 
  const handleDelete = async (appId) => {
    if (!window.confirm('Delete this app permanently?')) return;
    try {
      await appService.delete(appId);
      setApps(prev => prev.filter(a => a.appId !== appId));
      toast.success('App deleted');
    } catch { toast.error('Delete failed'); }
  };
 
  const loadPrediction = async (appId) => {
    try {
      const res = await recommendationService.predictDownloads(appId);
      setPredictions(p => ({ ...p, [appId]: res.data }));
    } catch { toast.error('Prediction unavailable'); }
  };
 
  const stats = {
    total: apps.length,
    live: apps.filter(a => a.status === 'APPROVED').length,
    pending: apps.filter(a => a.status === 'PENDING').length,
    totalDownloads: apps.reduce((s, a) => s + (a.downloadCount || 0), 0),
  };
 
  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
        <div className="page-header" style={{ marginBottom: 0 }}>
          <h1 className="page-title">Developer Console</h1>
          <p className="page-subtitle">Manage your apps and track performance</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditApp(null); setShowForm(true); }}>
          <Plus size={16} /> New App
        </button>
      </div>
 
      {/* Tabs */}
      <div className="tabs" style={{ marginBottom: 24, maxWidth: 360 }}>
        <button className={`tab-btn ${activeTab === 'apps' ? 'active' : ''}`} onClick={() => setActiveTab('apps')}>
          My Apps
        </button>
        <button className={`tab-btn ${activeTab === 'analytics' ? 'active' : ''}`} onClick={() => setActiveTab('analytics')}>
          📊 Analytics
        </button>
      </div>
 
      {activeTab === 'analytics' ? (
        <AnalyticsTab analytics={analytics} loading={analyticsLoading} />
      ) : (
        <>
          {/* Stats */}
          <div className="grid grid-4" style={{ marginBottom: 28 }}>
            {[
              { label: 'Total Apps', value: stats.total, icon: '📦' },
              { label: 'Live', value: stats.live, icon: '🟢' },
              { label: 'Pending', value: stats.pending, icon: '⏳' },
              { label: 'Total Downloads', value: stats.totalDownloads.toLocaleString(), icon: '⬇️' },
            ].map(s => (
              <div key={s.label} className="stat-card">
                <div style={{ fontSize: '1.5rem' }}>{s.icon}</div>
                <div className="stat-value">{s.value}</div>
                <div className="stat-label">{s.label}</div>
              </div>
            ))}
          </div>
 
          {/* App list */}
          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : apps.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📦</div>
              <h3>No apps yet</h3>
              <p>Submit your first app to the marketplace</p>
              <button className="btn btn-primary" onClick={() => setShowForm(true)}>
                <Plus size={15} /> Submit App
              </button>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>App</th><th>Category</th><th>Status</th>
                    <th>Downloads</th><th>Rating</th><th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {apps.map(app => {
                    const st = STATUS_LABELS[app.status] || {};
                    const pred = predictions[app.appId];
                    return (
                      <React.Fragment key={app.appId}>
                        <tr>
                          <td>
                            <div style={{ fontWeight: 600 }}>{app.name}</div>
                            <div className="text-xs text-muted">v{app.currentVersion || '—'}</div>
                          </td>
                          <td><span className="badge badge-category text-xs">{app.category?.replace('_', ' ')}</span></td>
                          <td><span className={`badge ${st.cls}`}>{st.label}</span></td>
                          <td style={{ fontWeight: 600 }}>{(app.downloadCount || 0).toLocaleString()}</td>
                          <td>
                            <span style={{ color: 'var(--warning)', fontWeight: 600 }}>
                              ⭐ {app.averageRating > 0 ? parseFloat(app.averageRating).toFixed(1) : '—'}
                            </span>
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button className="btn btn-ghost btn-sm" title="View"
                                onClick={() => navigate(`/apps/${app.appId}`)}>
                                <Eye size={13} />
                              </button>
                              <button className="btn btn-secondary btn-sm" title="Edit"
                                onClick={() => { setEditApp(app); setShowForm(true); }}>
                                <Edit2 size={13} />
                              </button>
                              <button className="btn btn-secondary btn-sm" title="AI Prediction"
                                onClick={() => loadPrediction(app.appId)}>
                                <TrendingUp size={13} />
                              </button>
                              <button className="btn btn-danger btn-sm" title="Delete"
                                onClick={() => handleDelete(app.appId)}>
                                <Trash2 size={13} />
                              </button>
                            </div>
                          </td>
                        </tr>
                        {pred && (
                          <tr>
                            <td colSpan={6}>
                              <div style={{
                                padding: 12, background: 'rgba(99,102,241,0.08)',
                                borderRadius: 'var(--radius-md)', border: '1px solid rgba(99,102,241,0.2)',
                                display: 'flex', gap: 24, flexWrap: 'wrap', fontSize: '0.85rem'
                              }}>
                                <span>🧠 <strong>AI 30-Day Forecast:</strong> {pred.predicted30DayDownloads?.toLocaleString()} downloads</span>
                                <span>📈 Growth: +{pred.growthRatePercent}%</span>
                                <span>🎯 Confidence: {Math.round((pred.confidenceScore || 0) * 100)}%</span>
                                <button style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-dim)', fontSize: '0.8rem' }}
                                  onClick={() => setPredictions(p => { const n = {...p}; delete n[app.appId]; return n; })}>
                                  ✕ Dismiss
                                </button>
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
 
      {showForm && (
        <AppForm
          initial={editApp}
          onSave={handleSave}
          onClose={() => { setShowForm(false); setEditApp(null); }}
          loading={saving}
        />
      )}
    </div>
  );
}