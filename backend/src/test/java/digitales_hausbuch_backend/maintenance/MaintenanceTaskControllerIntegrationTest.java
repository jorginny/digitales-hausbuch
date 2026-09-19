package digitales_hausbuch_backend.maintenance;

import digitales_hausbuch_backend.householdobject.HouseholdObjectRepository;
import digitales_hausbuch_backend.property.PropertyRepository;
import digitales_hausbuch_backend.room.RoomRepository;
import digitales_hausbuch_backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceTaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaintenanceTaskRepository maintenanceTaskRepository;

    @Autowired
    private HouseholdObjectRepository householdObjectRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        maintenanceTaskRepository.deleteAll();
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateMaintenanceTaskForOwnHouseholdObject() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Waschmaschine"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        String taskRequest = """
        {
          "title": "Filter reinigen",
          "description": "Flusensieb kontrollieren und reinigen",
          "dueDate": "2026-10-01"
        }
        """;

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Filter reinigen"))
                .andExpect(jsonPath("$.description")
                        .value("Flusensieb kontrollieren und reinigen"))
                .andExpect(jsonPath("$.dueDate").value("2026-10-01"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.householdObjectId").value(objectId));

        assertEquals(1, maintenanceTaskRepository.count());
    }

    @Test
    void shouldReturnMaintenanceTasksForOwnHouseholdObject() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Wartung durchführen"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Druck kontrollieren"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @Test
    void shouldRejectMaintenanceTaskWithoutTitle() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Waschmaschine"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertEquals(0, maintenanceTaskRepository.count());
    }

    @Test
    void shouldNotAllowCreatingMaintenanceTaskForForeignHouseholdObject()
            throws Exception {

        String userOneRequest = """
        {
          "email": "user1@example.de",
          "password": "MeinPasswort123"
        }
        """;

        String userTwoRequest = """
        {
          "email": "user2@example.de",
          "password": "MeinPasswort123"
        }
        """;

        // User 1
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isCreated());

        MvcResult userOneLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userOneSession =
                (MockHttpSession) userOneLogin.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Haus von User 1"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        // User 2
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isCreated());

        MvcResult userTwoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userTwoSession =
                (MockHttpSession) userTwoLogin.getRequest().getSession(false);

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Fremde Wartung"
                        }
                        """))
                .andExpect(status().isNotFound());

        assertEquals(0, maintenanceTaskRepository.count());
    }

    @Test
    void shouldCreateRecurringMaintenanceTask() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Heizung warten",
                          "dueDate": "2026-10-01",
                          "recurrenceInterval": 12,
                          "recurrenceUnit": "MONTHS"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceInterval").value(12))
                .andExpect(jsonPath("$.recurrenceUnit").value("MONTHS"));
    }

    @Test
    void shouldUpdateRecurringMaintenanceTask() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Heizung warten",
                          "recurrenceInterval": 12,
                          "recurrenceUnit": "MONTHS"
                        }
                        """))
                .andExpect(status().isCreated());

        Long taskId =
                maintenanceTaskRepository.findAll().get(0).getId();

        mockMvc.perform(put(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}",
                        propertyId,
                        roomId,
                        objectId,
                        taskId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Heizung jährlich warten",
                          "recurrenceInterval": 1,
                          "recurrenceUnit": "YEARS"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Heizung jährlich warten"))
                .andExpect(jsonPath("$.recurrenceInterval").value(1))
                .andExpect(jsonPath("$.recurrenceUnit").value("YEARS"));

        MaintenanceTask updatedTask =
                maintenanceTaskRepository.findById(taskId).orElseThrow();

        assertEquals(1, updatedTask.getRecurrenceInterval());
        assertEquals(
                RecurrenceUnit.YEARS,
                updatedTask.getRecurrenceUnit()
        );
    }

    @Test
    void shouldRejectNonPositiveRecurrenceInterval() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();


        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Filter reinigen",
                          "recurrenceInterval": 0,
                          "recurrenceUnit": "MONTHS"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRecurrenceIntervalWithoutUnit() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Heizung warten",
                          "recurrenceInterval": 12,
                          "recurrenceUnit": "MONTHS"
                        }
                        """))
                .andExpect(status().isCreated());

        Long taskId =
                maintenanceTaskRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Filter reinigen",
                          "recurrenceInterval": 6
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCompleteOneTimeMaintenanceTask() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Waschmaschine"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Filter reinigen",
                          "dueDate": "2026-10-01"
                        }
                        """))
                .andExpect(status().isCreated());

        Long taskId =
                maintenanceTaskRepository.findAll().get(0).getId();

        mockMvc.perform(put(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                        propertyId,
                        roomId,
                        objectId,
                        taskId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").exists());

        MaintenanceTask completedTask =
                maintenanceTaskRepository.findById(taskId).orElseThrow();

        assertEquals(true, completedTask.isCompleted());
    }

    @Test
    void shouldScheduleNextDueDateForRecurringMaintenanceTask() throws Exception {

        String userRequest = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Mein Haus"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizungsraum"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Heizung"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Heizung warten",
                          "dueDate": "2026-10-01",
                          "recurrenceInterval": 12,
                          "recurrenceUnit": "MONTHS"
                        }
                        """))
                .andExpect(status().isCreated());

        Long taskId =
                maintenanceTaskRepository.findAll().get(0).getId();

        mockMvc.perform(put(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                        propertyId,
                        roomId,
                        objectId,
                        taskId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.completedAt").exists())
                .andExpect(jsonPath("$.dueDate").value("2027-10-01"));

        MaintenanceTask task =
                maintenanceTaskRepository.findById(taskId).orElseThrow();

        assertEquals(false, task.isCompleted());
        assertEquals(
                LocalDate.of(2027, 10, 1),
                task.getDueDate()
        );
    }

    @Test
    void shouldNotAllowCompletingForeignMaintenanceTask() throws Exception {

        String userOneRequest = """
        {
          "email": "user1@example.de",
          "password": "MeinPasswort123"
        }
        """;

        String userTwoRequest = """
        {
          "email": "user2@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isCreated());

        MvcResult userOneLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userOneSession =
                (MockHttpSession) userOneLogin.getRequest().getSession(false);

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Haus von User 1"
                        }
                        """))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Keller"
                        }
                        """))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Waschmaschine"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                        propertyId,
                        roomId,
                        objectId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Filter reinigen"
                        }
                        """))
                .andExpect(status().isCreated());

        Long taskId =
                maintenanceTaskRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isCreated());

        MvcResult userTwoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userTwoSession =
                (MockHttpSession) userTwoLogin.getRequest().getSession(false);

        mockMvc.perform(put(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                        propertyId,
                        roomId,
                        objectId,
                        taskId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());

        MaintenanceTask unchangedTask =
                maintenanceTaskRepository.findById(taskId).orElseThrow();

        assertEquals(false, unchangedTask.isCompleted());
    }
}
