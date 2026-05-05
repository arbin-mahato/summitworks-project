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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Configuration
@Profile("local")
public class BookingDataInitializer {
    private static final String ACTIVE = "ACTIVE";
    private static final List<HotelSeed> HOTEL_SEEDS = List.of(
            new HotelSeed("The Imperial Connaught", "Delhi", "New Delhi", "Central business hotel near Connaught Place with polished interiors and efficient service.", bd("8200")),
            new HotelSeed("Amber Courtyard Residency", "Rajasthan", "Jaipur", "Heritage-inspired stay close to old city markets and forts.", bd("6900")),
            new HotelSeed("Marine Crown Hotel", "Maharashtra", "Mumbai", "Contemporary upscale hotel with quick access to corporate and waterfront districts.", bd("11800")),
            new HotelSeed("Sea Pearl Retreat", "Goa", "Panaji", "Leisure hotel blending coastal style with easy beach connectivity.", bd("11200")),
            new HotelSeed("Silicon Grove Suites", "Karnataka", "Bengaluru", "Modern business hotel positioned for tech park commuters.", bd("7600")),
            new HotelSeed("Lakeside Cedar Inn", "Telangana", "Hyderabad", "Urban hotel with polished rooms and dependable service standards.", bd("7400")),
            new HotelSeed("Backwater Orchid Resort", "Kerala", "Kochi", "Relaxed waterfront-style property with a calm leisure atmosphere.", bd("7800")),
            new HotelSeed("Ganga View Residency", "Uttar Pradesh", "Varanasi", "Classic stay close to ghats and cultural landmarks.", bd("6100")),
            new HotelSeed("Royal Neem Palace", "Rajasthan", "Udaipur", "Refined lakeside-style hotel inspired by regional architecture.", bd("8700")),
            new HotelSeed("Monsoon Bay Hotel", "Tamil Nadu", "Chennai", "Coastal city hotel balancing business convenience and leisure comfort.", bd("7300")),
            new HotelSeed("Blue Dune Escapes", "Goa", "Calangute", "Lively holiday hotel suited for beach travelers and small groups.", bd("10400")),
            new HotelSeed("Crescent Business Hotel", "Maharashtra", "Pune", "Professional urban stay near commercial hubs and educational districts.", bd("6800")),
            new HotelSeed("Fort Horizon Stay", "Madhya Pradesh", "Indore", "Smart midscale hotel with reliable comfort and clean design.", bd("6200")),
            new HotelSeed("Temple Tree Grand", "Tamil Nadu", "Madurai", "Refined city stay designed for pilgrims and heritage travelers.", bd("5900")),
            new HotelSeed("Palm Cliff Resort", "Kerala", "Kovalam", "Seaside holiday retreat with easy access to promenades and beaches.", bd("8600")),
            new HotelSeed("Saffron City Hotel", "Uttar Pradesh", "Lucknow", "Elegant urban property with a blend of classic and modern styling.", bd("6400")),
            new HotelSeed("Golden Lakeview Inn", "Rajasthan", "Jodhpur", "Comfortable heritage-toned stay close to the old fort district.", bd("6600")),
            new HotelSeed("Harbor Line Suites", "Gujarat", "Ahmedabad", "Well-connected city hotel suited for business and event travelers.", bd("7000")),
            new HotelSeed("Regal Banyan Stay", "West Bengal", "Kolkata", "Classic metropolitan hotel near major commercial and cultural quarters.", bd("7100")),
            new HotelSeed("Hillcrest Pine Lodge", "Himachal Pradesh", "Shimla", "Cool-weather retreat with mountain-facing rooms and warm interiors.", bd("7500")),
            new HotelSeed("Canal House Premium", "Punjab", "Amritsar", "Premium city hotel with warm hospitality near cultural landmarks.", bd("6700")),
            new HotelSeed("Riverstone Grand", "Uttarakhand", "Dehradun", "Mid-upscale hotel with calm interiors and efficient city access.", bd("6900")),
            new HotelSeed("Mirage Desert Camp Hotel", "Rajasthan", "Jaisalmer", "Warm desert-themed property blending comfort with local character.", bd("7200")),
            new HotelSeed("Cedar Creek Hotel", "Karnataka", "Mysuru", "Refined stay with understated interiors near the palace district.", bd("6500")),
            new HotelSeed("Coral Harbour Residency", "Andhra Pradesh", "Visakhapatnam", "Sea-facing urban hotel with dependable service and modern rooms.", bd("7100")),
            new HotelSeed("Lotus Court Hotel", "Bihar", "Patna", "Business-friendly hotel offering clean layouts and practical amenities.", bd("5600")),
            new HotelSeed("Shoreline Regent", "Goa", "Candolim", "Upscale coastal stay with a lively atmosphere and resort-style amenities.", bd("11500")),
            new HotelSeed("Metroline Plaza", "Delhi", "New Delhi", "Smart hotel built for business travelers and quick city movement.", bd("7900")),
            new HotelSeed("Sunlit Mango Hotel", "Maharashtra", "Nagpur", "Warm-toned city stay with comfortable rooms and practical services.", bd("5900")),
            new HotelSeed("White Cedar Retreat", "Uttarakhand", "Rishikesh", "Calm riverside-inspired property for wellness and leisure travelers.", bd("7200")),
            new HotelSeed("Velvet Oak Hotel", "Punjab", "Ludhiana", "Business-focused hotel with polished interiors and efficient service.", bd("6100")),
            new HotelSeed("Heritage Moss Residency", "Odisha", "Bhubaneswar", "Contemporary city hotel with subtle cultural design influences.", bd("6300")),
            new HotelSeed("Silver Palm Suites", "Karnataka", "Mangaluru", "Coastal business-leisure hotel with comfortable contemporary rooms.", bd("6800")),
            new HotelSeed("Bamboo Trail Hotel", "Assam", "Guwahati", "Modern hotel offering relaxed interiors and easy airport access.", bd("6600")),
            new HotelSeed("Opal Crest Hotel", "Telangana", "Warangal", "Midscale urban property with reliable standards and clean finishes.", bd("5800")),
            new HotelSeed("Red Earth Lodge", "Chhattisgarh", "Raipur", "Comfort-focused hotel with straightforward amenities and clean rooms.", bd("5700")),
            new HotelSeed("Moonlit Courtyard", "Madhya Pradesh", "Bhopal", "City hotel with polished interiors and balanced business-leisure appeal.", bd("6100")),
            new HotelSeed("Emerald Passage Hotel", "Kerala", "Munnar", "Hill retreat with scenic charm and comfortable leisure accommodation.", bd("8400")),
            new HotelSeed("Cobalt Residency", "Tamil Nadu", "Coimbatore", "Practical city hotel serving business, transit, and family travelers.", bd("6200")),
            new HotelSeed("River Pearl Stay", "Punjab", "Chandigarh", "Planned-city hotel with clean modern lines and efficient service.", bd("7600")),
            new HotelSeed("Sunspire Residency", "Gujarat", "Surat", "Comfortable city hotel with polished rooms and dependable service.", bd("6500")),
            new HotelSeed("Jade Fountain Hotel", "Maharashtra", "Nashik", "Relaxed city stay with vineyard-region appeal and contemporary rooms.", bd("6400")),
            new HotelSeed("Snow Maple Lodge", "Jammu and Kashmir", "Srinagar", "Scenic stay with serene decor inspired by valley landscapes.", bd("9300")),
            new HotelSeed("Stone Arch Residency", "Jharkhand", "Ranchi", "Comfort-driven business hotel with simple, polished interiors.", bd("5600")),
            new HotelSeed("Harbor Mist Hotel", "Kerala", "Kozhikode", "Coastal city property with soft interiors and reliable service.", bd("6900")),
            new HotelSeed("Platinum Gateway", "Haryana", "Gurugram", "Contemporary business hotel in a major corporate district.", bd("9100")),
            new HotelSeed("Copper Leaf Retreat", "Uttar Pradesh", "Agra", "Tourist-friendly hotel balancing comfort and heritage route access.", bd("6700")),
            new HotelSeed("Cloudberry Heights", "Sikkim", "Gangtok", "Mountain city stay with scenic outlooks and warm hospitality.", bd("7800")),
            new HotelSeed("Marigold Crest Hotel", "Andhra Pradesh", "Vijayawada", "Smart city hotel designed for business and transit convenience.", bd("6100")),
            new HotelSeed("Tamarind House Stay", "Tamil Nadu", "Puducherry", "French-quarter inspired stay with calm spaces and boutique character.", bd("8300"))
    );

