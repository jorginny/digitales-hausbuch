package digitales_hausbuch_backend.maintenance;

import digitales_hausbuch_backend.householdobject.HouseholdObject;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
