package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.repository.BookingRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class BookingCleanupService {
    private final BookingRepo bookingRepo;

    public BookingCleanupService(BookingRepo bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldBookings() {
        bookingRepo.deleteByBookingDateBefore(LocalDate.now().minusWeeks(2));
    }
}

