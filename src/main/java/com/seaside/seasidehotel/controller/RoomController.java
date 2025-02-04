package com.seaside.seasidehotel.controller;

import com.seaside.seasidehotel.exception.PhotoRetrievalException;
import com.seaside.seasidehotel.model.Booking;
import com.seaside.seasidehotel.model.Room;
import com.seaside.seasidehotel.response.BookingResponse;
import com.seaside.seasidehotel.response.RoomResponse;
import com.seaside.seasidehotel.service.BookingService;
import com.seaside.seasidehotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam(value = "picture", required = false) MultipartFile pic,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room saveRoom = roomService.addNewRoom(pic, roomType, roomPrice);

        RoomResponse response = new RoomResponse(saveRoom.getId(), saveRoom.getRoomType(), saveRoom.getRoomPrice());

        // removable
        if (pic != null && !pic.isEmpty()) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(saveRoom.getId());
            String base64Photo = Base64.encodeBase64String(photoBytes);
            response.setPhoto(base64Photo);
        }

        return ResponseEntity.ok(response);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/room-types")
    public Set<String> getAllRoomTypes() {
        Set<String> roomTypes = roomService.getAllRoomTypes();
        System.out.println("Room types: " + roomTypes);
        return roomTypes;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {

        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            RoomResponse roomResponse = new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice());
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                roomResponse.setPhoto(base64Photo);
            }
            roomResponses.add(roomResponse);
        }
        return ResponseEntity.ok(roomResponses);
    }

    private RoomResponse getRoomResponse(Room room) {
        List<Booking> bookings = bookingService.getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingsInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getConfirmationCode()))
                .toList();

        byte[] photo = null;
        Blob roomPhoto = room.getPhoto();

        if (roomPhoto != null) {
            try {
                photo = roomPhoto.getBytes(1, (int) roomPhoto.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(), bookingsInfo, photo);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/deleteRoom/{roomId}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable("roomId") Long roomId) {

        Map<String, Object> response = new HashMap<>();

        try {
            roomService.deleteRoom(roomId);
            response.put("message", "success");
            response.put("roomId", roomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "error");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PatchMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(required = false) MultipartFile pic) throws SQLException, IOException {

        byte[] photoBytes = pic != null && !pic.isEmpty() ? pic.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;

        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);

        theRoom.setPhoto(photoBlob);

        RoomResponse response = getRoomResponse(theRoom);

        return ResponseEntity.ok(response);
    }
}























