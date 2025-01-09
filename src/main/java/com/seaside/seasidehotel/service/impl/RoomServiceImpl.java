package com.seaside.seasidehotel.service.impl;

import com.seaside.seasidehotel.exception.ResourceNotFoundException;
import com.seaside.seasidehotel.model.Room;
import com.seaside.seasidehotel.repository.RoomRepository;
import com.seaside.seasidehotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room addNewRoom(MultipartFile pic, String roomType, BigDecimal roomPrice)
            throws SQLException, IOException {

        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);

        if (!pic.isEmpty()) {
            byte[] bytes = pic.getBytes();
            Blob photoBlob = new SerialBlob(bytes);
            room.setPhoto(photoBlob);
        }

        return roomRepository.save(room);
    }

    @Override
    public Set<String> getAllRoomTypes() {

        List<Room> allRooms = roomRepository.findAll();
        Set<String> allRoomTypes = new HashSet<>();

        for (Room room : allRooms) {
            allRoomTypes.add(room.getRoomType());
        }

        return allRoomTypes;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {

        Optional<Room> theRoom = roomRepository.findById(roomId);

        if (theRoom.isEmpty()) {
            throw new ResourceNotFoundException("Failed to fetch the room with ID " + roomId);
        }

        Blob photoBlob = theRoom.get().getPhoto();
        if (photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length()); // Retrieves all the bytes
            // from the Blob(Binary Large Object), starting from position 1 and continuing for the
            // entire length of the Blob.
        }
        return null;
    }
}














