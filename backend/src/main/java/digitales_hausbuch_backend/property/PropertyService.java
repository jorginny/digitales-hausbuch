package digitales_hausbuch_backend.property;

import digitales_hausbuch_backend.room.Room;
import digitales_hausbuch_backend.room.RoomRepository;
import digitales_hausbuch_backend.room.RoomService;
import digitales_hausbuch_backend.user.User;
import digitales_hausbuch_backend.user.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides business logic for properties owned by application users.
 *
 * <p>Property access is always scoped to the authenticated owner. Deleting
 * a property triggers an explicit bottom-up deletion of rooms, household
 * objects, maintenance tasks and maintenance records.</p>
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    public PropertyService(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            RoomRepository roomRepository,
            RoomService roomService
    ) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
    }

    public PropertyResponse createProperty(
            PropertyRequest request,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = new Property(
                request.name(),
                request.address(),
                owner
        );

        Property savedProperty =
                propertyRepository.save(property);

        return new PropertyResponse(
                savedProperty.getId(),
                savedProperty.getName(),
                savedProperty.getAddress(),
                savedProperty.getCreatedAt()
        );
    }

    public PropertyResponse getProperty(
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
                        new PropertyNotFoundException (
                                "Immobilie wurde nicht gefunden."
                        )
                );

        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getCreatedAt()
        );
    }

    public PropertyResponse updateProperty(
            Long propertyId,
            PropertyRequest request,
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
                        new PropertyNotFoundException (
                                "Immobilie wurde nicht gefunden."
                        )
                );

        property.setName(request.name());
        property.setAddress(request.address());

        Property savedProperty =
                propertyRepository.save(property);

        return new PropertyResponse(
                savedProperty.getId(),
                savedProperty.getName(),
                savedProperty.getAddress(),
                savedProperty.getCreatedAt()
        );
    }

    public List<PropertyResponse> getPropertiesForUser(String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        return propertyRepository.findByOwner(owner)
                .stream()
                .map(property -> new PropertyResponse(
                        property.getId(),
                        property.getName(),
                        property.getAddress(),
                        property.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Deletes a property and all of its dependent data.
     *
     * <p>The property is first resolved for the authenticated owner. Rooms
     * are then deleted through the service hierarchy so that nested objects,
     * maintenance tasks and maintenance history are removed in an order that
     * preserves referential integrity.</p>
     *
     * @param propertyId ID of the property
     * @param userEmail email address of the authenticated user
     */
    @Transactional
    public void deleteProperty(
            Long propertyId,
            String userEmail
    ) {
        Property property =
                getOwnedProperty(
                        propertyId,
                        userEmail
                );

        List<Room> rooms =
                roomRepository
                        .findByProperty(property);

        for (Room room : rooms) {
            roomService
                    .deleteRoomWithDependencies(
                            room
                    );
        }

        propertyRepository.delete(property);
    }

    private Property getOwnedProperty(
            Long propertyId,
            String userEmail
    ) {
        User owner =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Benutzer wurde nicht gefunden."
                                )
                        );

        return propertyRepository
                .findByIdAndOwner(
                        propertyId,
                        owner
                )
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );
    }
}
