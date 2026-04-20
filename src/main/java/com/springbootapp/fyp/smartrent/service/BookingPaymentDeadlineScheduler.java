package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingPaymentDeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingPaymentDeadlineScheduler.class);

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public BookingPaymentDeadlineScheduler(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    @Transactional
    @Scheduled(cron = "${app.booking.payment-deadline.scheduler-cron:0 */10 * * * *}")
    public void processPaymentDeadlines() {
        LocalDateTime now = LocalDateTime.now();

        sendOneHourReminderForUnpaidBookings(now);
        autoCancelExpiredUnpaidBookings(now);
    }

    private void sendOneHourReminderForUnpaidBookings(LocalDateTime now) {
        LocalDateTime reminderWindowStart = now.minusHours(24);
        LocalDateTime reminderWindowEnd = now.minusHours(23);

        List<Booking> bookingsToWarn = bookingRepository
                .findByPaymentStatusAndBookingStatusInAndWarningSentFalseAndCreatedAtBetween(
                        Booking.PaymentStatus.UNPAID,
                        List.of(Booking.BookingStatus.PENDING, Booking.BookingStatus.CONFIRMED),
                        reminderWindowStart,
                        reminderWindowEnd
                );

        for (Booking booking : bookingsToWarn) {
            try {
                String to = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
                if (to != null && !to.isBlank()) {
                    emailService.sendPaymentDeadlineReminder(to, booking.getBookingId());
                }
                booking.setWarningSent(true);
                bookingRepository.save(booking);
            } catch (Exception e) {
                log.warn("Failed to send payment deadline reminder for booking {}: {}",
                        booking.getBookingId(), e.getMessage());
            }
        }
    }

    private void autoCancelExpiredUnpaidBookings(LocalDateTime now) {
        LocalDateTime expiryCutoff = now.minusHours(24);

        List<Booking> bookingsToCancel = bookingRepository
                .findByPaymentStatusAndBookingStatusInAndCreatedAtBefore(
                        Booking.PaymentStatus.UNPAID,
                        List.of(Booking.BookingStatus.PENDING, Booking.BookingStatus.CONFIRMED),
                        expiryCutoff
                );

        for (Booking booking : bookingsToCancel) {
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            booking.setWarningSent(true);
            bookingRepository.save(booking);

            try {
                String to = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
                if (to != null && !to.isBlank()) {
                    emailService.sendAutoCancellationNotice(to, booking.getBookingId());
                }
            } catch (Exception e) {
                log.warn("Failed to send auto-cancellation notice for booking {}: {}",
                        booking.getBookingId(), e.getMessage());
            }
        }
    }
}
