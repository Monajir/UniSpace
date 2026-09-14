export interface BookingSlot {
  label: string;
  start: string;
  end: string;
}

export const BOOKING_SLOTS: readonly BookingSlot[] = [
  { label: "08:00", start: "08:00", end: "09:15" },
  { label: "09:15", start: "09:15", end: "10:30" },
  { label: "10:30", start: "10:30", end: "11:45" },
  { label: "11:45", start: "11:45", end: "13:00" },
  { label: "14:30", start: "14:30", end: "15:45" },
  { label: "15:45", start: "15:45", end: "17:00" },
];

export const findBookingSlot = (start: string | null, end: string | null) =>
  BOOKING_SLOTS.find((slot) => slot.start === start && slot.end === end);