    @Bean
    CommandLineRunner seedHotels(
            HotelRepository hotelRepository,
            RoomRepository roomRepository
    ) {
        return args -> {
            for (int i = 0; i < HOTEL_SEEDS.size(); i++) {
                HotelSeed seed = HOTEL_SEEDS.get(i);
                Hotel hotel = createHotelIfMissing(hotelRepository, seed);
                createStandardRooms(roomRepository, hotel, seed.pricePerNight(), i + 1);
            }
        };
    }

    private Hotel createHotelIfMissing(HotelRepository hotelRepository, HotelSeed seed) {
        return hotelRepository.findByName(seed.name())
                .orElseGet(() -> hotelRepository.save(buildHotel(seed)));
    }

    private void createStandardRooms(
            RoomRepository roomRepository,
            Hotel hotel,
            BigDecimal basePrice,
            int hotelIndex
    ) {
        createRoomIfMissing(
                roomRepository,
                hotel,
                String.format("STD-%03d", 100 + hotelIndex),
                "Standard",
                standardDescription(hotel.getCity()),
                standardFeatures(hotel.getState()),
                scaledPrice(basePrice, "0.92"),
                isBooked(hotelIndex, 1),
                bookedDate(hotelIndex, 1)
        );
        createRoomIfMissing(
                roomRepository,
                hotel,
                String.format("DLX-%03d", 200 + hotelIndex),
                "Deluxe",
                deluxeDescription(hotel.getCity()),
                deluxeFeatures(hotel.getState()),
                scaledPrice(basePrice, "1.04"),
                isBooked(hotelIndex, 2),
                bookedDate(hotelIndex, 2)
        );
        createRoomIfMissing(
                roomRepository,
                hotel,
                String.format("STE-%03d", 300 + hotelIndex),
                "Suite",
                suiteDescription(hotel.getCity()),
                suiteFeatures(hotel.getState()),
                scaledPrice(basePrice, "1.16"),
                isBooked(hotelIndex, 3),
                bookedDate(hotelIndex, 3)
        );
    }

