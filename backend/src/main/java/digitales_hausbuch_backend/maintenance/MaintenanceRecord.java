package digitales_hausbuch_backend.maintenance;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate completedAt;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_task_id", nullable = false)
    private MaintenanceTask maintenanceTask;

    protected MaintenanceRecord() {
    }

    public MaintenanceRecord(
            LocalDate completedAt,
            String note,
            MaintenanceTask maintenanceTask
    ) {
        this.completedAt = completedAt;
        this.note = note;
        this.maintenanceTask = maintenanceTask;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public String getNote() {
        return note;
    }

    public MaintenanceTask getMaintenanceTask() {
        return maintenanceTask;
    }
}