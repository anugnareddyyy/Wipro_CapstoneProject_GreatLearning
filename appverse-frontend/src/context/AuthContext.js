import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/api';
import toast from 'react-hot-toast';

/**
 * Authentication Context for AppVerse.
 * 
 * Provides:
 * - currentUser: logged-in user object or null
 * - login / register / logout methods
 * - isAuthenticated, isAdmin, isDeveloper helpers
 * - loading state for initial auth check
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount: restore user session from localStorage
  useEffect(() => {
    const stored = localStorage.getItem('appverse_user');
    const token = localStorage.getItem('appverse_token');
    if (stored && token) {
      try {
        setCurrentUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem('appverse_user');
        localStorage.removeItem('appverse_token');
      }
    }
    setLoading(false);
  }, []);

  const saveSession = (data) => {
    localStorage.setItem('appverse_token', data.token);
    localStorage.setItem('appverse_user', JSON.stringify({
      userId: data.userId,
      username: data.username,
      email: data.email,
      role: data.role,
      fullName: data.fullName,
      avatarUrl: data.avatarUrl,
    }));
    setCurrentUser({
      userId: data.userId,
      username: data.username,
      email: data.email,
      role: data.role,
      fullName: data.fullName,
      avatarUrl: data.avatarUrl,
    });
  };

  const login = useCallback(async (credentials) => {
    const { data } = await authService.login(credentials);
    saveSession(data);
    toast.success(`Welcome back, ${data.username}!`);
    return data;
  }, []);

  const register = useCallback(async (formData) => {
    const { data } = await authService.register(formData);
    saveSession(data);
    toast.success(`Account created! Welcome, ${data.username}!`);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('appverse_token');
    localStorage.removeItem('appverse_user');
    setCurrentUser(null);
    toast.success('Logged out successfully');
  }, []);

  const isAuthenticated = Boolean(currentUser);
  const isAdmin = currentUser?.role === 'ADMIN';
  const isDeveloper = currentUser?.role === 'DEVELOPER' || isAdmin;

  return (
    <AuthContext.Provider value={{
      currentUser, loading,
      login, register, logout,
      isAuthenticated, isAdmin, isDeveloper
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
