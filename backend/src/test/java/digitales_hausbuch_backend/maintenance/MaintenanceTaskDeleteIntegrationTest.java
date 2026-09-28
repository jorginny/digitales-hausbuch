package digitales_hausbuch_backend.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import digitales_hausbuch_backend.user.UserRepository;
import digitales_hausbuch_backend.property.PropertyRepository;
import digitales_hausbuch_backend.room.RoomRepository;
import digitales_hausbuch_backend.householdobject.HouseholdObjectRepository;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceTaskDeleteIntegrationTest {

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
    void setUp() {

        maintenanceRecordRepository.deleteAll();
        maintenanceTaskRepository.deleteAll();
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldDeleteMaintenanceTaskWithoutHistory()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "delete-task@test.de",
                        "password123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Testhaus"
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

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        "Heizung prüfen"
                );

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isPresent();

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isEmpty();
    }

    @Test
    void shouldDeleteMaintenanceTaskAndItsHistory()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "delete-history@test.de",
                        "password123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Testhaus"
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

        Long taskId =
                createMaintenanceTask(
                        session,
                        propertyId,
                        roomId,
                        objectId,
                        "Heizung warten"
                );

        completeMaintenanceTask(
                session,
                propertyId,
                roomId,
                objectId,
                taskId,
                "Wartung durchgeführt"
        );

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(
                                taskId
                        )
        ).isTrue();

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isEmpty();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(
                                taskId
                        )
        ).isFalse();
    }

    @Test
    void shouldNotDeleteMaintenanceTaskFromForeignProperty()
            throws Exception {

        MockHttpSession ownerSession =
                registerAndLogin(
                        "owner-delete@test.de",
                        "password123"
                );

        Long propertyId =
                createProperty(
                        ownerSession,
                        "Haus Eigentümer"
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
                        "Heizung prüfen"
                );

        MockHttpSession foreignSession =
                registerAndLogin(
                        "foreign-delete@test.de",
                        "password123"
                );

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(
                                        foreignSession
                                )
                )
                .andExpect(
                        status().isNotFound()
                );

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isPresent();
    }

    private MockHttpSession registerAndLogin(
            String email,
            String password
    ) throws Exception {

        String registerJson =
                """
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
                                .content(
                                        registerJson
                                )
                )
                .andExpect(
                        status().isCreated()
                );

        String loginJson =
                """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(
                        email,
                        password
                );

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                loginJson
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession)
                        loginResult
                                .getRequest()
                                .getSession(false);

        assertThat(session)
                .isNotNull();

        return session;
    }

    private Long createProperty(
            MockHttpSession session,
            String name
    ) throws Exception {

        String json =
                """
                {
                  "name": "%s",
                  "address": "Teststraße 1"
                }
                """.formatted(name);

        MvcResult result =
                mockMvc.perform(
                                post("/api/properties")
                                        .session(session)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(json)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result
                                .getResponse()
                                .getContentAsString()
                );

        return response
                .get("id")
                .asLong();
    }

    private Long createRoom(
            MockHttpSession session,
            Long propertyId,
            String name
    ) throws Exception {

        String json =
                """
                {
                  "name": "%s"
                }
                """.formatted(name);

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
                                        .content(json)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result
                                .getResponse()
                                .getContentAsString()
                );

        return response
                .get("id")
                .asLong();
    }

    private Long createHouseholdObject(
            MockHttpSession session,
            Long propertyId,
            Long roomId,
            String name
    ) throws Exception {

        String json =
                """
                {
                  "name": "%s",
                  "description": "Testobjekt",
                  "manufacturer": "Testhersteller",
                  "model": "Testmodell"
                }
                """.formatted(name);

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
                                        .content(json)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result
                                .getResponse()
                                .getContentAsString()
                );

        return response
                .get("id")
                .asLong();
    }

    private Long createMaintenanceTask(
            MockHttpSession session,
            Long propertyId,
            Long roomId,
            Long objectId,
            String title
    ) throws Exception {

        String json =
                """
                {
                  "title": "%s",
                  "description": "Testwartung",
                  "dueDate": "2027-01-15"
                }
                """.formatted(title);

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
                                        .content(json)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result
                                .getResponse()
                                .getContentAsString()
                );

        return response
                .get("id")
                .asLong();
    }

    private void completeMaintenanceTask(
            MockHttpSession session,
            Long propertyId,
            Long roomId,
            Long objectId,
            Long taskId,
            String note
    ) throws Exception {

        String json =
                """
                {
                  "note": "%s"
                }
                """.formatted(note);

        mockMvc.perform(
                        put(
                                "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks/{taskId}/complete",
                                propertyId,
                                roomId,
                                objectId,
                                taskId
                        )
                                .session(session)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isOk()
                );
    }
}