    private void createRoomIfMissing(
            RoomRepository roomRepository,
            Hotel hotel,
            String roomLabel,
            String roomType,
            String description,
            String features,
            BigDecimal price,
            boolean booked,
            LocalDate bookedDate
    ) {
        if (roomRepository.existsByHotelIdAndRoomLabel(hotel.getId(), roomLabel)) {
            return;
        }

        roomRepository.save(buildRoom(hotel, roomLabel, roomType, description, features, price, booked, bookedDate));
    }

    private Hotel buildHotel(HotelSeed seed) {
        Hotel hotel = new Hotel();
        hotel.setName(seed.name());
        hotel.setState(seed.state());
        hotel.setCity(seed.city());
        hotel.setDescription(seed.description());
        hotel.setPricePerNight(seed.pricePerNight());
        return hotel;
    }

    private Room buildRoom(
            Hotel hotel,
            String roomLabel,
            String roomType,
            String description,
            String features,
            BigDecimal price,
            boolean booked,
            LocalDate bookedDate
    ) {
        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomLabel(roomLabel);
        room.setRoomType(roomType);
        room.setDescription(description);
        room.setFeatures(features);
        room.setState(ACTIVE);
        room.setBooked(booked);
        room.setBookedDate(booked ? bookedDate : null);
        room.setPrice(price);
        return room;
    }

    private boolean isBooked(int hotelIndex, int roomOffset) {
        return (hotelIndex + roomOffset) % 2 == 0;
    }

    private LocalDate bookedDate(int hotelIndex, int roomOffset) {
        if (!isBooked(hotelIndex, roomOffset)) {
            return null;
        }
        return LocalDate.now().minusDays((hotelIndex * 3L + roomOffset) % 18 + 1);
    }

    private BigDecimal scaledPrice(BigDecimal basePrice, String factor) {
        return basePrice.multiply(new BigDecimal(factor)).setScale(2, RoundingMode.HALF_UP);
    }

    private String standardDescription(String city) {
        return "Comfortable room designed for practical stays in " + city + ".";
    }

    private String deluxeDescription(String city) {
        return "Deluxe room with added space and upgraded furnishings in " + city + ".";
    }

    private String suiteDescription(String city) {
        return "Well-appointed suite suited for longer premium stays in " + city + ".";
    }

    private String standardFeatures(String state) {
        if (isHillState(state)) {
            return "WiFi, Heater, TV, Tea Maker";
        }
        return "WiFi, AC, TV, Tea Maker";
    }

    private String deluxeFeatures(String state) {
        if (isCoastalState(state)) {
            return "WiFi, AC, TV, Balcony, Mini Fridge";
        }
        if (isHillState(state)) {
            return "WiFi, Heater, TV, Balcony, Coffee Maker";
        }
        return "WiFi, AC, TV, Work Desk, Mini Fridge";
    }

    private String suiteFeatures(String state) {
        if (isCoastalState(state)) {
            return "WiFi, AC, TV, Living Area, Sea View";
        }
        if (isHillState(state)) {
            return "WiFi, Heater, TV, Living Area, Valley View";
        }
        return "WiFi, AC, TV, Living Area, Bathtub";
    }

    private boolean isCoastalState(String state) {
        return List.of("Goa", "Kerala", "Tamil Nadu", "Andhra Pradesh", "Karnataka").contains(state);
    }

    private boolean isHillState(String state) {
        return List.of("Himachal Pradesh", "Uttarakhand", "Jammu and Kashmir", "Sikkim").contains(state);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record HotelSeed(
            String name,
            String state,
            String city,
            String description,
            BigDecimal pricePerNight
    ) {
    }
}
