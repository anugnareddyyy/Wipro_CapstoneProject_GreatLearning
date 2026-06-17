import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * Route guard that redirects unauthenticated users to login.
 * Optionally restricts access to specific roles.
 *
 * Usage:
 *   <ProtectedRoute>          // any authenticated user
 *   <ProtectedRoute roles={['ADMIN']}>   // admin only
 *   <ProtectedRoute roles={['DEVELOPER','ADMIN']}>
 */
export default function ProtectedRoute({ children, roles }) {
  const { currentUser, loading, isAuthenticated } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="loading-center" style={{ minHeight: '80vh' }}>
        <div className="spinner" />
        <span>Loading...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles && !roles.includes(currentUser?.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
