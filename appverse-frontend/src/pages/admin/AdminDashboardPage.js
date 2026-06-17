// import React, { useEffect, useState } from 'react';
// import { CheckCircle, XCircle, Eye, Star, Flag } from 'lucide-react';
// import { appService, reviewService } from '../../services/api';
// import { useNavigate } from 'react-router-dom';
// import toast from 'react-hot-toast';
// import ReviewModerationTab from './ReviewModerationTab';

// export default function AdminDashboardPage() {
//   const navigate = useNavigate();
//   const [pending, setPending] = useState([]);
//   const [flagged, setFlagged] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const [activeTab, setActiveTab] = useState('pending');

//   useEffect(() => {
//     const load = async () => {
//       setLoading(true);
//       try {
//         const [pendRes, flagRes] = await Promise.all([
//           appService.getPending(),
//           reviewService.getFlagged(),
//         ]);
//         setPending(pendRes.data);
//         setFlagged(flagRes.data);
//       } catch { toast.error('Failed to load admin data'); }
//       finally { setLoading(false); }
//     };
//     load();
//   }, []);

//   const handleStatus = async (appId, status) => {
//     try {
//       await appService.updateStatus(appId, status);
//       setPending(prev => prev.filter(a => a.appId !== appId));
//       toast.success(`App ${status.toLowerCase()}!`);
//     } catch { toast.error('Action failed'); }
//   };

//   const handleToggleVisibility = async (reviewId) => {
//     try {
//       const res = await reviewService.toggleVisibility(reviewId);
//       setFlagged(prev => prev.map(r => r.reviewId === reviewId ? res.data : r));
//       toast.success('Review visibility toggled');
//     } catch { toast.error('Action failed'); }
//   };

//   return (
//     <div>
//       <div className="page-header">
//         <h1 className="page-title">Admin Dashboard</h1>
//         <p className="page-subtitle">Review pending apps and moderate flagged content</p>
//       </div>

//       {/* Stats */}
//       <div className="grid grid-3" style={{ marginBottom: 28 }}>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>⏳</div>
//           <div className="stat-value">{pending.length}</div>
//           <div className="stat-label">Pending Apps</div>
//         </div>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>🚩</div>
//           <div className="stat-value">{flagged.length}</div>
//           <div className="stat-label">Flagged Reviews</div>
//         </div>
//         <div className="stat-card">
//           <div style={{ fontSize: '1.5rem' }}>🧠</div>
//           <div className="stat-value">AI</div>
//           <div className="stat-label">Powered Moderation</div>
//         </div>
//       </div>

//       {/* Tabs */}
//       <div className="tabs" style={{ marginBottom: 24, maxWidth: 400 }}>
//         <button className={`tab-btn ${activeTab === 'pending' ? 'active' : ''}`} onClick={() => setActiveTab('pending')}>
//           Pending Apps ({pending.length})
//         </button>
//         <button className={`tab-btn ${activeTab === 'flagged' ? 'active' : ''}`} onClick={() => setActiveTab('flagged')}>
//           Flagged Reviews ({flagged.length})
//         </button>
//       </div>

