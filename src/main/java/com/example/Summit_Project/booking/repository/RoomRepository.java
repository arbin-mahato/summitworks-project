package com.example.Summit_Project.booking.repository;

import com.example.Summit_Project.booking.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByHotelIdAndRoomLabel(Long hotelId, String roomLabel);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "hotel")
    Optional<Room> findByIdAndHotelIdAndStateIgnoreCase(Long id, Long hotelId, String state);

    @EntityGraph(attributePaths = "hotel")
    List<Room> findByHotelIdAndStateIgnoreCaseOrderByIdAsc(Long hotelId, String state);

    @EntityGraph(attributePaths = "hotel")
    Optional<Room> findByIdAndStateIgnoreCase(Long id, String state);
}
