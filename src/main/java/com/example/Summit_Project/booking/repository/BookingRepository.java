package com.example.Summit_Project.booking.repository;

import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.dto.AdminBookingView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select b
            from Booking b
            where b.room.id in :roomIds
              and b.status = :status
              and b.checkInDate < :checkOutDate
              and b.checkOutDate > :checkInDate
            """)
    List<Booking> findOverlappingBookingsForRooms(
            @Param("roomIds") List<Long> roomIds,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("status") BookingStatus status
    );

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.room.id = :roomId
              and b.status = :status
              and b.checkInDate < :checkOutDate
              and b.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingBookingForRoom(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("status") BookingStatus status
    );

    boolean existsByRoomIdAndStatusAndCheckOutDateGreaterThanEqual(Long roomId, BookingStatus status, LocalDate checkOutDate);

    boolean existsByRoomId(Long roomId);

    Optional<Booking> findByIdAndUserEmail(Long id, String email);

    Optional<Booking> findFirstByRoomIdAndStatusAndCheckOutDateGreaterThanEqualOrderByCheckInDateAsc(
            Long roomId,
            BookingStatus status,
            LocalDate checkOutDate
    );

    @Query("""
            select new com.example.Summit_Project.booking.dto.AdminBookingView(
                b.id,
                coalesce(u.fullName, 'Deleted user'),
                coalesce(h.name, 'Deleted hotel'),
                coalesce(r.roomLabel, 'Deleted room'),
                b.checkInDate,
                b.checkOutDate,
                b.totalPrice,
                b.status,
                b.bookingDate
            )
            from Booking b
            left join b.user u
            left join b.hotel h
            left join b.room r
            where (:status is null or b.status = :status)
              and (:checkInDateFrom is null or b.checkInDate >= :checkInDateFrom)
              and (:checkOutDateTo is null or b.checkOutDate <= :checkOutDateTo)
            order by b.bookingDate desc
            """)
    List<AdminBookingView> findAdminBookings(
            @Param("status") BookingStatus status,
            @Param("checkInDateFrom") LocalDate checkInDateFrom,
            @Param("checkOutDateTo") LocalDate checkOutDateTo
    );

    List<Booking> findByUserEmailOrderByBookingDateDesc(String email);

    List<Booking> findAllByOrderByBookingDateDesc();
}
