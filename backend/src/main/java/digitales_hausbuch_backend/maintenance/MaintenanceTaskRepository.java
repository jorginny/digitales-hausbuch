package digitales_hausbuch_backend.maintenance;

import digitales_hausbuch_backend.householdobject.HouseholdObject;
import digitales_hausbuch_backend.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskRepository
        extends JpaRepository<MaintenanceTask, Long> {

    List<MaintenanceTask> findByHouseholdObject(
            HouseholdObject householdObject
    );

    Optional<MaintenanceTask> findByIdAndHouseholdObject(
            Long id,
            HouseholdObject householdObject
    );

    List<MaintenanceTask>
    findByHouseholdObjectRoomPropertyAndCompletedFalse(
            Property property
    );

    @Query("""
    SELECT task
    FROM MaintenanceTask task
    WHERE task.householdObject.room.property = :property
      AND task.completed = false
    ORDER BY
      CASE WHEN task.dueDate IS NULL THEN 1 ELSE 0 END,
      task.dueDate ASC
""")
    List<MaintenanceTask> findOpenTasksByPropertySorted(
            @Param("property") Property property
    );
}
