package com.example.Summit_Project.api;

import com.example.Summit_Project.auth.controller.AdminUserController;
import com.example.Summit_Project.auth.controller.AuthController;
import com.example.Summit_Project.auth.dto.AuthResponse;
import com.example.Summit_Project.auth.dto.UserRoleResponse;
import com.example.Summit_Project.auth.entity.Role;
import com.example.Summit_Project.auth.service.AuthService;
import com.example.Summit_Project.booking.controller.AdminBookingController;
import com.example.Summit_Project.booking.controller.AdminInventoryController;
import com.example.Summit_Project.booking.controller.BookingController;
import com.example.Summit_Project.booking.controller.HotelController;
import com.example.Summit_Project.booking.controller.RoomController;
import com.example.Summit_Project.booking.controller.UserBookingController;
import com.example.Summit_Project.booking.dto.AdminActionResponse;
import com.example.Summit_Project.booking.dto.AdminBookingView;
import com.example.Summit_Project.booking.dto.AdminCreateHotelRequest;
import com.example.Summit_Project.booking.dto.AdminCreateRoomRequest;
import com.example.Summit_Project.booking.dto.AdminHotelResponse;
import com.example.Summit_Project.booking.dto.AdminRoomResponse;
import com.example.Summit_Project.booking.dto.AvailabilityView;
import com.example.Summit_Project.booking.dto.BookingResponse;
import com.example.Summit_Project.booking.dto.HotelCalendarResponse;
import com.example.Summit_Project.booking.dto.HotelResponse;
import com.example.Summit_Project.booking.dto.RoomDetailResponse;
import com.example.Summit_Project.booking.dto.RoomSummaryResponse;
import com.example.Summit_Project.booking.dto.UserBookingHistoryResponse;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.service.AdminInventoryService;
import com.example.Summit_Project.booking.service.BookingService;
import com.example.Summit_Project.booking.service.HotelService;
import com.example.Summit_Project.booking.service.RoomService;
import com.example.Summit_Project.config.CorsProperties;
import com.example.Summit_Project.config.JwtProperties;
import com.example.Summit_Project.config.SecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.SecretKey;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        AdminUserController.class,
        HotelController.class,
        RoomController.class,
        BookingController.class,
        UserBookingController.class,
        AdminBookingController.class,
        AdminInventoryController.class
})
@Import({SecurityConfig.class, ApiControllerTest.TestPropertiesConfig.class})
class ApiControllerTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";
    private static final String JWT_ISSUER = "test-suite";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockBean
    private AuthService authService;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private AdminInventoryService adminInventoryService;

    @Test
    void loginReturnsJwtPayload() throws Exception {
        Instant expiresAt = Instant.parse("2026-05-04T10:15:30Z");
        when(authService.authenticate(any())).thenReturn(new AuthResponse("jwt-token", "Bearer", expiresAt, Role.USER));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "User123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("auth_token=jwt-token")))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.expiresAt").value("2026-05-04T10:15:30Z"));
    }

    @Test
    void loginRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").exists());

        verify(authService, never()).authenticate(any());
    }

    @Test
    void signupCreatesUser() throws Exception {
        Instant expiresAt = Instant.parse("2026-05-04T10:15:30Z");
        when(authService.signup(any())).thenReturn(new AuthResponse("signup-token", "Bearer", expiresAt, Role.USER));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "fullName": "Arbin",
                                   "email": "arbin@example.com",
                                   "password": "abc123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("auth_token=signup-token")))
                .andExpect(jsonPath("$.token").value("signup-token"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void adminCanAssignRole() throws Exception {
        when(authService.assignRole(anyString(), any())).thenReturn(
                new UserRoleResponse("sam@example.com", Role.ADMIN, "Role updated successfully. The user must log in again to receive a token with the new role.")
        );

        mockMvc.perform(patch("/api/v1/admin/users/sam@example.com/role")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("sam@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void nonAdminCannotAssignRole() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/sam@example.com/role")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanListHotels() throws Exception {
        when(hotelService.getHotels(any(), any(), any(), anyInt())).thenReturn(List.of(
                new HotelResponse(
                        1L,
                        "Grand Summit Hotel",
                        "California",
                        "San Diego",
                        "Sea view",
                        new BigDecimal("199.99"),
                        3,
                        2,
                        List.of(new AvailabilityView(LocalDate.parse("2026-05-10"), true, 2))
                )
        ));

        mockMvc.perform(get("/api/v1/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("state", "California")
                        .param("city", "San Diego")
                        .param("startDate", "2026-05-10")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hotelName").value("Grand Summit Hotel"))
                .andExpect(jsonPath("$[0].calendar[0].available").value(true));
    }

    @Test
    void authenticatedUserCanListHotelsUsingJwtCookie() throws Exception {
        when(hotelService.getHotels(any(), any(), any(), anyInt())).thenReturn(List.of(
                new HotelResponse(
                        1L,
                        "Grand Summit Hotel",
                        "California",
                        "San Diego",
                        "Sea view",
                        new BigDecimal("199.99"),
                        3,
                        2,
                        List.of(new AvailabilityView(LocalDate.parse("2026-05-10"), true, 2))
                )
        ));

        mockMvc.perform(get("/api/v1/hotels")
                        .cookie(new Cookie("auth_token", rawJwtToken("user@example.com", "USER")))
                        .param("state", "California")
                        .param("city", "San Diego")
                        .param("startDate", "2026-05-10")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hotelName").value("Grand Summit Hotel"));
    }

    @Test
    void hotelsEndpointRejectsInvalidDays() throws Exception {
        mockMvc.perform(get("/api/v1/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("days", "8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void authenticatedUserCanGetHotelCalendar() throws Exception {
        when(hotelService.getHotelCalendar(anyLong(), any(), anyInt())).thenReturn(
                new HotelCalendarResponse(
                        1L,
                        "Grand Summit Hotel",
                        3,
                        List.of(new AvailabilityView(LocalDate.parse("2026-05-10"), true, 2))
                )
        );

        mockMvc.perform(get("/api/v1/hotels/1/calendar")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("startDate", "2026-05-10")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(1))
                .andExpect(jsonPath("$.calendar[0].availableRooms").value(2));
    }

    @Test
    void authenticatedUserCanListStates() throws Exception {
        when(hotelService.getStates()).thenReturn(List.of("Delhi", "Goa", "Maharashtra"));

        mockMvc.perform(get("/api/v1/hotels/states")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Delhi"))
                .andExpect(jsonPath("$[1]").value("Goa"))
                .andExpect(jsonPath("$[2]").value("Maharashtra"));
    }

    @Test
    void authenticatedUserCanListCitiesFilteredByState() throws Exception {
        when(hotelService.getCities("Goa")).thenReturn(List.of("Calangute", "Candolim", "Panaji"));

        mockMvc.perform(get("/api/v1/hotels/cities")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("state", "Goa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Calangute"))
                .andExpect(jsonPath("$[1]").value("Candolim"))
                .andExpect(jsonPath("$[2]").value("Panaji"));
    }

    @Test
    void authenticatedUserCanListRoomsForHotel() throws Exception {
        when(roomService.getAvailableRoomsByHotel(anyLong(), any(), any())).thenReturn(List.of(
                new RoomSummaryResponse(
                        10L,
                        "GS-101",
                        "Deluxe",
                        new BigDecimal("149.99"),
                        List.of("WiFi", "Breakfast"),
                        true
                )
        ));

        mockMvc.perform(get("/api/v1/hotels/1/rooms")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("checkInDate", "2026-05-10")
                        .param("checkOutDate", "2026-05-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomLabel").value("GS-101"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void authenticatedUserCanListAllRoomsForHotelWithoutDates() throws Exception {
        when(roomService.getRoomsByHotel(anyLong())).thenReturn(List.of(
                new RoomSummaryResponse(
                        11L,
                        "GS-102",
                        "Twin Deluxe",
                        new BigDecimal("129.99"),
                        List.of("WiFi"),
                        true
                )
        ));

        mockMvc.perform(get("/api/v1/hotels/1/rooms")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(11))
                .andExpect(jsonPath("$[0].roomLabel").value("GS-102"));
    }

    @Test
    void authenticatedUserCanGetRoomDetail() throws Exception {
        when(roomService.getRoomDetails(anyLong(), any(), any())).thenReturn(
                new RoomDetailResponse(
                        10L,
                        1L,
                        "Grand Summit Hotel",
                        "GS-101",
                        "Deluxe",
                        "Corner room",
                        List.of("WiFi", "Breakfast"),
                        new BigDecimal("149.99"),
                        true
                )
        );

        mockMvc.perform(get("/api/v1/rooms/10")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .param("checkInDate", "2026-05-10")
                        .param("checkOutDate", "2026-05-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelName").value("Grand Summit Hotel"))
                .andExpect(jsonPath("$.features[0]").value("WiFi"));
    }

    @Test
    void userCanCreateBooking() throws Exception {
        when(bookingService.createBooking(any(), any())).thenReturn(
                new BookingResponse(
                        25L,
                        "Your booking for the Hotel Grand Summit Hotel is successful",
                        "Grand Summit Hotel",
                        "GS-101",
                        LocalDate.parse("2026-05-10"),
                        LocalDate.parse("2026-05-12"),
                        new BigDecimal("299.98"),
                        BookingStatus.CONFIRMED,
                        OffsetDateTime.parse("2026-05-04T10:15:30Z")
                )
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomId": 10,
                                  "checkInDate": "2026-05-10",
                                  "checkOutDate": "2026-05-12"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(25))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void userCanCancelBooking() throws Exception {
        when(bookingService.cancelBooking(anyLong(), any())).thenReturn(
                new BookingResponse(
                        25L,
                        "Your booking for room GS-101 has been cancelled",
                        "Grand Summit Hotel",
                        "GS-101",
                        LocalDate.parse("2026-05-10"),
                        LocalDate.parse("2026-05-12"),
                        new BigDecimal("299.98"),
                        BookingStatus.CANCELLED,
                        OffsetDateTime.parse("2026-05-04T10:15:30Z")
                )
        );

        mockMvc.perform(delete("/api/v1/bookings/25")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(25))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void adminCannotCreateBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomId": 10,
                                  "checkInDate": "2026-05-10",
                                  "checkOutDate": "2026-05-12"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotCancelBooking() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/25")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookingRejectsPastCheckInDate() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomId": 10,
                                  "checkInDate": "2026-05-03",
                                  "checkOutDate": "2026-05-10"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));

        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void authenticatedUserCanReadOwnBookings() throws Exception {
        when(bookingService.getUserBookingHistory("user")).thenReturn(List.of(
                new UserBookingHistoryResponse(
                        30L,
                        "Grand Summit Hotel",
                        "San Diego",
                        "California",
                        "GS-101",
                        "Deluxe",
                        OffsetDateTime.parse("2026-05-04T10:15:30Z"),
                        LocalDate.parse("2026-05-10"),
                        LocalDate.parse("2026-05-12"),
                        new BigDecimal("299.98"),
                        BookingStatus.CONFIRMED
                )
        ));

        mockMvc.perform(get("/api/v1/users/me/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("user", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(30))
                .andExpect(jsonPath("$[0].hotelName").value("Grand Summit Hotel"));
    }

    @Test
    void adminCanReadAllBookings() throws Exception {
        when(bookingService.getAllBookingsForAdmin(any(), any(), any())).thenReturn(List.of(
                new AdminBookingView(
                        40L,
                        "Test User",
                        "Grand Summit Hotel",
                        "GS-101",
                        LocalDate.parse("2026-05-10"),
                        LocalDate.parse("2026-05-12"),
                        new BigDecimal("299.98"),
                        BookingStatus.CONFIRMED,
                        OffsetDateTime.parse("2026-05-04T10:15:30Z")
                )
        ));

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("Test User"))
                .andExpect(jsonPath("$[0].hotelName").value("Grand Summit Hotel"))
                .andExpect(jsonPath("$[0].roomLabel").value("GS-101"))
                .andExpect(jsonPath("$[0].bookingId").value(40));
    }

    @Test
    void adminCanFilterBookings() throws Exception {
        when(bookingService.getAllBookingsForAdmin(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .param("status", "CONFIRMED")
                        .param("checkInDateFrom", "2026-05-10")
                        .param("checkOutDateTo", "2026-05-12"))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanManageInventory() throws Exception {
        when(adminInventoryService.createHotel(any(AdminCreateHotelRequest.class))).thenReturn(
                new AdminHotelResponse(
                        1L,
                        "Cliff View",
                        "Colorado",
                        "Aspen",
                        "Mountain stay",
                        new BigDecimal("229.99"),
                        "Hotel created successfully"
                )
        );
        when(adminInventoryService.createRoom(anyLong(), any(AdminCreateRoomRequest.class))).thenReturn(
                new AdminRoomResponse(
                        10L,
                        1L,
                        "Cliff View",
                        "CV-101",
                        "Suite",
                        "Top floor",
                        List.of("Balcony", "Fireplace"),
                        "ACTIVE",
                        false,
                        null,
                        new BigDecimal("279.99"),
                        "Room created successfully"
                )
        );
        when(adminInventoryService.deleteRoom(10L)).thenReturn(new AdminActionResponse("Room deleted successfully"));

        mockMvc.perform(post("/api/v1/admin/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cliff View",
                                  "state": "Colorado",
                                  "city": "Aspen",
                                  "description": "Mountain stay",
                                  "pricePerNight": 229.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hotelId").value(1));

        mockMvc.perform(post("/api/v1/admin/hotels/1/rooms")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomLabel": "CV-101",
                                  "roomType": "Suite",
                                  "description": "Top floor",
                                  "features": "Balcony, Fireplace",
                                  "state": "ACTIVE",
                                  "price": 279.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(10))
                .andExpect(jsonPath("$.features[0]").value("Balcony"));

        mockMvc.perform(delete("/api/v1/admin/rooms/10")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Room deleted successfully"));
    }

    @Test
    void invalidAdminInventoryPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/hotels")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "state": "",
                                  "city": "",
                                  "pricePerNight": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void unauthenticatedProtectedEndpointIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/hotels"))
                .andExpect(status().isForbidden());
    }

    private String bearerToken(String email, String role) {
        return "Bearer " + rawJwtToken(email, role);
    }

    private String rawJwtToken(String email, String role) {
        SecretKey signingKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .issuer(JWT_ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .claim("role", role)
                .signWith(signingKey)
                .compact();
    }

    @TestConfiguration
    static class TestPropertiesConfig {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties(JWT_SECRET, 60, JWT_ISSUER);
        }

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost:4200"));
        }
    }
}
