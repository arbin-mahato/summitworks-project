package com.example.Summit_Project.booking.repository;

import com.example.Summit_Project.booking.entity.Hotel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Hotel> findByName(String name);

    @EntityGraph(attributePaths = "rooms")
    Optional<Hotel> findById(Long id);

    @EntityGraph(attributePaths = "rooms")
    List<Hotel> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "rooms")
    List<Hotel> findAllByStateIgnoreCaseOrderByNameAsc(String state);

    @EntityGraph(attributePaths = "rooms")
    List<Hotel> findAllByCityIgnoreCaseOrderByNameAsc(String city);

    @EntityGraph(attributePaths = "rooms")
    List<Hotel> findAllByStateIgnoreCaseAndCityIgnoreCaseOrderByNameAsc(String state, String city);

    @Query("""
            select distinct h.state
            from Hotel h
            order by h.state asc
            """)
    List<String> findDistinctStates();

    @Query("""
            select distinct h.city
            from Hotel h
            where (:state is null or h.state = :state)
            order by h.city asc
            """)
    List<String> findDistinctCitiesByState(@Param("state") String state);
}
