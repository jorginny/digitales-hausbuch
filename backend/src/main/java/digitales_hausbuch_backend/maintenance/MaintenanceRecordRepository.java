package digitales_hausbuch_backend.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordRepository
        extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord>
    findByMaintenanceTaskOrderByCompletedAtDesc(
            MaintenanceTask maintenanceTask
    );

    void deleteByMaintenanceTask(
            MaintenanceTask maintenanceTask
    );

    boolean existsByMaintenanceTask_Id(
            Long maintenanceTaskId
    );
}
