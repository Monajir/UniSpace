const BOOKING_SCHEDULE_STORAGE_KEY = "unispace_booking_schedule_updated_at";
export const BOOKING_SCHEDULE_UPDATED_EVENT = "unispace:booking-schedule-updated";

export function notifyBookingScheduleChanged() {
  localStorage.setItem(BOOKING_SCHEDULE_STORAGE_KEY, Date.now().toString());
  window.dispatchEvent(new Event(BOOKING_SCHEDULE_UPDATED_EVENT));
}

export function isBookingScheduleStorageEvent(event: StorageEvent) {
  return event.key === BOOKING_SCHEDULE_STORAGE_KEY;
}
