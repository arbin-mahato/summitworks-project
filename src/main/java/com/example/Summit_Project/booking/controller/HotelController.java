package com.example.Summit_Project.booking.controller;

import com.example.Summit_Project.booking.dto.HotelResponse;
import com.example.Summit_Project.booking.dto.HotelCalendarResponse;
import com.example.Summit_Project.booking.service.HotelService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getHotels(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") @Min(1) @Max(7) int days
    ) {
        return ResponseEntity.ok(hotelService.getHotels(state, city, startDate, days));
    }

    @GetMapping("/{hotelId}/calendar")
    public ResponseEntity<HotelCalendarResponse> getHotelCalendar(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") @Min(1) @Max(7) int days
    ) {
        return ResponseEntity.ok(hotelService.getHotelCalendar(hotelId, startDate, days));
    }
}
