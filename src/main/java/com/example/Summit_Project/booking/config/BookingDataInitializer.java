package com.example.Summit_Project.booking.config;

import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.repository.HotelRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Profile("local")
public class BookingDataInitializer {
    private static final String ACTIVE = "ACTIVE";

    @Bean
    CommandLineRunner seedHotels(
            HotelRepository hotelRepository,
            RoomRepository roomRepository
    ) {
        return args -> {
            Hotel grandSummit = createHotelIfMissing(
                    hotelRepository,
                    "Grand Summit Hotel",
                    "California",
                    "San Diego",
                    "Luxury business hotel near the harbor.",
                    new BigDecimal("159.99")
            );
            Hotel oceanCrest = createHotelIfMissing(
                    hotelRepository,
                    "Ocean Crest Resort",
                    "Florida",
                    "Miami",
                    "Beachside resort with family-friendly rooms.",
                    new BigDecimal("219.50")
            );
            Hotel mapleLeaf = createHotelIfMissing(
                    hotelRepository,
                    "Maple Leaf Suites",
                    "New York",
                    "Albany",
                    "Comfort stay with long-stay amenities.",
                    new BigDecimal("134.75")
            );

            createRoomIfMissing(roomRepository, grandSummit, "GS-101", "Deluxe King", "King room with city view.", "WiFi,Smart TV,Mini Bar", new BigDecimal("159.99"));
            createRoomIfMissing(roomRepository, grandSummit, "GS-102", "Twin Deluxe", "Twin beds with workspace.", "WiFi,Work Desk,Coffee Maker", new BigDecimal("149.99"));
            createRoomIfMissing(roomRepository, grandSummit, "GS-201", "Suite", "Suite with living area.", "WiFi,Smart TV,Balcony,Mini Bar", new BigDecimal("199.99"));
            createRoomIfMissing(roomRepository, oceanCrest, "OC-301", "Ocean View", "Ocean-facing queen room.", "WiFi,Balcony,Breakfast", new BigDecimal("219.50"));
            createRoomIfMissing(roomRepository, oceanCrest, "OC-302", "Family Room", "Large room for family stays.", "WiFi,2 Queen Beds,Breakfast", new BigDecimal("239.50"));
            createRoomIfMissing(roomRepository, mapleLeaf, "ML-401", "Studio", "Compact studio room.", "WiFi,Kitchenette,Work Desk", new BigDecimal("134.75"));
            createRoomIfMissing(roomRepository, mapleLeaf, "ML-402", "Executive Studio", "Studio with extra lounge space.", "WiFi,Kitchenette,Sofa", new BigDecimal("154.75"));
        };
    }

    private Hotel createHotelIfMissing(
            HotelRepository hotelRepository,
            String name,
            String state,
            String city,
            String description,
            BigDecimal startingPrice
    ) {
        return hotelRepository.findByName(name)
                .orElseGet(() -> hotelRepository.save(buildHotel(name, state, city, description, startingPrice)));
    }

    private void createRoomIfMissing(
            RoomRepository roomRepository,
            Hotel hotel,
            String roomLabel,
            String roomType,
            String description,
            String features,
            BigDecimal price
    ) {
        if (roomRepository.existsByHotelIdAndRoomLabel(hotel.getId(), roomLabel)) {
            return;
        }

        roomRepository.save(buildRoom(hotel, roomLabel, roomType, description, features, price));
    }

    private Hotel buildHotel(String name, String state, String city, String description, BigDecimal startingPrice) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setState(state);
        hotel.setCity(city);
        hotel.setDescription(description);
        hotel.setPricePerNight(startingPrice);
        return hotel;
    }

    private Room buildRoom(
            Hotel hotel,
            String roomLabel,
            String roomType,
            String description,
            String features,
            BigDecimal price
    ) {
        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomLabel(roomLabel);
        room.setRoomType(roomType);
        room.setDescription(description);
        room.setFeatures(features);
        room.setState(ACTIVE);
        room.setBooked(false);
        room.setPrice(price);
        return room;
    }
}
