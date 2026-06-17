import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
 
const SENTIMENT_FILTERS = ['ALL', 'POSITIVE', 'NEUTRAL', 'NEGATIVE'];
 
export default function ReviewDashboardPage() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sentimentFilter, setSentimentFilter] = useState('ALL');
  const [flaggedOnly, setFlaggedOnly] = useState(false);
 
  useEffect(() => { loadReviews(); }, [sentimentFilter, flaggedOnly]);
 
  const loadReviews = async () => {
    setLoading(true);
    try {
      const params = {};
      if (sentimentFilter !== 'ALL') params.sentiment = sentimentFilter;
      if (flaggedOnly) params.flaggedOnly = true;
 
      const res = await api.get('/reviews/dashboard', { params });
      setReviews(res.data.content || res.data);
    } catch {
      toast.error('Failed to load review dashboard');
    } finally {
      setLoading(false);
    }
  };
 
  const sentimentColor = (s) => ({
    POSITIVE: 'var(--success)',
    NEGATIVE: 'var(--danger)',
    NEUTRAL: 'var(--text-dim)',
  }[s] || 'var(--text-dim)');
 
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Review Dashboard</h1>
        <p className="page-subtitle">Platform-wide review monitoring and sentiment overview</p>
      </div>
 
      <div style={{ display: 'flex', gap: 10, marginBottom: 20, flexWrap: 'wrap' }}>
        {SENTIMENT_FILTERS.map(s => (
          <button
            key={s}
            className={`btn ${sentimentFilter === s ? 'btn-primary' : 'btn-secondary'} btn-sm`}
            onClick={() => setSentimentFilter(s)}
          >
            {s}
          </button>
        ))}
        <button
          className={`btn ${flaggedOnly ? 'btn-danger' : 'btn-secondary'} btn-sm`}
          onClick={() => setFlaggedOnly(!flaggedOnly)}
          style={{ marginLeft: 'auto' }}
        >
          🚩 Flagged Only
        </button>
      </div>
 
      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : reviews.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">💬</div>
          <h3>No reviews found</h3>
          <p>Try adjusting your filters</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {reviews.map(r => (
            <div key={r.reviewId} className="card" style={{ padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, flexWrap: 'wrap', gap: 8 }}>
                <div>
                  <strong>{r.userName}</strong> on <strong>{r.appName}</strong>
                  <span style={{ marginLeft: 10, color: 'var(--warning)' }}>⭐ {r.rating}</span>
                  {r.predictedRating && (
                    <span style={{ marginLeft: 10, fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                      🤖 Predicted: {r.predictedRating}⭐
                    </span>
                  )}
                </div>
                <span style={{ color: sentimentColor(r.sentiment), fontWeight: 600, fontSize: '0.8rem' }}>
                  {r.sentiment}
                </span>
              </div>
              <p style={{ color: 'var(--text-dim)', margin: 0 }}>{r.comment}</p>
              {r.isFlagged && (
                <span className="badge badge-rejected" style={{ marginTop: 8, display: 'inline-block' }}>
                  🚩 Flagged
                </span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}