//       {loading ? (
//         <div className="loading-center"><div className="spinner" /></div>
//       ) : activeTab === 'pending' ? (
//         pending.length === 0 ? (
//           <div className="empty-state">
//             <div className="empty-state-icon">✅</div>
//             <h3>All caught up!</h3>
//             <p>No apps pending review</p>
//           </div>
//         ) : (
//           <div className="table-wrapper">
//             <table>
//               <thead>
//                 <tr>
//                   <th>App</th><th>Developer</th><th>Category</th>
//                   <th>Submitted</th><th>Actions</th>
//                 </tr>
//               </thead>
//               <tbody>
//                 {pending.map(app => (
//                   <tr key={app.appId}>
//                     <td>
//                       <div style={{ fontWeight: 600 }}>{app.name}</div>
//                       <div className="text-xs text-muted">{app.tagline}</div>
//                     </td>
//                     <td>{app.developerName}</td>
//                     <td><span className="badge badge-category text-xs">{app.category?.replace('_', ' ')}</span></td>
//                     <td className="text-xs text-muted">
//                       {new Date(app.createdAt).toLocaleDateString()}
//                     </td>
//                     <td>
//                       <div style={{ display: 'flex', gap: 6 }}>
//                         <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/apps/${app.appId}`)}>
//                           <Eye size={13} /> Preview
//                         </button>
//                         <button className="btn btn-success btn-sm"
//                           onClick={() => handleStatus(app.appId, 'APPROVED')}>
//                           <CheckCircle size={13} /> Approve
//                         </button>
//                         <button className="btn btn-danger btn-sm"
//                           onClick={() => handleStatus(app.appId, 'REJECTED')}>
//                           <XCircle size={13} /> Reject
//                         </button>
//                       </div>
//                     </td>
//                   </tr>
//                 ))}
//               </tbody>
//             </table>
//           </div>
//         )
//       ) : (
//         flagged.length === 0 ? (
//           <div className="empty-state">
//             <div className="empty-state-icon">🏳️</div>
//             <h3>No flagged reviews</h3>
//             <p>AI has not flagged any suspicious reviews</p>
//           </div>
//         ) : (
//           <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
//             {flagged.map(review => (
//               <div key={review.reviewId} className="card" style={{ borderColor: 'rgba(245,158,11,0.3)' }}>
//                 <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 }}>
//                   <div style={{ flex: 1 }}>
//                     <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 6 }}>
//                       <Flag size={14} color="var(--warning)" />
//                       <span style={{ fontWeight: 600 }}>{review.username}</span>
//                       <span className="text-xs text-muted">on</span>
//                       <span style={{ fontWeight: 600, color: 'var(--primary-light)' }}>{review.appName}</span>
//                       <div className="star-rating">
//                         {[1,2,3,4,5].map(n => (
//                           <span key={n} className={`star ${n <= review.rating ? 'filled' : ''}`} style={{ fontSize: '0.8rem' }}>★</span>
//                         ))}
//                       </div>
//                       <span className={`badge badge-${review.sentiment?.toLowerCase()}`}>{review.sentiment}</span>
//                     </div>
//                     {review.reviewText && (
//                       <p className="text-sm" style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
//                         {review.reviewText}
//                       </p>
//                     )}
//                     <div className="text-xs text-muted" style={{ marginTop: 6 }}>
//                       ⚠️ AI flagged as suspicious · {new Date(review.createdAt).toLocaleDateString()}
//                     </div>
//                   </div>
//                   <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
//                     <button className="btn btn-success btn-sm"
//                       onClick={() => handleToggleVisibility(review.reviewId)}>
//                       {review.isVisible ? 'Hide' : 'Show'}
//                     </button>
//                   </div>
//                 </div>
//               </div>
//             ))}
//           </div>
//         )
//       )}
//     </div>
//   );
// }

import React, { useEffect, useState } from 'react';
import { CheckCircle, XCircle, Eye, Star, Flag } from 'lucide-react';
import { appService } from '../../services/api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import ReviewModerationTab from './ReviewModerationTab';
 
export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('pending');
 
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await appService.getPending();
        setPending(res.data);
      } catch {
        toast.error('Failed to load admin data');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);
 
  const handleStatus = async (appId, status) => {
    try {
      await appService.updateStatus(appId, status);
      setPending(prev => prev.filter(a => a.appId !== appId));
      toast.success(`App ${status.toLowerCase()}!`);
    } catch {
      toast.error('Action failed');
    }
  };
 
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="page-subtitle">Review pending apps and moderate flagged content</p>
      </div>
 
      {/* Stats */}
      <div className="grid grid-3" style={{ marginBottom: 28 }}>
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>⏳</div>
          <div className="stat-value">{pending.length}</div>
          <div className="stat-label">Pending Apps</div>
        </div>
 
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>🚩</div>
          <div className="stat-value">—</div>
          <div className="stat-label">Flagged Reviews</div>
        </div>
 
        <div className="stat-card">
          <div style={{ fontSize: '1.5rem' }}>🧠</div>
          <div className="stat-value">AI</div>
          <div className="stat-label">Powered Moderation</div>
        </div>
      </div>
 
      {/* Tabs */}
      <div className="tabs" style={{ marginBottom: 24, maxWidth: 400 }}>
        <button
          className={`tab-btn ${activeTab === 'pending' ? 'active' : ''}`}
          onClick={() => setActiveTab('pending')}
        >
          Pending Apps ({pending.length})
        </button>
        <button
          className={`tab-btn ${activeTab === 'flagged' ? 'active' : ''}`}
          onClick={() => setActiveTab('flagged')}
        >
          Flagged Reviews
        </button>
      </div>
 
      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : activeTab === 'pending' ? (
        pending.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">✅</div>
            <h3>All caught up!</h3>
            <p>No apps pending review</p>
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>App</th><th>Developer</th><th>Category</th>
                  <th>Submitted</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {pending.map(app => (
                  <tr key={app.appId}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{app.name}</div>
                      <div className="text-xs text-muted">{app.tagline}</div>
                    </td>
                    <td>{app.developerName}</td>
                    <td><span className="badge badge-category text-xs">{app.category?.replace('_', ' ')}</span></td>
                    <td className="text-xs text-muted">
                      {new Date(app.createdAt).toLocaleDateString()}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/apps/${app.appId}`)}>
                          <Eye size={13} /> Preview
                        </button>
                        <button className="btn btn-success btn-sm"
                          onClick={() => handleStatus(app.appId, 'APPROVED')}>
                          <CheckCircle size={13} /> Approve
                        </button>
                        <button className="btn btn-danger btn-sm"
                          onClick={() => handleStatus(app.appId, 'REJECTED')}>
                          <XCircle size={13} /> Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      ) : (
        <ReviewModerationTab />
      )}
    </div>
  );
}