import { AlertCircle, Bell, CheckCheck, CheckCircle2, Clock3, XCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { UserNotification, useNotifications } from "@/hooks/useNotifications";

const appearance = (type: UserNotification["type"]) => {
  if (type === "booking_approved") {
    return { icon: CheckCircle2, className: "border-success/40 bg-success/5", iconClass: "text-success" };
  }
  if (type === "booking_rejected") {
    return { icon: XCircle, className: "border-destructive/40 bg-destructive/5", iconClass: "text-destructive" };
  }
  return { icon: Clock3, className: "border-warning/40 bg-warning/5", iconClass: "text-warning" };
};

const formatTimestamp = (value: string) =>
  new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));

export function NotificationFeed() {
  const { notifications, unreadCount, loading, error, refresh, markRead, markAllRead } = useNotifications();

  return (
    <Card className="profile-section glass shadow-elegant">
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <CardTitle className="flex items-center gap-2">
          <Bell className="h-5 w-5" /> Notifications
          {unreadCount > 0 && <Badge>{unreadCount} unread</Badge>}
        </CardTitle>
        {unreadCount > 0 && (
          <Button variant="ghost" size="sm" onClick={() => void markAllRead()}>
            <CheckCheck className="mr-2 h-4 w-4" /> Mark all read
          </Button>
        )}
      </CardHeader>
      <CardContent>
        {loading && notifications.length === 0 ? (
          <div className="space-y-3">
            {[0, 1, 2].map((item) => <Skeleton key={item} className="h-24 w-full rounded-xl" />)}
          </div>
        ) : error ? (
          <div className="py-8 text-center text-destructive">
            <AlertCircle className="mx-auto mb-2 h-8 w-8" />
            <p>{error}</p>
            <Button variant="ghost" className="mt-2" onClick={() => void refresh()}>Retry</Button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="py-10 text-center text-muted-foreground">
            <Bell className="mx-auto mb-3 h-10 w-10 opacity-60" />
            <p>No notifications yet.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {notifications.map((notification) => {
              const style = appearance(notification.type);
              const Icon = style.icon;
              return (
                <div
                  key={notification.id}
                  className={`flex items-start gap-4 rounded-xl border p-4 transition-opacity ${style.className} ${notification.is_read ? "opacity-70" : "ring-1 ring-primary/15"}`}
                >
                  <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${style.iconClass}`} />
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <h4 className="font-semibold">{notification.title}</h4>
                      {!notification.is_read && <span className="h-2 w-2 rounded-full bg-primary" aria-label="Unread" />}
                    </div>
                    <p className="mt-1 text-sm text-muted-foreground">{notification.message}</p>
                    <p className="mt-2 text-xs text-muted-foreground">{formatTimestamp(notification.created_at)}</p>
                  </div>
                  {!notification.is_read && (
                    <Button variant="ghost" size="sm" onClick={() => void markRead(notification.id)}>
                      Mark read
                    </Button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
