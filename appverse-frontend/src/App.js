import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Navbar from './components/common/Navbar';


// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import MarketplacePage from './pages/marketplace/MarketplacePage';
import AppDetailPage from './pages/marketplace/AppDetailPage';
import DeveloperConsolePage from './pages/developer/DeveloperConsolePage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import RecommendationsPage from './pages/ai/RecommendationsPage';
import ChatBot from './pages/ai/ChatBot';
import ReviewDashboardPage from './pages/admin/ReviewDashboardPage';
 
// inside <Routes>, alongside your other admin routes:
<Route path="/admin/reviews" element={
  <ProtectedRoute roles={['ADMIN']}>
    <ReviewDashboardPage />
  </ProtectedRoute>
} />
 

import './styles/global.css';

/**
 * Root application component.
 * Sets up routing, auth context, and toast notifications.
 */
function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        {/* Global toast notifications */}
        <Toaster
          position="top-right"
          toastOptions={{
            style: {
              background: 'var(--bg-surface)',
              color: 'var(--text)',
              border: '1px solid var(--border-light)',
              fontFamily: 'var(--font-body)',
              fontSize: '0.875rem',
            },
            success: { iconTheme: { primary: 'var(--success)', secondary: 'white' } },
            error: { iconTheme: { primary: 'var(--danger)', secondary: 'white' } },
          }}
        />

        <div className="app-layout">
          <Navbar />

          <main className="main-content">
            <Routes>
              {/* Public */}
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/marketplace" element={<MarketplacePage />} />
              <Route path="/apps/:appId" element={<AppDetailPage />} />

              {/* Protected: any authenticated user */}
              <Route path="/recommendations" element={
                <ProtectedRoute>
                  <RecommendationsPage />
                </ProtectedRoute>
              } />

              {/* Protected: Developer or Admin */}
              <Route path="/developer" element={
                <ProtectedRoute roles={['DEVELOPER', 'ADMIN']}>
                  <DeveloperConsolePage />
                </ProtectedRoute>
              } />

              {/* Protected: Admin only */}
              <Route path="/admin" element={
                <ProtectedRoute roles={['ADMIN']}>
                  <AdminDashboardPage />
                </ProtectedRoute>
              } />

              {/* Unauthorized */}
              <Route path="/unauthorized" element={
                <div className="empty-state" style={{ minHeight: '60vh' }}>
                  <div className="empty-state-icon">🔒</div>
                  <h3>Access Denied</h3>
                  <p>You don't have permission to view this page.</p>
                </div>
              } />

              {/* 404 */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>

          {/* Footer */}
          <footer style={{
            borderTop: '1px solid var(--border)',
            padding: '16px 24px',
            textAlign: 'center',
            color: 'var(--text-dim)',
            fontSize: '0.8rem',
            background: 'var(--bg-surface)',
          }}>
            ⬡ AppVerse AI · Smart App Marketplace · Built with Spring Boot & React
          </footer>
          <ChatBot/>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
