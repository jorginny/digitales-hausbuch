package digitales_hausbuch_backend.maintenance;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

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
    void cleanUp() {

        maintenanceRecordRepository.deleteAll();
        maintenanceTaskRepository.deleteAll();
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateHistoryEntryWhenMaintenanceTaskIsCompleted()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "user@example.de",
                        "MeinPasswort123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Mein Haus"
                );

        Long roomId =
                createRoom(
                        session,
                        propertyId,
                        "Keller"
                );

        Long objectId =
                createHouseholdObject(
                        session,
                        propertyId,
                        roomId,
                        "Waschmaschine"
                );

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        """
                        {
                          "title": "Filter reinigen",
                          "dueDate": "2026-10-01"
                        }
                        """
                );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/history",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].maintenanceTaskId")
                                .value(taskId)
                )
                .andExpect(
                        jsonPath("$[0].completedAt")
                                .exists()
                );

        long recordCount =
                maintenanceRecordRepository.count();

        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                recordCount
        );
    }

    @Test
    void shouldCreateMultipleHistoryEntriesForRecurringTask()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "user@example.de",
                        "MeinPasswort123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Mein Haus"
                );

        Long roomId =
                createRoom(
                        session,
                        propertyId,
                        "Heizungsraum"
                );

        Long objectId =
                createHouseholdObject(
                        session,
                        propertyId,
                        roomId,
                        "Heizung"
                );

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        """
                        {
                          "title": "Heizung warten",
                          "dueDate": "2026-10-01",
                          "recurrenceInterval": 12,
                          "recurrenceUnit": "MONTHS"
                        }
                        """
                );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.completed")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.dueDate")
                                .value("2027-10-01")
                );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.completed")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.dueDate")
                                .value("2028-10-01")
                );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/history",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].maintenanceTaskId")
                                .value(taskId)
                )
                .andExpect(
                        jsonPath("$[1].maintenanceTaskId")
                                .value(taskId)
                );

        long recordCount =
                maintenanceRecordRepository.count();

        org.junit.jupiter.api.Assertions.assertEquals(
                2,
                recordCount
        );
    }

    @Test
    void shouldNotAllowAccessToHistoryOfForeignMaintenanceTask()
            throws Exception {

        MockHttpSession ownerSession =
                registerAndLogin(
                        "owner@example.de",
                        "MeinPasswort123"
                );

        Long propertyId =
                createProperty(
                        ownerSession,
                        "Haus des Besitzers"
                );

        Long roomId =
                createRoom(
                        ownerSession,
                        propertyId,
                        "Keller"
                );

        Long objectId =
                createHouseholdObject(
                        ownerSession,
                        propertyId,
                        roomId,
                        "Heizung"
                );

        Long taskId =
                createMaintenanceTask(
                        ownerSession,
                        propertyId,
                        roomId,
                        objectId,
                        """
                        {
                          "title": "Heizung warten",
                          "dueDate": "2026-10-01"
                        }
                        """
                );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(ownerSession)
                )
                .andExpect(status().isOk());

        MockHttpSession foreignSession =
                registerAndLogin(
                        "other@example.de",
                        "MeinPasswort123"
                );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/history",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(foreignSession)
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void shouldReturnEmptyHistoryWhenTaskWasNeverCompleted()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "user@example.de",
                        "MeinPasswort123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Mein Haus"
                );

        Long roomId =
                createRoom(
                        session,
                        propertyId,
                        "Keller"
                );

        Long objectId =
                createHouseholdObject(
                        session,
                        propertyId,
                        roomId,
                        "Waschmaschine"
                );

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        """
                        {
                          "title": "Filter reinigen",
                          "dueDate": "2026-10-01"
                        }
                        """
                );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/history",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );
    }

    @Test
    void shouldSaveNoteInMaintenanceHistory()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "user@example.de",
                        "MeinPasswort123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Mein Haus"
                );

        Long roomId =
                createRoom(
                        session,
                        propertyId,
                        "Keller"
                );

        Long objectId =
                createHouseholdObject(
                        session,
                        propertyId,
                        roomId,
                        "Waschmaschine"
                );

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        """
                        {
                          "title": "Filter reinigen",
                          "dueDate": "2026-10-01"
                        }
                        """
                );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "note": "Filter gereinigt und Dichtung geprüft."
                                }
                            """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/history",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].note")
                                .value(
                                        "Filter gereinigt und Dichtung geprüft."
                                )
                );
    }

    private MockHttpSession registerAndLogin(
            String email,
            String password
    ) throws Exception {

        String request = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(
                email,
                password
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(
                        status().isCreated()
                );

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(request)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        return (MockHttpSession)
                loginResult
                        .getRequest()
                        .getSession(false);
    }

    private Long createProperty(
            MockHttpSession session,
            String name
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post("/api/properties")
                                        .session(session)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {
                                                  "name": "%s"
                                                }
                                                """.formatted(name)
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return response.get("id").asLong();
    }

    private Long createRoom(
            MockHttpSession session,
            Long propertyId,
            String name
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post(
                                        "/api/properties/{propertyId}/rooms",
                                        propertyId
                                )
                                        .session(session)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {
                                                  "name": "%s"
                                                }
                                                """.formatted(name)
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return response.get("id").asLong();
    }

    private Long createHouseholdObject(
            MockHttpSession session,
            Long propertyId,
            Long roomId,
            String name
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post(
                                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                                        propertyId,
                                        roomId
                                )
                                        .session(session)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {
                                                  "name": "%s"
                                                }
                                                """.formatted(name)
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return response.get("id").asLong();
    }

    private Long createMaintenanceTask(
            MockHttpSession session,
            Long propertyId,
            Long roomId,
            Long objectId,
            String requestBody
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post(
                                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks",
                                        propertyId,
                                        roomId,
                                        objectId
                                )
                                        .session(session)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(requestBody)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return response.get("id").asLong();
    }
}