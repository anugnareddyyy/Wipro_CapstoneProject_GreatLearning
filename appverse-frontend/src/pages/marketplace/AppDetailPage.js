import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Star, Download, ArrowLeft, Send, Trash2 } from 'lucide-react';
import { appService, reviewService } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import AppCard from '../../components/marketplace/AppCard';
import toast from 'react-hot-toast';
import api from '../../services/api';

const CATEGORY_EMOJI = {
  PRODUCTIVITY:'⚡',ENTERTAINMENT:'🎬',EDUCATION:'📚',SOCIAL:'💬',
  UTILITIES:'🔧',GAMING:'🎮',FINANCE:'💰',HEALTH_FITNESS:'💪',
  TRAVEL:'✈️',PHOTOGRAPHY:'📷',MUSIC:'🎵',NEWS:'📰',
  SHOPPING:'🛍️',FOOD_DRINK:'🍕',SPORTS:'⚽',OTHER:'📦',
};

function StarPicker({ value, onChange }) {
  const [hover, setHover] = useState(0);
  return (
    <div className="star-rating">
      {[1,2,3,4,5].map(n => (
        <span
          key={n}
          className={`star interactive ${n <= (hover || value) ? 'filled' : ''}`}
          onMouseEnter={() => setHover(n)}
          onMouseLeave={() => setHover(0)}
          onClick={() => onChange(n)}
        >★</span>
      ))}
    </div>
  );
}

