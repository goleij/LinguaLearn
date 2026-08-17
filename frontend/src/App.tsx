import { Route, Routes } from 'react-router-dom';
import MainLayout from './components/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider } from './hooks/useAuth';
import { NotificationProvider } from './hooks/useNotification';
import { WordExplorerProvider } from './hooks/useWordExplorer';
import DashboardPage from './pages/DashboardPage';
import LessonPage from './pages/LessonPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import RegisterPage from './pages/RegisterPage';

export default function App() {
  return (
    <NotificationProvider>
      <AuthProvider>
        {/* Any German word in the app can be clicked to open the Word Explorer */}
        <WordExplorerProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<MainLayout />}>
                <Route path="/" element={<DashboardPage />} />
                <Route path="/lesson/:lessonId" element={<LessonPage />} />
                <Route path="/profile" element={<ProfilePage />} />
              </Route>
            </Route>
          </Routes>
        </WordExplorerProvider>
      </AuthProvider>
    </NotificationProvider>
  );
}
