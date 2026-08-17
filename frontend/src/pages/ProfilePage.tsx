import { useEffect, useState } from 'react';
import { learningService } from '../services/learningService';
import type { RecentActivity, Profile } from '../types';

/** Avatar, stat cards and the recent activity list. */
export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    learningService
      .getProfile()
      .then(setProfile)
      .catch((error) => console.error('Failed to load the profile', error))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="p-5 text-ink-muted">Loading…</div>;
  }

  if (!profile) {
    return <div className="p-5 text-ink-muted">Profile unavailable</div>;
  }

  const { user, completedLessonsCount, recentActivity } = profile;
  const memberSince = new Date(user.createdAt).toLocaleDateString('en-US', {
    month: 'long',
    year: 'numeric',
  });

  return (
    <div className="flex h-full flex-col items-center p-5">
      <div className="flex w-full max-w-[600px] flex-col items-center rounded-[20px] bg-white p-5 shadow-card">
        <div className="flex h-[100px] w-[100px] items-center justify-center rounded-full bg-brand-green">
          <span className="text-[40px] font-bold text-white">
            {user.username.charAt(0).toUpperCase()}
          </span>
        </div>

        <h2 className="mb-[5px] mt-[10px] text-ink">{user.username}</h2>
        <p className="m-0 text-ink-muted">{user.email}</p>

        <div className="mt-5 flex w-full justify-center gap-2">
          <StatCard label="Total XP" value={String(user.totalXp)} color="bg-brand-yellow" />
          <StatCard label="Streak" value={`${user.currentStreak} days`} color="bg-brand-orange" />
          <StatCard
            label="Completed"
            value={`${completedLessonsCount} lessons`}
            color="bg-brand-green"
          />
        </div>

        <section className="mt-[30px] w-full">
          <h3 className="text-ink">Recent RecentActivity</h3>

          {recentActivity.length === 0 ? (
            <p className="text-ink-muted">No completed lessons yet. Start learning!</p>
          ) : (
            recentActivity.map((activity) => (
              <RecentActivityRow key={activity.lessonId} activity={activity} />
            ))
          )}
        </section>

        <p className="mt-5 text-xs text-ink-faint">Member since {memberSince}</p>
      </div>
    </div>
  );
}

function StatCard({ label, value, color }: { label: string; value: string; color: string }) {
  return (
    <div className={`flex w-[120px] flex-col items-center rounded-2xl p-4 ${color}`}>
      <span className="text-2xl font-bold text-white">{value}</span>
      <span className="text-xs text-white/80">{label}</span>
    </div>
  );
}

function RecentActivityRow({ activity }: { activity: RecentActivity }) {
  const date = activity.completedAt
    ? new Date(activity.completedAt).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      })
    : 'Recently';

  return (
    <div className="mb-[10px] flex w-full items-center rounded-[10px] bg-surface-page p-4">
      <span className="mr-[10px] text-xl text-brand-green">✓</span>

      <div className="flex flex-1 flex-col">
        <span className="font-bold text-ink">{activity.lessonName}</span>
        <span className="text-xs text-ink-muted">{date}</span>
      </div>

      <span className="rounded-[10px] bg-brand-yellow px-[10px] py-[3px] text-xs font-bold text-ink">
        +{activity.xpEarned} XP
      </span>
    </div>
  );
}