export default function AppDetailPage() {
  const { appId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, currentUser } = useAuth();

  const [app, setApp] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [similar, setSimilar] = useState([]);
  const [sentiment, setSentiment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('reviews');

  const [reviewForm, setReviewForm] = useState({ rating: 0, title: '', reviewText: '' });
  const [submitting, setSubmitting] = useState(false);
  const [alreadyReviewed, setAlreadyReviewed] = useState(false);

  const [reviewText, setReviewText] = useState('');
  const [rating, setRating] = useState(5);
  const [predictedRating, setPredictedRating] = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [appRes, revRes, simRes, sentRes] = await Promise.all([
          appService.getById(appId),
          reviewService.getForApp(appId, { page: 0, size: 10 }),
          appService.getSimilar(appId, 4),
          reviewService.getSentiment(appId),
        ]);
        setApp(appRes.data);
        setReviews(revRes.data.content || []);
        setSimilar(simRes.data);
        setSentiment(sentRes.data);

        // Check if user already reviewed
        if (isAuthenticated && currentUser) {
          const myRevs = revRes.data.content || [];
          setAlreadyReviewed(myRevs.some(r => r.userId === currentUser.userId));
        }
      } catch { navigate('/marketplace'); }
      finally { setLoading(false); }
    };
    load();
  }, [appId, isAuthenticated, currentUser, navigate]);

  const handleDownload = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    try {
      await appService.recordDownload(appId, 'WEB', 'Unknown');
      toast.success('Download started!');
    } catch { toast.error('Download failed'); }
  };

  const handleReviewTextChange = async (text) => {
  setReviewText(text);
  if (text.length > 15) {
    try {
      const res = await api.post('/reviews/predict-rating', { text });
      setPredictedRating(res.data.predictedRating);
    } catch {
      setPredictedRating(null);
    }
  } else {
    setPredictedRating(null);
  }
};

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (reviewForm.rating === 0) { toast.error('Please select a rating'); return; }
    setSubmitting(true);
    try {
      const res = await reviewService.submit(appId, reviewForm);
      setReviews(prev => [res.data, ...prev]);
      setAlreadyReviewed(true);
      setReviewForm({ rating: 0, title: '', reviewText: '' });
      toast.success('Review submitted! AI analysis complete.');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      await reviewService.delete(reviewId);
      setReviews(prev => prev.filter(r => r.reviewId !== reviewId));
      setAlreadyReviewed(false);
      toast.success('Review deleted');
    } catch { toast.error('Failed to delete review'); }
  };

  if (loading) return (
    <div className="loading-center" style={{ minHeight: '60vh' }}>
      <div className="spinner" /><span>Loading app details...</span>
    </div>
  );

  if (!app) return null;

  const isFree = !app.price || parseFloat(app.price) === 0;
  const emoji = CATEGORY_EMOJI[app.category] || '📦';

  const totalSentiment = sentiment
    ? (sentiment.sentimentDistribution?.POSITIVE || 0) +
      (sentiment.sentimentDistribution?.NEGATIVE || 0) +
      (sentiment.sentimentDistribution?.NEUTRAL || 0)
    : 0;

  return (
    <div>
      {/* Back */}
      <button className="btn btn-ghost btn-sm" onClick={() => navigate(-1)} style={{ marginBottom: 20 }}>
        <ArrowLeft size={15} /> Back
      </button>

      {/* App header */}
      <div className="card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          {/* Icon */}
          <div style={{
            width: 120, height: 120, flexShrink: 0,
            background: 'linear-gradient(135deg, var(--bg-surface2), var(--bg-surface3))',
            borderRadius: 24, display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '3.5rem', border: '1px solid var(--border)',
          }}>
            {app.iconUrl
              ? <img src={app.iconUrl} alt={app.name} style={{ width: 90, height: 90, borderRadius: 16 }} />
              : emoji}
          </div>

          {/* Info */}
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 8 }}>
              <span className="badge badge-category">{app.category?.replace('_', ' ')}</span>
              {app.isFeatured && <span className="badge" style={{ background: 'rgba(245,158,11,0.15)', color: 'var(--warning)' }}>⭐ Featured</span>}
              <span className={`badge ${isFree ? 'badge-free' : 'badge-paid'}`}>
                {isFree ? 'Free' : `$${parseFloat(app.price).toFixed(2)}`}
              </span>
            </div>

            <h1 style={{ fontFamily: 'var(--font-display)', fontSize: '1.75rem', fontWeight: 800, marginBottom: 6 }}>
              {app.name}
            </h1>
            <p style={{ color: 'var(--text-muted)', marginBottom: 12 }}>
              by <strong style={{ color: 'var(--text)' }}>{app.developerName}</strong>
              {app.currentVersion && <span style={{ marginLeft: 12, fontSize: '0.8rem' }}>v{app.currentVersion}</span>}
            </p>

            {app.tagline && (
              <p style={{ color: 'var(--text-muted)', fontStyle: 'italic', marginBottom: 16, fontSize: '0.95rem' }}>
                "{app.tagline}"
              </p>
            )}

            {/* Stats row */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 20, marginBottom: 20 }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--warning)', fontWeight: 700, fontSize: '1.1rem' }}>
                  <Star size={16} fill="currentColor" />
                  {app.averageRating > 0 ? parseFloat(app.averageRating).toFixed(1) : '—'}
                </div>
                <div className="text-xs text-muted">{app.reviewCount} reviews</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--primary-light)' }}>
                  {app.downloadCount?.toLocaleString() || 0}
                </div>
                <div className="text-xs text-muted">Downloads</div>
              </div>
            </div>

            <button className="btn btn-primary" onClick={handleDownload}>
              <Download size={16} /> Download App
            </button>
          </div>
        </div>
      </div>

      <div className="sidebar-layout">
        {/* Main content */}
        <div className="sidebar-content">
          {/* Tabs */}
          <div className="tabs" style={{ marginBottom: 24 }}>
            {['reviews', 'description', 'sentiment'].map(tab => (
              <button key={tab} className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
                onClick={() => setActiveTab(tab)}>
                {tab === 'sentiment' ? '🧠 AI Sentiment' : tab.charAt(0).toUpperCase() + tab.slice(1)}
              </button>
            ))}
          </div>

          {/* Description tab */}
          {activeTab === 'description' && (
            <div className="card">
              <h3 style={{ fontFamily: 'var(--font-display)', marginBottom: 12 }}>About this app</h3>
              <p style={{ color: 'var(--text-muted)', lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>{app.description}</p>
              {app.tags && (
                <div style={{ marginTop: 16, display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {app.tags.split(',').map(tag => tag.trim()).filter(Boolean).map(tag => (
                    <span key={tag} className="badge badge-category">{tag}</span>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Reviews tab */}
          {activeTab === 'reviews' && (
            <div>
              {/* Write review */}
              {isAuthenticated && !alreadyReviewed && (
                <div className="card" style={{ marginBottom: 20 }}>
                  <div className="card-header">
                    <span className="card-title">Write a Review</span>
                  </div>
                  <form onSubmit={handleSubmitReview}>
                    <div className="form-group">
                      <label className="form-label required">Your Rating</label>
                      <StarPicker value={reviewForm.rating} onChange={(v) => setReviewForm({ ...reviewForm, rating: v })} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Title (optional)</label>
                      <input className="form-input" placeholder="Summarize your experience"
                        value={reviewForm.title}
                        onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })} />
                    </div>
                    
                    <div className="form-group">
                      <label className="form-label">Review</label>
                      
                      <textarea className="form-textarea" rows={4} placeholder="Share details about your experience..."
                        value={reviewForm.reviewText}
                        onChange={(e) => setReviewForm({ ...reviewForm, reviewText: e.target.value })} />
                      <div className="form-help">AI will analyze sentiment after submission</div>
                    </div>
                    <button type="submit" className={`btn btn-primary ${submitting ? 'btn-loading' : ''}`} disabled={submitting}>
                      {!submitting && <><Send size={14} /> Submit Review</>}
                    </button>
                  </form>
                </div>
              )}

              {/* Review list */}
              {reviews.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">💬</div>
                  <h3>No reviews yet</h3>
                  <p>Be the first to share your experience</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  {reviews.map(review => (
                    <div key={review.reviewId} className="card">
                      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <div className="avatar" style={{ width: 36, height: 36, fontSize: '0.75rem' }}>
                            {(review.username || 'U').substring(0, 2).toUpperCase()}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{review.username}</div>
                            <div className="star-rating">
                              {[1,2,3,4,5].map(n => (
                                <span key={n} className={`star ${n <= review.rating ? 'filled' : ''}`}>★</span>
                              ))}
                            </div>
                          </div>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <span className={`badge badge-${review.sentiment?.toLowerCase()}`}>
                            {review.sentiment}
                          </span>
                          {currentUser?.userId === review.userId && (
                            <button className="btn btn-ghost btn-sm"
                              onClick={() => handleDeleteReview(review.reviewId)}>
                              <Trash2 size={13} />
                            </button>
                          )}
                        </div>
                      </div>
                      {review.title && <div style={{ fontWeight: 600, marginBottom: 4 }}>{review.title}</div>}
                      {review.reviewText && <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', lineHeight: 1.6 }}>{review.reviewText}</p>}
                      <div className="text-xs text-muted" style={{ marginTop: 8 }}>
                        {new Date(review.createdAt).toLocaleDateString()}
                        {review.isFakeFlagged && <span style={{ color: 'var(--warning)', marginLeft: 8 }}>⚠️ Under review</span>}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* AI Sentiment tab */}
          {activeTab === 'sentiment' && sentiment && (
            <div className="card">
              <div className="card-header">
                <span className="card-title">🧠 AI Sentiment Analysis</span>
                <span className="badge badge-category">AI Powered</span>
              </div>

              <div className="grid grid-3" style={{ marginBottom: 24 }}>
                {['POSITIVE', 'NEGATIVE', 'NEUTRAL'].map(type => {
                  const count = sentiment.sentimentDistribution?.[type] || 0;
                  const pct = sentiment.sentimentPercentages?.[type] || 0;
                  return (
                    <div key={type} style={{
                      padding: 16, borderRadius: 'var(--radius-md)',
                      background: 'var(--bg-surface2)', border: '1px solid var(--border)', textAlign: 'center'
                    }}>
                      <div style={{ fontSize: '1.5rem', marginBottom: 4 }}>
                        {type === 'POSITIVE' ? '😊' : type === 'NEGATIVE' ? '😟' : '😐'}
                      </div>
                      <div style={{ fontWeight: 700, fontSize: '1.25rem' }}>{pct}%</div>
                      <div className="text-xs text-muted">{type} · {count} reviews</div>
                      <div style={{ height: 4, borderRadius: 4, marginTop: 8 }}
                        className={`sentiment-${type.toLowerCase()}`}
                        style={{
                          height: 4, borderRadius: 4, marginTop: 8,
                          background: type === 'POSITIVE' ? 'var(--success)' : type === 'NEGATIVE' ? 'var(--danger)' : 'var(--warning)',
                          width: `${pct}%`, minWidth: 4
                        }} />
                    </div>
                  );
                })}
              </div>

              <div style={{ padding: 16, background: 'rgba(99,102,241,0.08)', borderRadius: 'var(--radius-md)', border: '1px solid rgba(99,102,241,0.2)' }}>
                <div style={{ fontWeight: 600, marginBottom: 4 }}>Overall Sentiment</div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                  {sentiment.overallSentiment} based on {totalSentiment} analyzed reviews.
                  Average rating: <strong style={{ color: 'var(--warning)' }}>⭐ {parseFloat(sentiment.averageRating || 0).toFixed(1)}</strong>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Sidebar: similar apps */}
        {similar.length > 0 && (
          <div className="sidebar">
            <div className="section-title" style={{ fontSize: '0.9rem' }}>Similar Apps</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {similar.map(a => (
                <div key={a.appId} className="card card-hover"
                  style={{ padding: 12, cursor: 'pointer' }}
                  onClick={() => navigate(`/apps/${a.appId}`)}>
                  <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                    <div style={{
                      width: 40, height: 40, borderRadius: 10, flexShrink: 0,
                      background: 'var(--bg-surface2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem'
                    }}>
                      {CATEGORY_EMOJI[a.category] || '📦'}
                    </div>
                    <div style={{ minWidth: 0 }}>
                      <div style={{ fontWeight: 600, fontSize: '0.85rem', truncate: true }}>{a.name}</div>
                      <div className="text-xs text-muted">
                        ⭐ {a.averageRating > 0 ? parseFloat(a.averageRating).toFixed(1) : 'New'}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
