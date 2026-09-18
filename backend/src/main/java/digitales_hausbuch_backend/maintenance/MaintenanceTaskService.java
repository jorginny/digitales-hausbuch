package digitales_hausbuch_backend.maintenance;

import digitales_hausbuch_backend.householdobject.HouseholdObject;
import digitales_hausbuch_backend.householdobject.HouseholdObjectNotFoundException;
import digitales_hausbuch_backend.householdobject.HouseholdObjectRepository;
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
public class MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final HouseholdObjectRepository householdObjectRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public MaintenanceTaskService(
            MaintenanceTaskRepository maintenanceTaskRepository,
            HouseholdObjectRepository householdObjectRepository,
            RoomRepository roomRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository) {

        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.householdObjectRepository = householdObjectRepository;
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public MaintenanceTaskResponse createMaintenanceTask(
            Long propertyId,
            Long roomId,
            Long objectId,
            MaintenanceTaskRequest request,
            String userEmail) {

        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        validateRecurrence(
                request.recurrenceInterval(),
                request.recurrenceUnit()
        );

        MaintenanceTask task = new MaintenanceTask(
                request.title(),
                request.description(),
                request.dueDate(),
                request.recurrenceInterval(),
                request.recurrenceUnit(),
                householdObject
        );

        MaintenanceTask savedTask =
                maintenanceTaskRepository.save(task);

        return toResponse(savedTask);
    }

    public List<MaintenanceTaskResponse> getMaintenanceTasks(
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

        return maintenanceTaskRepository
                .findByHouseholdObject(householdObject)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HouseholdObject getOwnedHouseholdObject(
            Long propertyId,
            Long roomId,
            Long objectId,
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
                .findByIdAndRoom(objectId, room)
                .orElseThrow(() ->
                        new HouseholdObjectNotFoundException(
                                "Objekt wurde nicht gefunden."
                        )
                );
    }

    private MaintenanceTaskResponse toResponse(
            MaintenanceTask task) {

        return new MaintenanceTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.isCompleted(),
                task.getRecurrenceInterval(),
                task.getRecurrenceUnit(),
                task.getHouseholdObject().getId()
        );
    }

    private void validateRecurrence(
            Integer recurrenceInterval,
            RecurrenceUnit recurrenceUnit) {

        boolean intervalSet =
                recurrenceInterval != null;

        boolean unitSet =
                recurrenceUnit != null;

        if (intervalSet != unitSet) {
            throw new InvalidRecurrenceException(
                    "Wiederholungsintervall und Einheit müssen gemeinsam angegeben werden."
            );
        }
    }

    public MaintenanceTaskResponse updateMaintenanceTask(
            Long propertyId,
            Long roomId,
            Long objectId,
            Long taskId,
            MaintenanceTaskRequest request,
            String userEmail) {

        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        MaintenanceTask task = maintenanceTaskRepository
                .findByIdAndHouseholdObject(
                        taskId,
                        householdObject
                )
                .orElseThrow(() ->
                        new MaintenanceTaskNotFoundException(
                                "Wartungsaufgabe wurde nicht gefunden."
                        )
                );

        validateRecurrence(
                request.recurrenceInterval(),
                request.recurrenceUnit()
        );

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setRecurrenceInterval(
                request.recurrenceInterval()
        );
        task.setRecurrenceUnit(
                request.recurrenceUnit()
        );

        MaintenanceTask savedTask =
                maintenanceTaskRepository.save(task);

        return toResponse(savedTask);
    }


}
