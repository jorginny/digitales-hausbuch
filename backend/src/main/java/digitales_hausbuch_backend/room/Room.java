package digitales_hausbuch_backend.room;

import digitales_hausbuch_backend.property.Property;
import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    public Room() {
    }

    public Room(String name, Property property) {
        this.name = name;
        this.property = property;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Property getProperty() {
        return property;
    }

    public void setName(String name) {
        this.name = name;
    }
}
