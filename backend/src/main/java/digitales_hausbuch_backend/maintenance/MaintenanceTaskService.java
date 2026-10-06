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
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;

/**
 * Provides the business logic for maintenance tasks and their history.
 *
 * <p>The service verifies ownership through the complete hierarchy
 * Property -> Room -> HouseholdObject before accessing a task. It also
 * handles recurrence validation, completion, history creation, overview
 * queries and deletion of dependent maintenance records.</p>
 */
@Service
public class MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final HouseholdObjectRepository householdObjectRepository;
    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public MaintenanceTaskService(
            MaintenanceTaskRepository maintenanceTaskRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            HouseholdObjectRepository householdObjectRepository,
            RoomRepository roomRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository
    ) {
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
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
                task.getCompletedAt(),
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

    /**
     * Marks a maintenance task as completed and stores a history record.
     *
     * <p>For recurring tasks, the next due date is calculated from the
     * previous due date and the task remains open. One-time tasks are
     * marked as completed.</p>
     *
     * @param propertyId ID of the property containing the task
     * @param roomId ID of the room containing the household object
     * @param objectId ID of the household object
     * @param taskId ID of the maintenance task
     * @param note optional note stored in the maintenance history
     * @param userEmail email address of the authenticated user
     * @return the updated maintenance task
     */
    public MaintenanceTaskResponse completeMaintenanceTask(
            Long propertyId,
            Long roomId,
            Long objectId,
            Long taskId,
            String note,
            String userEmail
    ) {

        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        MaintenanceTask task =
                maintenanceTaskRepository
                        .findByIdAndHouseholdObject(
                                taskId,
                                householdObject
                        )
                        .orElseThrow(() ->
                                new MaintenanceTaskNotFoundException(
                                        "Wartungsaufgabe wurde nicht gefunden."
                                )
                        );

        LocalDate completedAt = LocalDate.now();

        MaintenanceRecord record =
                new MaintenanceRecord(
                        completedAt,
                        note,
                        task
                );

        maintenanceRecordRepository.save(record);

        task.setCompletedAt(completedAt);

        if (
                task.getRecurrenceInterval() != null &&
                        task.getRecurrenceUnit() != null
        ) {

            LocalDate nextDueDate;

            if (task.getRecurrenceUnit() == RecurrenceUnit.MONTHS) {

                nextDueDate =
                        task.getDueDate()
                                .plusMonths(
                                        task.getRecurrenceInterval()
                                );

            } else {

                nextDueDate =
                        task.getDueDate()
                                .plusYears(
                                        task.getRecurrenceInterval()
                                );
            }

            task.setDueDate(nextDueDate);
            task.setCompleted(false);

        } else {

            task.setCompleted(true);
        }

        MaintenanceTask savedTask =
                maintenanceTaskRepository.save(task);

        return toResponse(savedTask);
    }

    /**
     * Returns all open maintenance tasks of a property for the authenticated owner.
     * The repository query sorts dated tasks by due date and places tasks without
     * a due date at the end.
     *
     * @param propertyId ID of the property
     * @param userEmail email address of the authenticated user
     * @return open maintenance tasks including room and object information
     */
    public List<MaintenanceOverviewResponse> getOpenMaintenanceTasks(
            Long propertyId,
            String userEmail
    ) {
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("Benutzer wurde nicht gefunden.")
                );

        Property property = propertyRepository
                .findByIdAndOwner(propertyId, user)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Immobilie wurde nicht gefunden."
                        )
                );

        List<MaintenanceTask> tasks =
                maintenanceTaskRepository
                        .findOpenTasksByPropertySorted(property);

        return tasks.stream()
                .map(this::toOverviewResponse)
                .toList();
    }

    private MaintenanceOverviewResponse toOverviewResponse(
            MaintenanceTask task
    ) {
        HouseholdObject householdObject =
                task.getHouseholdObject();

        Room room =
                householdObject.getRoom();

        return new MaintenanceOverviewResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getRecurrenceInterval(),
                task.getRecurrenceUnit(),
                householdObject.getId(),
                householdObject.getName(),
                room.getId(),
                room.getName()
        );
    }

    /**
     * Returns the completion history of a maintenance task in descending date order.
     * Ownership of the complete resource hierarchy is checked before the history
     * is read.
     *
     * @param propertyId ID of the property
     * @param roomId ID of the room
     * @param objectId ID of the household object
     * @param taskId ID of the maintenance task
     * @param userEmail email address of the authenticated user
     * @return history records for the maintenance task
     */
    public List<MaintenanceRecordResponse> getMaintenanceHistory(
            Long propertyId,
            Long roomId,
            Long objectId,
            Long taskId,
            String userEmail
    ) {

        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        MaintenanceTask task =
                maintenanceTaskRepository
                        .findByIdAndHouseholdObject(
                                taskId,
                                householdObject
                        )
                        .orElseThrow(() ->
                                new MaintenanceTaskNotFoundException(
                                        "Wartungsaufgabe wurde nicht gefunden."
                                )
                        );

        return maintenanceRecordRepository
                .findByMaintenanceTaskOrderByCompletedAtDesc(task)
                .stream()
                .map(this::toMaintenanceRecordResponse)
                .toList();
    }

    private MaintenanceRecordResponse toMaintenanceRecordResponse(
            MaintenanceRecord record
    ) {
        return new MaintenanceRecordResponse(
                record.getId(),
                record.getCompletedAt(),
                record.getNote(),
                record.getMaintenanceTask().getId()
        );
    }

    /**
     * Deletes a maintenance task after verifying that it belongs to the
     * authenticated user. Associated history records are removed first to
     * preserve referential integrity.
     *
     * @param propertyId ID of the property
     * @param roomId ID of the room
     * @param objectId ID of the household object
     * @param taskId ID of the maintenance task
     * @param userEmail email address of the authenticated user
     */
    @Transactional
    public void deleteMaintenanceTask(
            Long propertyId,
            Long roomId,
            Long objectId,
            Long taskId,
            String userEmail
    ) {
        HouseholdObject householdObject =
                getOwnedHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        userEmail
                );

        MaintenanceTask task =
                maintenanceTaskRepository
                        .findByIdAndHouseholdObject(
                                taskId,
                                householdObject
                        )
                        .orElseThrow(
                                () -> new MaintenanceTaskNotFoundException(
                                        "Wartungsaufgabe nicht gefunden."
                                )
                        );

        deleteMaintenanceTaskWithHistory(task);
    }

    /**
     * Deletes all history records of a maintenance task before deleting the task.
     * This helper is reused by higher-level hierarchical delete operations.
     *
     * @param task maintenance task to delete
     */
    @Transactional
    public void deleteMaintenanceTaskWithHistory(
            MaintenanceTask task
    ) {
        maintenanceRecordRepository
                .deleteByMaintenanceTask(task);

        maintenanceTaskRepository
                .delete(task);
    }


}
