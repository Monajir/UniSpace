import { useCallback, useEffect, useMemo, useState } from "react";
import { apiErrorMessage, apiUrl } from "@/lib/api";

export interface UserNotification {
  id: string;
  booking_id: string | null;
  type: "booking_requested" | "booking_assigned" | "booking_approved" | "booking_rejected";
  title: string;
  message: string;
  is_read: boolean;
  created_at: string;
}

const isNotificationList = (value: unknown): value is UserNotification[] =>
  Array.isArray(value) &&
  value.every(
    (notification) =>
      notification !== null &&
      typeof notification === "object" &&
      ["id", "type", "title", "message", "created_at"].every(
        (key) => key in notification && typeof notification[key as keyof typeof notification] === "string"
      ) &&
      "is_read" in notification &&
      typeof notification.is_read === "boolean"
  );

export function useNotifications(enabled = true) {
  const [notifications, setNotifications] = useState<UserNotification[]>([]);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!enabled) return;
    const token = localStorage.getItem("auth_token");
    if (!token) {
      setError("Sign in to view notifications");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await fetch(apiUrl("/api/notifications"), {
        headers: { Authorization: `Bearer ${token}` },
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error(await apiErrorMessage(response, "Failed to load notifications"));
      }
      const data: unknown = await response.json();
      if (!isNotificationList(data)) {
        throw new Error("The server returned invalid notification data");
      }
      setNotifications(data);
    } catch (fetchError) {
      setError(fetchError instanceof Error ? fetchError.message : "Failed to load notifications");
    } finally {
      setLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    void refresh();
    window.addEventListener("focus", refresh);
    return () => window.removeEventListener("focus", refresh);
  }, [refresh]);

  const updateReadState = async (path: string, ids: string[]) => {
    const token = localStorage.getItem("auth_token");
    if (!token) return;
    try {
      setError(null);
      const response = await fetch(apiUrl(path), {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}` },
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error(await apiErrorMessage(response, "Failed to update notifications"));
      }
      const selected = new Set(ids);
      setNotifications((current) =>
        current.map((notification) =>
          selected.has(notification.id) ? { ...notification, is_read: true } : notification
        )
      );
    } catch (updateError) {
      setError(updateError instanceof Error ? updateError.message : "Failed to update notifications");
    }
  };

  const markRead = (id: string) => updateReadState(`/api/notifications/${id}/read`, [id]);
  const markAllRead = () =>
    updateReadState(
      "/api/notifications/read-all",
      notifications.filter((notification) => !notification.is_read).map((notification) => notification.id)
    );

  const unreadCount = useMemo(
    () => notifications.filter((notification) => !notification.is_read).length,
    [notifications]
  );

  return { notifications, unreadCount, loading, error, refresh, markRead, markAllRead };
}
