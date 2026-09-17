package digitales_hausbuch_backend.householdobject;

import digitales_hausbuch_backend.property.Property;
import digitales_hausbuch_backend.property.PropertyNotFoundException;
import digitales_hausbuch_backend.property.PropertyRepository;
import digitales_hausbuch_backend.room.Room;
import digitales_hausbuch_backend.room.RoomNotFoundException;
import digitales_hausbuch_backend.room.RoomRepository;
import digitales_hausbuch_backend.user.User;
import digitales_hausbuch_backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseholdObjectService {

    private final HouseholdObjectRepository householdObjectRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public HouseholdObjectService(
            HouseholdObjectRepository householdObjectRepository,
            RoomRepository roomRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository) {

        this.householdObjectRepository = householdObjectRepository;
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public HouseholdObjectResponse createHouseholdObject(
            Long propertyId,
            Long roomId,
            HouseholdObjectRequest request,
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

        HouseholdObject householdObject =
                new HouseholdObject(
                        request.name(),
                        request.description(),
                        request.manufacturer(),
                        request.model(),
                        request.purchaseDate(),
                        room
                );

        HouseholdObject savedObject =
                householdObjectRepository.save(householdObject);

        return toResponse(savedObject);
    }

    private HouseholdObjectResponse toResponse(
            HouseholdObject householdObject) {

        return new HouseholdObjectResponse(
                householdObject.getId(),
                householdObject.getName(),
                householdObject.getDescription(),
                householdObject.getManufacturer(),
                householdObject.getModel(),
                householdObject.getPurchaseDate(),
                householdObject.getRoom().getId()
        );
    }

    public List<HouseholdObjectResponse> getHouseholdObjects(
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

        return householdObjectRepository
                .findByRoom(room)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HouseholdObjectResponse updateHouseholdObject(
            Long propertyId,
            Long roomId,
            Long objectId,
            HouseholdObjectRequest request,
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

        HouseholdObject householdObject =
                householdObjectRepository
                        .findByIdAndRoom(objectId, room)
                        .orElseThrow(() ->
                                new HouseholdObjectNotFoundException(
                                        "Objekt wurde nicht gefunden."
                                )
                        );

        householdObject.setName(request.name());
        householdObject.setDescription(request.description());
        householdObject.setManufacturer(request.manufacturer());
        householdObject.setModel(request.model());
        householdObject.setPurchaseDate(request.purchaseDate());

        HouseholdObject savedObject =
                householdObjectRepository.save(householdObject);

        return toResponse(savedObject);
    }
}
