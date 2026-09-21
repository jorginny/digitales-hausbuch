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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceOverviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        maintenanceTaskRepository.deleteAll();
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnOpenMaintenanceTasksFromDifferentRoomsAndObjects()
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

        Long kitchenId =
                createRoom(
                        session,
                        propertyId,
                        "Küche"
                );

        Long basementId =
                createRoom(
                        session,
                        propertyId,
                        "Keller"
                );

        Long dishwasherId =
                createHouseholdObject(
                        session,
                        propertyId,
                        kitchenId,
                        "Spülmaschine"
                );

        Long heatingId =
                createHouseholdObject(
                        session,
                        propertyId,
                        basementId,
                        "Heizung"
                );

        createMaintenanceTask(
                session,
                propertyId,
                kitchenId,
                dishwasherId,
                """
                {
                  "title": "Filter reinigen",
                  "dueDate": "2026-10-01"
                }
                """
        );

        createMaintenanceTask(
                session,
                propertyId,
                basementId,
                heatingId,
                """
                {
                  "title": "Heizung warten",
                  "dueDate": "2026-11-01",
                  "recurrenceInterval": 12,
                  "recurrenceUnit": "MONTHS"
                }
                """
        );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(
                        jsonPath(
                                "$[?(@.title == 'Filter reinigen')]"
                        ).exists()
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.title == 'Heizung warten')]"
                        ).exists()
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.objectName == 'Spülmaschine')]"
                        ).exists()
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.objectName == 'Heizung')]"
                        ).exists()
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.roomName == 'Küche')]"
                        ).exists()
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.roomName == 'Keller')]"
                        ).exists()
                );
    }

    @Test
    void shouldNotReturnCompletedOneTimeMaintenanceTask()
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

        Long completedTaskId =
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

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Maschine prüfen",
                  "dueDate": "2026-12-01"
                }
                """
        );

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                completedTaskId
                        )
                                .session(session)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$[0].title")
                                .value("Maschine prüfen")
                )

                .andExpect(
                        jsonPath(
                                "$[?(@.title == 'Filter reinigen')]"
                        ).doesNotExist()
                );
    }

    @Test
    void shouldReturnNotFoundForForeignProperty()
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

        MockHttpSession foreignSession =
                registerAndLogin(
                        "other@example.de",
                        "MeinPasswort123"
                );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
                        )
                                .session(foreignSession)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyListWhenNoOpenMaintenanceTasksExist()
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

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
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
    void shouldReturnOpenMaintenanceTasksSortedByDueDateAscending()
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
                        "Heizung"
                );

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Späte Aufgabe",
                  "dueDate": "2026-12-01"
                }
                """
        );

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Frühe Aufgabe",
                  "dueDate": "2026-10-01"
                }
                """
        );

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Mittlere Aufgabe",
                  "dueDate": "2026-11-01"
                }
                """
        );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Frühe Aufgabe")
                )
                .andExpect(
                        jsonPath("$[1].title")
                                .value("Mittlere Aufgabe")
                )
                .andExpect(
                        jsonPath("$[2].title")
                                .value("Späte Aufgabe")
                );
    }

    @Test
    void shouldReturnTasksWithoutDueDateAtTheEnd()
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

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Ohne Termin"
                }
                """
        );

        createMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                """
                {
                  "title": "Mit Termin",
                  "dueDate": "2026-10-01"
                }
                """
        );

        mockMvc.perform(
                        get(
                                "/api/properties/{propertyId}/maintenance-tasks",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Mit Termin")
                )
                .andExpect(
                        jsonPath("$[1].title")
                                .value("Ohne Termin")
                )
                .andExpect(
                        jsonPath("$[1].dueDate")
                                .doesNotExist()
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
