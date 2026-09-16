package digitales_hausbuch_backend.room;

import digitales_hausbuch_backend.property.Property;
import digitales_hausbuch_backend.property.PropertyNotFoundException;
import digitales_hausbuch_backend.property.PropertyRepository;
import digitales_hausbuch_backend.user.User;
import digitales_hausbuch_backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public RoomService(
            RoomRepository roomRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository) {

        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    private RoomResponse toResponse(Room room) {

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getProperty().getId()
        );
    }

    public RoomResponse createRoom(
            Long propertyId,
            RoomRequest request,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = propertyRepository
                .findByIdAndOwner(propertyId, owner)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );

        Room room = new Room(
                request.name(),
                property
        );

        Room savedRoom = roomRepository.save(room);

        return toResponse(savedRoom);
    }

    public List<RoomResponse> getRooms(
            Long propertyId,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = propertyRepository
                .findByIdAndOwner(propertyId, owner)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );

        return roomRepository.findByProperty(property)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponse updateRoom(
            Long propertyId,
            Long roomId,
            RoomRequest request,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = propertyRepository
                .findByIdAndOwner(propertyId, owner)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );

        Room room = roomRepository
                .findByIdAndProperty(roomId, property)
                .orElseThrow(() ->
                        new RoomNotFoundException(
                                "Raum wurde nicht gefunden."
                        )
                );

        room.setName(request.name());

        Room savedRoom = roomRepository.save(room);

        return toResponse(savedRoom);
    }

    public void deleteRoom(
            Long propertyId,
            Long roomId,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = propertyRepository
                .findByIdAndOwner(propertyId, owner)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );

        Room room = roomRepository
                .findByIdAndProperty(roomId, property)
                .orElseThrow(() ->
                        new RoomNotFoundException(
                                "Raum wurde nicht gefunden."
                        )
                );

        roomRepository.delete(room);
    }
}
