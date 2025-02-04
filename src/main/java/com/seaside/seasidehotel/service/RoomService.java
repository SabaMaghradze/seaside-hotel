package com.seaside.seasidehotel.service;

import com.seaside.seasidehotel.model.Room;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Service
public interface RoomService {
    Room addNewRoom(MultipartFile pic, String roomType, BigDecimal roomPrice) throws SQLException, IOException;
    Set<String> getAllRoomTypes();
    List<Room> getAllRooms();
    byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException;
    void deleteRoom(Long roomId);
    Room updateRoom(Long id, String roomType, BigDecimal roomPrice, byte[] pic);
}
