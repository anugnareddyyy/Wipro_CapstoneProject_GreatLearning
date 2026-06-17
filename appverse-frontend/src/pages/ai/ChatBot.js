import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Bot, User, Loader, ChevronDown } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
 
const SUGGESTED_PROMPTS = [
  "Recommend a productivity app",
  "Best free gaming apps?",
  "I need an education app",
  "How do I publish my app?",
  "Show me fitness apps",
];
 
const CATEGORY_EMOJI = {
  PRODUCTIVITY:'⚡', ENTERTAINMENT:'🎬', EDUCATION:'📚',
  SOCIAL:'💬', UTILITIES:'🔧', GAMING:'🎮', FINANCE:'💰',
  HEALTH_FITNESS:'💪', TRAVEL:'✈️', MUSIC:'🎵',
  NEWS:'📰', SHOPPING:'🛍️', OTHER:'📦',
};
 
export default function ChatBot() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([{
    role: 'assistant',
    content: "Hi! 👋 I'm AppBot, powered by Gemini AI. I can help you find the perfect app or answer any questions about AppVerse. What are you looking for?",
    apps: [],
  }]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [unread, setUnread] = useState(0);
  const bottomRef = useRef(null);
  const inputRef = useRef(null);
  const navigate = useNavigate();
 
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);
 
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 100);
      setUnread(0);
    }
  }, [open]);
 
  const sendMessage = async (text) => {
    const userText = text || input.trim();
    if (!userText || loading) return;
    setInput('');
 
    setMessages(prev => [...prev, { role: 'user', content: userText, apps: [] }]);
    setLoading(true);
 
    try {
      const history = messages.slice(1).map(m => ({
        role: m.role, content: m.content,
      }));
 
      const res = await api.post('/chat', { message: userText, history });
 
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: res.data.message,
        apps: res.data.recommendedApps || [],
      }]);
 
      if (!open) setUnread(n => n + 1);
 
    } catch {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: "Sorry, I'm having trouble right now. Please try again! 🔄",
        apps: [],
      }]);
    } finally {
      setLoading(false);
    }
  };
 
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };
 
  return (
    <>
      {/* Floating button */}
      <button
        onClick={() => setOpen(!open)}
        style={{
          position: 'fixed', bottom: 28, right: 28, zIndex: 1000,
          width: 56, height: 56, borderRadius: '50%',
          background: 'linear-gradient(135deg, #6366f1, #7c3aed)',
          border: 'none', cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 4px 24px rgba(99,102,241,0.5)',
          transition: 'transform 0.2s ease',
        }}
        onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.1)'}
        onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
      >
        {open ? <ChevronDown size={24} color="white" /> : <MessageCircle size={24} color="white" />}
        {unread > 0 && !open && (
          <div style={{
            position: 'absolute', top: -4, right: -4,
            width: 20, height: 20, borderRadius: '50%',
            background: '#ef4444', color: 'white',
            fontSize: '0.7rem', fontWeight: 700,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>{unread}</div>
        )}
      </button>
 
      {/* Chat window */}
      {open && (
        <div style={{
          position: 'fixed', bottom: 96, right: 28, zIndex: 999,
          width: 370, height: 540,
          background: '#111827',
          border: '1px solid #1e2d47',
          borderRadius: 20,
          display: 'flex', flexDirection: 'column',
          boxShadow: '0 8px 40px rgba(0,0,0,0.7)',
          overflow: 'hidden',
        }}>
 
          {/* Header */}
          <div style={{
            padding: '14px 16px',
            background: 'linear-gradient(135deg, #4f46e5, #7c3aed)',
            display: 'flex', alignItems: 'center', gap: 10,
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: '50%',
              background: 'rgba(255,255,255,0.2)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Bot size={20} color="white" />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ color: 'white', fontWeight: 700, fontSize: '0.95rem' }}>AppBot AI</div>
              <div style={{ color: 'rgba(255,255,255,0.7)', fontSize: '0.72rem' }}>
                Powered by Gemini · Always online
              </div>
            </div>
            <button
              onClick={() => setMessages([{
                role: 'assistant',
                content: "Chat cleared! How can I help you today?",
                apps: [],
              }])}
              style={{
                background: 'rgba(255,255,255,0.15)', border: 'none',
                borderRadius: 6, padding: '4px 8px',
                color: 'white', fontSize: '0.7rem', cursor: 'pointer',
              }}
            >Clear</button>
            <button onClick={() => setOpen(false)} style={{
              background: 'none', border: 'none',
              cursor: 'pointer', color: 'white', marginLeft: 4,
            }}>
              <X size={18} />
            </button>
          </div>
 
          {/* Messages */}
          <div style={{
            flex: 1, overflowY: 'auto', padding: '16px 14px',
            display: 'flex', flexDirection: 'column', gap: 12,
          }}>
            {messages.map((msg, i) => (
              <div key={i} style={{
                display: 'flex',
                flexDirection: msg.role === 'user' ? 'row-reverse' : 'row',
                gap: 8, alignItems: 'flex-start',
              }}>
                <div style={{
                  width: 28, height: 28, borderRadius: '50%', flexShrink: 0,
                  background: msg.role === 'user'
                    ? 'linear-gradient(135deg, #6366f1, #a78bfa)'
                    : '#1e2d47',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  {msg.role === 'user'
                    ? <User size={14} color="white" />
                    : <Bot size={14} color="#818cf8" />}
                </div>
 
                <div style={{ maxWidth: '78%', display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{
                    padding: '10px 14px',
                    borderRadius: msg.role === 'user'
                      ? '16px 4px 16px 16px'
                      : '4px 16px 16px 16px',
                    background: msg.role === 'user'
                      ? 'linear-gradient(135deg, #4f46e5, #7c3aed)'
                      : '#1a2236',
                    color: 'white', fontSize: '0.85rem', lineHeight: 1.6,
                    border: msg.role === 'assistant' ? '1px solid #1e2d47' : 'none',
                    whiteSpace: 'pre-wrap',
                  }}>
                    {msg.content}
                  </div>
 
                  {/* App cards */}
                  {msg.apps && msg.apps.length > 0 && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      <div style={{ fontSize: '0.7rem', color: '#64748b' }}>
                        🎯 Recommended apps:
                      </div>
                      {msg.apps.map(app => (
                        <div key={app.appId}
                          onClick={() => { navigate(`/apps/${app.appId}`); setOpen(false); }}
                          style={{
                            display: 'flex', gap: 10, alignItems: 'center',
                            padding: '8px 10px', background: '#1f2d45',
                            borderRadius: 10, cursor: 'pointer',
                            border: '1px solid #2a3a55',
                          }}
                        >
                          <div style={{
                            width: 32, height: 32, borderRadius: 8,
                            background: '#2a3a55', flexShrink: 0,
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            fontSize: '1rem',
                          }}>
                            {CATEGORY_EMOJI[app.category] || '📦'}
                          </div>
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{
                              fontWeight: 600, fontSize: '0.8rem', color: 'white',
                              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                            }}>{app.name}</div>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>
                              ⭐ {app.averageRating > 0 ? parseFloat(app.averageRating).toFixed(1) : 'New'}
                            </div>
                          </div>
                          <div style={{ fontSize: '0.7rem', color: '#6366f1', fontWeight: 600 }}>
                            View →
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
 
            {/* Typing indicator */}
            {loading && (
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <div style={{
                  width: 28, height: 28, borderRadius: '50%', background: '#1e2d47',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  <Bot size={14} color="#818cf8" />
                </div>
                <div style={{
                  padding: '10px 16px', background: '#1a2236',
                  borderRadius: '4px 16px 16px 16px',
                  border: '1px solid #1e2d47',
                  display: 'flex', gap: 4, alignItems: 'center',
                }}>
                  {[0, 1, 2].map(i => (
                    <div key={i} style={{
                      width: 6, height: 6, borderRadius: '50%',
                      background: '#6366f1',
                      animation: `bounce 1s ease ${i * 0.15}s infinite`,
                    }} />
                  ))}
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>
 
          {/* Suggested prompts */}
          {messages.length === 1 && (
            <div style={{ padding: '0 14px 10px', display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {SUGGESTED_PROMPTS.map(prompt => (
                <button key={prompt} onClick={() => sendMessage(prompt)} style={{
                  padding: '5px 10px',
                  background: 'rgba(99,102,241,0.12)',
                  border: '1px solid rgba(99,102,241,0.25)',
                  borderRadius: 20, color: '#818cf8', fontSize: '0.72rem',
                  cursor: 'pointer', fontFamily: 'Inter, sans-serif',
                }}>
                  {prompt}
                </button>
              ))}
            </div>
          )}
 
          {/* Input */}
          <div style={{
            padding: '12px 14px',
            borderTop: '1px solid #1e2d47',
            display: 'flex', gap: 8, alignItems: 'flex-end',
          }}>
            <textarea
              ref={inputRef}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask me anything about apps..."
              rows={1}
              style={{
                flex: 1, background: '#1a2236',
                border: '1px solid #2a3a55',
                borderRadius: 12, padding: '9px 12px',
                color: 'white', fontSize: '0.85rem',
                fontFamily: 'Inter, sans-serif',
                outline: 'none', resize: 'none',
                lineHeight: 1.5, maxHeight: 80,
              }}
            />
            <button
              onClick={() => sendMessage()}
              disabled={!input.trim() || loading}
              style={{
                width: 38, height: 38, borderRadius: 10,
                background: input.trim() && !loading
                  ? 'linear-gradient(135deg, #6366f1, #7c3aed)'
                  : '#1a2236',
                border: '1px solid #2a3a55',
                cursor: input.trim() && !loading ? 'pointer' : 'not-allowed',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}
            >
              {loading
                ? <Loader size={16} color="#6366f1" />
                : <Send size={16} color={input.trim() ? 'white' : '#64748b'} />}
            </button>
          </div>
        </div>
      )}
 
      <style>{`
        @keyframes bounce {
          0%, 100% { transform: translateY(0); opacity: 0.4; }
          50% { transform: translateY(-4px); opacity: 1; }
        }
      `}</style>
    </>
  );
}