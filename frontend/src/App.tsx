import { Navigate, Route, Routes } from 'react-router-dom';
import MainLayout from './components/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { NotificationProvider } from './hooks/useNotification';
import { WordExplorerProvider } from './hooks/useWordExplorer';
import DashboardPage from './pages/DashboardPage';
import LandingPage from './pages/LandingPage';
import LessonPage from './pages/LessonPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import RegisterPage from './pages/RegisterPage';
import WordBankPage from './pages/WordBankPage';

export default function App() {
  return (
    <NotificationProvider>
      <AuthProvider>
        {/* Any German word in the app can be clicked to open the Word Explorer */}
        <WordExplorerProvider>
          <Routes>
            {/* "/" introduces the app to visitors and steps aside for learners */}
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<MainLayout />}>
                <Route path="/learn" element={<DashboardPage />} />
                <Route path="/lesson/:lessonId" element={<LessonPage />} />
                <Route path="/words" element={<WordBankPage />} />
                <Route path="/profile" element={<ProfilePage />} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </WordExplorerProvider>
      </AuthProvider>
    </NotificationProvider>
  );
}

/**
 * A signed in learner has no use for the sales pitch, so they go straight to
 * their dashboard. Waiting for the session check first stops the landing page
 * flashing on every reload.
 */
function Home() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center bg-surface-page text-ink-muted">
        Loading…
      </div>
    );
  }

  return user ? <Navigate to="/learn" replace /> : <LandingPage />;
}
