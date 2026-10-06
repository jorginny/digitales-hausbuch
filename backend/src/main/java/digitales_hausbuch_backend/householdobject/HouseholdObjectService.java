package digitales_hausbuch_backend.householdobject;

import digitales_hausbuch_backend.maintenance.MaintenanceTask;
import digitales_hausbuch_backend.property.Property;
import digitales_hausbuch_backend.property.PropertyNotFoundException;
import digitales_hausbuch_backend.property.PropertyRepository;
import digitales_hausbuch_backend.room.Room;
import digitales_hausbuch_backend.room.RoomNotFoundException;
import digitales_hausbuch_backend.room.RoomRepository;
import digitales_hausbuch_backend.user.User;
import digitales_hausbuch_backend.user.UserRepository;
import digitales_hausbuch_backend.maintenance.MaintenanceTask;
import digitales_hausbuch_backend.maintenance.MaintenanceTaskRepository;
import digitales_hausbuch_backend.maintenance.MaintenanceTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides business logic for household objects assigned to rooms.
 *
 * <p>Operations validate the authenticated user's ownership through the
 * Property -> Room hierarchy. Deletion also removes all maintenance tasks
 * and their history records before the household object itself is deleted.</p>
 */
@Service
public class HouseholdObjectService {

    private final HouseholdObjectRepository householdObjectRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final MaintenanceTaskService maintenanceTaskService;

    public HouseholdObjectService(
            HouseholdObjectRepository householdObjectRepository,
            RoomRepository roomRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            MaintenanceTaskRepository maintenanceTaskRepository,
            MaintenanceTaskService maintenanceTaskService
    ) {
        this.householdObjectRepository = householdObjectRepository;
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.maintenanceTaskService = maintenanceTaskService;
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


    /**
     * Deletes a household object after verifying ownership of the containing
     * property and room.
     *
     * @param propertyId ID of the property
     * @param roomId ID of the room
     * @param objectId ID of the household object
     * @param userEmail email address of the authenticated user
     */
    @Transactional
    public void deleteHouseholdObject(
            Long propertyId,
            Long roomId,
            Long objectId,
            String userEmail) {

        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        deleteHouseholdObjectWithDependencies(
                householdObject
        );
    }

    private HouseholdObject getOwnedHouseholdObject(
            Long propertyId,
            Long roomId,
            Long objectId,
            String userEmail
    ) {
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
                .findByIdAndRoom(objectId, room)
                .orElseThrow(() ->
                        new HouseholdObjectNotFoundException(
                                "Objekt wurde nicht gefunden."
                        )
                );
    }

    /**
     * Deletes all maintenance tasks and history records belonging to a
     * household object before deleting the object itself.
     *
     * <p>This method is also used by room deletion so that dependent records
     * are removed in the correct order.</p>
     *
     * @param householdObject household object to delete
     */
    @Transactional
    public void deleteHouseholdObjectWithDependencies(
            HouseholdObject householdObject
    ) {
        List<MaintenanceTask> maintenanceTasks =
                maintenanceTaskRepository
                        .findByHouseholdObject(
                                householdObject
                        );

        for (MaintenanceTask task : maintenanceTasks) {
            maintenanceTaskService
                    .deleteMaintenanceTaskWithHistory(task);
        }

        householdObjectRepository
                .delete(householdObject);
    }
}
