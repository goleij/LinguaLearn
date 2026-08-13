import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

/** Toast notifications shown over the page. */
export type NotificationVariant = 'default' | 'success' | 'error';
export type NotificationPosition = 'top-center' | 'middle' | 'bottom-start';

interface NotificationOptions {
  variant?: NotificationVariant;
  position?: NotificationPosition;
  duration?: number;
}

interface ActiveNotification extends Required<NotificationOptions> {
  id: number;
  message: string;
}

interface NotificationContextValue {
  show: (message: string, options?: NotificationOptions) => void;
}

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

const positionClasses: Record<NotificationPosition, string> = {
  'top-center': 'top-6 left-1/2 -translate-x-1/2',
  middle: 'top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2',
  'bottom-start': 'bottom-6 left-6',
};

const variantClasses: Record<NotificationVariant, string> = {
  default: 'bg-ink text-white',
  success: 'bg-brand-green text-white',
  error: 'bg-feedback-error text-white',
};

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [notifications, setNotifications] = useState<ActiveNotification[]>([]);

  const show = useCallback((message: string, options: NotificationOptions = {}) => {
    const notification: ActiveNotification = {
      id: Date.now() + Math.random(),
      message,
      variant: options.variant ?? 'default',
      position: options.position ?? 'bottom-start',
      duration: options.duration ?? 3000,
    };

    setNotifications((current) => [...current, notification]);
    window.setTimeout(() => {
      setNotifications((current) => current.filter((item) => item.id !== notification.id));
    }, notification.duration);
  }, []);

  const value = useMemo(() => ({ show }), [show]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      {notifications.map((notification) => (
        <div
          key={notification.id}
          role="alert"
          className={`fixed z-50 rounded-xl px-5 py-3 text-sm font-medium shadow-card ${
            positionClasses[notification.position]
          } ${variantClasses[notification.variant]}`}
        >
          {notification.message}
        </div>
      ))}
    </NotificationContext.Provider>
  );
}

export function useNotification(): NotificationContextValue {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used inside a NotificationProvider');
  }
  return context;
}
