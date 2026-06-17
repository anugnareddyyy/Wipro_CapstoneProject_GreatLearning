import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Zap, TrendingUp, Star, Shield, Brain, BarChart2 } from 'lucide-react';
import AppCard from '../components/marketplace/AppCard';
import { appService } from '../services/api';

const FEATURES = [
  { icon: Brain, title: 'AI Recommendations', desc: 'Personalized picks powered by your download history and smart category matching.' },
  { icon: Star, title: 'Sentiment Analysis', desc: 'Every review is analyzed by AI to detect genuine feedback and surface the best apps.' },
  { icon: TrendingUp, title: 'Download Predictions', desc: 'Developers get 30-day download forecasts powered by our prediction engine.' },
  { icon: Shield, title: 'Fake Review Detection', desc: 'Our AI flags suspicious reviews to keep ratings trustworthy and meaningful.' },
  { icon: BarChart2, title: 'Analytics Dashboard', desc: 'Real-time insights on downloads, revenue, and user engagement per app.' },
  { icon: Zap, title: 'Developer Console', desc: 'One-click app publishing, version control, and release management tools.' },
];

export default function HomePage() {
  const navigate = useNavigate();
  const [featured, setFeatured] = useState([]);
  const [trending, setTrending] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [featRes, trendRes] = await Promise.all([
          appService.getFeatured(),
          appService.getTrending(8),
        ]);
        setFeatured(featRes.data.slice(0, 4));
        setTrending(trendRes.data.slice(0, 8));
      } catch {
        // Silently fail — show empty state
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <div>
      {/* HERO */}
      <div className="hero">
        <h1 className="hero-title">
          Discover Apps<br />Powered by AI
        </h1>
        <p className="hero-subtitle">
          AppVerse uses artificial intelligence to recommend the perfect apps,
          analyze reviews, and help developers grow their audience.
        </p>
        <div className="hero-actions">
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/marketplace')}>
            <Zap size={18} /> Explore Marketplace
          </button>
          <button className="btn btn-secondary btn-lg" onClick={() => navigate('/register')}>
            Join as Developer
          </button>
        </div>
      </div>

      {/* FEATURED APPS */}
      {(loading || featured.length > 0) && (
        <section style={{ marginBottom: 48 }}>
          <div className="section-title">
            ⭐ Featured Apps
          </div>
          {loading ? (
            <div className="grid grid-apps">
              {[...Array(4)].map((_, i) => (
                <div key={i} style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden' }}>
                  <div className="skeleton" style={{ height: 140 }} />
                  <div style={{ padding: 16, background: 'var(--bg-surface)', borderTop: '1px solid var(--border)' }}>
                    <div className="skeleton" style={{ height: 16, marginBottom: 8, borderRadius: 4 }} />
                    <div className="skeleton" style={{ height: 12, width: '70%', borderRadius: 4 }} />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-apps">
              {featured.map(app => <AppCard key={app.appId} app={app} />)}
            </div>
          )}
        </section>
      )}

      {/* TRENDING */}
      {(loading || trending.length > 0) && (
        <section style={{ marginBottom: 48 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <div className="section-title" style={{ marginBottom: 0 }}>
              🔥 Trending Now
            </div>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/marketplace?sort=trending')}>
              View all →
            </button>
          </div>
          {loading ? (
            <div className="grid grid-apps">
              {[...Array(8)].map((_, i) => (
                <div key={i} style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden' }}>
                  <div className="skeleton" style={{ height: 140 }} />
                  <div style={{ padding: 16, background: 'var(--bg-surface)' }}>
                    <div className="skeleton" style={{ height: 16, marginBottom: 8 }} />
                    <div className="skeleton" style={{ height: 12, width: '60%' }} />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-apps">
              {trending.map(app => <AppCard key={app.appId} app={app} />)}
            </div>
          )}
        </section>
      )}

      {/* FEATURES GRID */}
      <section style={{ marginBottom: 48 }}>
        <div className="section-title">🧠 Powered by AI</div>
        <div className="grid grid-3">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="card card-hover" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div style={{
                width: 44, height: 44,
                background: 'rgba(99, 102, 241, 0.15)',
                borderRadius: 'var(--radius-md)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <Icon size={22} color="var(--primary-light)" />
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontWeight: 600 }}>{title}</div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>{desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <div style={{
        background: 'linear-gradient(135deg, rgba(99,102,241,0.15) 0%, rgba(167,139,250,0.1) 100%)',
        border: '1px solid rgba(99,102,241,0.25)',
        borderRadius: 'var(--radius-xl)',
        padding: '48px',
        textAlign: 'center',
        marginBottom: 48,
      }}>
        <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '1.75rem', marginBottom: 12 }}>
          Ready to publish your app?
        </h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: 24, maxWidth: 480, margin: '0 auto 24px' }}>
          Join thousands of developers using AppVerse AI to reach users, analyze feedback,
          and grow downloads with intelligent insights.
        </p>
        <button className="btn btn-primary btn-lg" onClick={() => navigate('/register?role=DEVELOPER')}>
          Start as Developer — It's Free
        </button>
      </div>
    </div>
  );
}
