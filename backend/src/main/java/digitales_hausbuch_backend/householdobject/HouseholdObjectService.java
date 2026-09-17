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
}
