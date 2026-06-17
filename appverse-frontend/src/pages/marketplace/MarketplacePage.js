import React, { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, SlidersHorizontal } from 'lucide-react';
import AppCard from '../../components/marketplace/AppCard';
import { appService } from '../../services/api';

const CATEGORIES = [
  'ALL','PRODUCTIVITY','ENTERTAINMENT','EDUCATION','SOCIAL','UTILITIES',
  'GAMING','FINANCE','HEALTH_FITNESS','TRAVEL','MUSIC','NEWS','SHOPPING','OTHER'
];

const SORT_OPTIONS = [
  { value: 'downloadCount,desc', label: 'Most Downloaded' },
  { value: 'averageRating,desc', label: 'Highest Rated' },
  { value: 'createdAt,desc', label: 'Newest First' },
  { value: 'name,asc', label: 'Name A–Z' },
  {value : 'price,asc', label: 'Price: Low to High'},
  {value : 'price,desc', label: 'Price: High to Low'},
];

export default function MarketplacePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);

  const searchQ = searchParams.get('search') || '';
  const categoryQ = searchParams.get('category') || 'ALL';
  const sortQ = searchParams.get('sort') || 'downloadCount,desc';

  const [localSearch, setLocalSearch] = useState(searchQ);

  const fetchApps = useCallback(async () => {
    setLoading(true);
    try {
      const [sortBy, direction] = sortQ.split(',');
      const params = { page, size: 12, sortBy, direction };

      let res;
      if (searchQ) {
        res = await appService.search({
          query: searchQ,
          category: categoryQ !== 'ALL' ? categoryQ : undefined,
          page, size: 12
        });
      } else {
        if (categoryQ !== 'ALL') params.category = categoryQ;
        res = await appService.getAll(params);
      }
      setApps(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    } catch {
      setApps([]);
    } finally {
      setLoading(false);
    }
  }, [searchQ, categoryQ, sortQ, page]);

  useEffect(() => { fetchApps(); }, [fetchApps]);
  useEffect(() => { setPage(0); }, [searchQ, categoryQ, sortQ]);

  const updateParam = (key, value) => {
    const p = new URLSearchParams(searchParams);
    if (value) p.set(key, value); else p.delete(key);
    p.delete('page');
    setSearchParams(p);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    updateParam('search', localSearch.trim());
  };

  // clears the results of search bar , when it is emptied
  const handleSearchChange = (e) => {
    const value = e.target.value;
    setLocalSearch(value);
    if (value === '') {
        updateParam('search', '');
    }
};

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">App Marketplace</h1>
        <p className="page-subtitle">Discover, download, and review apps across all categories</p>
      </div>

      {/* Search + sort bar */}
      <div className="filter-bar">
        <form onSubmit={handleSearch} style={{ flex: 1, minWidth: 240 }}>
          <div className="search-bar">
            <Search size={15} className="search-icon" />
            <input
              type="text"
              placeholder="Search apps by name, description, or tags..."
              value={localSearch}
              onChange={(e) => setLocalSearch(e.target.value)}
            />
          </div>
        </form>

        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <SlidersHorizontal size={16} color="var(--text-dim)" />
          <select
            className="form-select"
            value={sortQ}
            onChange={(e) => updateParam('sort', e.target.value)}
            style={{ width: 'auto', padding: '8px 32px 8px 12px' }}
          >
            {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      </div>

      {/* Category chips */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 24 }}>
        {CATEGORIES.map(cat => (
          <button
            key={cat}
            className={`filter-chip ${categoryQ === cat ? 'active' : ''}`}
            onClick={() => updateParam('category', cat === 'ALL' ? '' : cat)}
          >
            {cat === 'ALL' ? 'All Categories' : cat.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Results */}
      {loading ? (
        <div className="grid grid-apps">
          {[...Array(12)].map((_, i) => (
            <div key={i} style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden', border: '1px solid var(--border)' }}>
              <div className="skeleton" style={{ height: 140 }} />
              <div style={{ padding: 16, background: 'var(--bg-surface)' }}>
                <div className="skeleton" style={{ height: 16, marginBottom: 8, borderRadius: 4 }} />
                <div className="skeleton" style={{ height: 12, width: '70%', borderRadius: 4 }} />
                <div className="skeleton" style={{ height: 12, width: '50%', borderRadius: 4, marginTop: 8 }} />
              </div>
            </div>
          ))}
        </div>
      ) : apps.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🔍</div>
          <h3>No apps found</h3>
          <p>{searchQ ? `No results for "${searchQ}"` : 'No apps in this category yet'}</p>
          <button className="btn btn-ghost btn-sm" onClick={() => { setSearchParams({}); setLocalSearch(''); }}>
            Clear filters
          </button>
        </div>
      ) : (
        <>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: 16 }}>
            {searchQ && <span>Results for <strong style={{ color: 'var(--text)' }}>"{searchQ}"</strong> · </span>}
            Showing {apps.length} apps
          </div>
          <div className="grid grid-apps">
            {apps.map(app => <AppCard key={app.appId} app={app} />)}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button className="page-btn" onClick={() => setPage(p => p - 1)} disabled={page === 0}>
                ←
              </button>
              {[...Array(Math.min(totalPages, 7))].map((_, i) => (
                <button
                  key={i}
                  className={`page-btn ${page === i ? 'active' : ''}`}
                  onClick={() => setPage(i)}
                >
                  {i + 1}
                </button>
              ))}
              <button className="page-btn" onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>
                →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
