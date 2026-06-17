import React, { useEffect, useState } from 'react';
import { Brain, TrendingUp, Zap } from 'lucide-react';
import AppCard from '../../components/marketplace/AppCard';
import { recommendationService } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

export default function RecommendationsPage() {
  const { currentUser } = useAuth();
  const [recs, setRecs] = useState([]);
  const [trending, setTrending] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('for-you');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [recRes, trendRes] = await Promise.all([
          recommendationService.getForMe(12),
          recommendationService.getTrending(),
        ]);
        setRecs(recRes.data);
        setTrending(trendRes.data);
      } catch {} finally { setLoading(false); }
    };
    load();
  }, []);

  const trendingCategoryData = trending?.trendingByCategory
    ? Object.entries(trending.trendingByCategory).map(([k, v]) => ({ category: k.replace('_', ' '), count: v }))
    : [];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">AI Recommendations</h1>
        <p className="page-subtitle">
          Personalized for <strong>{currentUser?.username}</strong> · Powered by your activity
        </p>
      </div>

      {/* AI badge */}
      <div style={{
        display: 'inline-flex', alignItems: 'center', gap: 8, padding: '8px 16px',
        background: 'rgba(99,102,241,0.12)', borderRadius: 20, border: '1px solid rgba(99,102,241,0.25)',
        marginBottom: 24, fontSize: '0.85rem', color: 'var(--primary-light)'
      }}>
        <Brain size={15} /> AI-powered recommendation engine · Collaborative + Content-based filtering
      </div>

      <div className="tabs" style={{ marginBottom: 28, maxWidth: 360 }}>
        <button className={`tab-btn ${activeTab === 'for-you' ? 'active' : ''}`} onClick={() => setActiveTab('for-you')}>
          <Zap size={14} /> For You
        </button>
        <button className={`tab-btn ${activeTab === 'trending' ? 'active' : ''}`} onClick={() => setActiveTab('trending')}>
          <TrendingUp size={14} /> Trending Analysis
        </button>
      </div>

      {activeTab === 'for-you' && (
        loading ? (
          <div className="grid grid-apps">
            {[...Array(12)].map((_, i) => (
              <div key={i} style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden', border: '1px solid var(--border)' }}>
                <div className="skeleton" style={{ height: 140 }} />
                <div style={{ padding: 16, background: 'var(--bg-surface)' }}>
                  <div className="skeleton" style={{ height: 16, marginBottom: 8 }} />
                  <div className="skeleton" style={{ height: 12, width: '70%' }} />
                </div>
              </div>
            ))}
          </div>
        ) : recs.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🧠</div>
            <h3>Building your profile</h3>
            <p>Download a few apps and we'll start personalizing recommendations for you.</p>
          </div>
        ) : (
          <>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: 16 }}>
              {recs.length} apps recommended based on your interests
            </p>
            <div className="grid grid-apps">
              {recs.map(app => <AppCard key={app.appId} app={app} />)}
            </div>
          </>
        )
      )}

      {activeTab === 'trending' && trending && (
        <div>
          {/* Summary cards */}
          <div className="grid grid-3" style={{ marginBottom: 28 }}>
            <div className="stat-card">
              <div style={{ fontSize: '1.5rem' }}>⬇️</div>
              <div className="stat-value">{(trending.totalPlatformDownloads || 0).toLocaleString()}</div>
              <div className="stat-label">Platform Downloads</div>
            </div>
            <div className="stat-card">
              <div style={{ fontSize: '1.5rem' }}>🏆</div>
              <div className="stat-value" style={{ fontSize: '1.2rem' }}>{trending.topCategory || '—'}</div>
              <div className="stat-label">Top Category</div>
            </div>
            <div className="stat-card">
              <div style={{ fontSize: '1.5rem' }}>📱</div>
              <div className="stat-value">{trending.trendingApps?.length || 0}</div>
              <div className="stat-label">Trending Apps</div>
            </div>
          </div>

          {/* Chart */}
          {trendingCategoryData.length > 0 && (
            <div className="card" style={{ marginBottom: 28 }}>
              <div className="card-header">
                <span className="card-title">Trending by Category</span>
              </div>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={trendingCategoryData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <XAxis dataKey="category" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 11 }} />
                  <Tooltip
                    contentStyle={{ background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text)' }}
                  />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                    {trendingCategoryData.map((_, i) => (
                      <Cell key={i} fill={i % 2 === 0 ? 'var(--primary)' : 'var(--accent)'} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Trending apps grid */}
          {trending.trendingApps?.length > 0 && (
            <>
              <div className="section-title">🔥 Trending Apps</div>
              <div className="grid grid-apps">
                {trending.trendingApps.map(app => <AppCard key={app.appId} app={app} />)}
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}
