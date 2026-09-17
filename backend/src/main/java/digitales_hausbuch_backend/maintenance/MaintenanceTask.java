package digitales_hausbuch_backend.maintenance;

import digitales_hausbuch_backend.householdobject.HouseholdObject;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_object_id", nullable = false)
    private HouseholdObject householdObject;

    public MaintenanceTask() {
    }

    public MaintenanceTask(
            String title,
            String description,
            LocalDate dueDate,
            HouseholdObject householdObject) {

        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.householdObject = householdObject;
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public HouseholdObject getHouseholdObject() {
        return householdObject;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
