package com.example.Summit_Project.booking.repository;

import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

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

    List<Booking> findByUserUsernameOrderByBookingDateDesc(String username);

    List<Booking> findAllByOrderByBookingDateDesc();
}
