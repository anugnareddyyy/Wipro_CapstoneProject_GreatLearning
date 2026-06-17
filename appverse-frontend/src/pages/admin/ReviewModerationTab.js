import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
 
export default function ReviewModerationTab() {
  const [flaggedReviews, setFlaggedReviews] = useState([]);
  const [loading, setLoading] = useState(true);
 
  useEffect(() => { loadFlagged(); }, []);
 
  const loadFlagged = async () => {
    setLoading(true);
    try {
      const res = await api.get('/reviews/flagged');
      setFlaggedReviews(res.data);
    } catch {
      toast.error('Failed to load flagged reviews');
    } finally {
      setLoading(false);
    }
  };
 
  const moderate = async (reviewId, status) => {
    try {
      await api.patch(`/reviews/${reviewId}/moderate?status=${status}`);
      setFlaggedReviews(prev => prev.filter(r => r.reviewId !== reviewId));
      toast.success(status === 'APPROVED' ? 'Review approved' : 'Review removed');
    } catch {
      toast.error('Moderation action failed');
    }
  };
 
  if (loading) {
    return <div className="loading-center"><div className="spinner" /></div>;
  }
 
  if (flaggedReviews.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">✅</div>
        <h3>No flagged reviews</h3>
        <p>All caught up — nothing needs moderation right now</p>
      </div>
    );
  }
 
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      {flaggedReviews.map(review => (
        <div key={review.reviewId} className="card" style={{ padding: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, flexWrap: 'wrap', gap: 8 }}>
            <div>
              <strong>{review.userName}</strong> on <strong>{review.appName}</strong>
              <span style={{ marginLeft: 10, color: 'var(--warning)' }}>⭐ {review.rating}</span>
              {review.predictedRating && (
                <span style={{ marginLeft: 10, fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                  🤖 Predicted: {review.predictedRating}⭐
                </span>
              )}
            </div>
            <span className="badge badge-rejected">🚩 Flagged</span>
          </div>
          <p style={{ color: 'var(--text-dim)', marginBottom: 12 }}>{review.comment}</p>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-primary btn-sm" onClick={() => moderate(review.reviewId, 'APPROVED')}>
              ✓ Approve
            </button>
            <button className="btn btn-danger btn-sm" onClick={() => moderate(review.reviewId, 'REMOVED')}>
              ✕ Remove
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}