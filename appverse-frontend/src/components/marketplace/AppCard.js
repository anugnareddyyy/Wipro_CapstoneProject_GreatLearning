import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Star, Download } from 'lucide-react';

/** Map category to emoji for visual icon fallback */
const CATEGORY_EMOJI = {
  PRODUCTIVITY: '⚡', ENTERTAINMENT: '🎬', EDUCATION: '📚',
  SOCIAL: '💬', UTILITIES: '🔧', GAMING: '🎮', FINANCE: '💰',
  HEALTH_FITNESS: '💪', TRAVEL: '✈️', PHOTOGRAPHY: '📷',
  MUSIC: '🎵', NEWS: '📰', SHOPPING: '🛍️', FOOD_DRINK: '🍕',
  SPORTS: '⚽', OTHER: '📦',
};

/**
 * Reusable card component for displaying an app in marketplace grids.
 */
export default function AppCard({ app }) {
  const navigate = useNavigate();

  const emoji = CATEGORY_EMOJI[app.category] || '📦';
  const isFree = !app.price || parseFloat(app.price) === 0;

  return (
    <div className="app-card" onClick={() => navigate(`/apps/${app.appId}`)}>
      {/* Icon / Banner */}
      <div className="app-card-icon">
        {app.iconUrl
          ? <img src={app.iconUrl} alt={app.name} style={{ width: 72, height: 72, objectFit: 'cover', borderRadius: 16, position: 'relative', zIndex: 1 }} />
          : <span style={{ fontSize: '3rem', position: 'relative', zIndex: 1 }}>{emoji}</span>
        }
        {app.isFeatured && (
          <div style={{
            position: 'absolute', top: 10, right: 10, zIndex: 2,
            background: 'linear-gradient(135deg, var(--warning), #f97316)',
            borderRadius: 20, padding: '2px 8px', fontSize: '0.65rem', fontWeight: 700, color: 'white'
          }}>
            FEATURED
          </div>
        )}
      </div>

      {/* Body */}
      <div className="app-card-body">
        <div className="app-card-name">{app.name}</div>
        <div className="app-card-tagline">
          {app.tagline || app.description?.substring(0, 80) + '...'}
        </div>
        <div style={{ marginTop: 10 }}>
          <span className="badge badge-category">{app.category?.replace('_', ' ')}</span>
        </div>
      </div>

      {/* Footer */}
      <div className="app-card-footer">
        <div className="app-rating">
          <Star size={13} fill="currentColor" />
          <span>{app.averageRating > 0 ? parseFloat(app.averageRating).toFixed(1) : 'New'}</span>
          {app.reviewCount > 0 && (
            <span style={{ color: 'var(--text-dim)', fontWeight: 400 }}>({app.reviewCount})</span>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {app.downloadCount > 0 && (
            <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', display: 'flex', alignItems: 'center', gap: 3 }}>
              <Download size={11} />
              {app.downloadCount >= 1000
                ? `${(app.downloadCount / 1000).toFixed(1)}k`
                : app.downloadCount}
            </span>
          )}
          <span className={`app-price ${!isFree ? 'paid' : ''}`}>
            {isFree ? 'Free' : `$${parseFloat(app.price).toFixed(2)}`}
          </span>
        </div>
      </div>
    </div>
  );
}
