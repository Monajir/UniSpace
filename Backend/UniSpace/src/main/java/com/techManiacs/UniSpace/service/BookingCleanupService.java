package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.Booking;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class BookingCleanupService {

    private final MongoTemplate mongoTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingCleanupService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Runs daily at 0:00 AM
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldBookings() {
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);
        String twoWeeksAgoStr = twoWeeksAgo.format(DATE_FORMATTER);

        Query query = new Query(Criteria.where("booking_date").lt(twoWeeksAgoStr));
        mongoTemplate.remove(query, Booking.class);
    }
}