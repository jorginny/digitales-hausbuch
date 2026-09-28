package digitales_hausbuch_backend.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import digitales_hausbuch_backend.householdobject.HouseholdObjectRepository;
import digitales_hausbuch_backend.maintenance.MaintenanceRecordRepository;
import digitales_hausbuch_backend.maintenance.MaintenanceTaskRepository;
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyDeleteIntegrationTest {

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
    void cleanDatabase() {
        maintenanceRecordRepository.deleteAll();
        maintenanceTaskRepository.deleteAll();
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldDeleteEmptyProperty()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "delete-property@test.de",
                        "password123"
                );

        Long propertyId =
                createProperty(
                        session,
                        "Testhaus"
                );

        assertThat(
                propertyRepository.findById(propertyId)
        ).isPresent();

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        assertThat(
                propertyRepository.findById(propertyId)
        ).isEmpty();
    }

    @Test
    void shouldDeletePropertyWithCompleteHierarchy()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "delete-property-hierarchy@test.de",
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
                propertyRepository.findById(propertyId)
        ).isPresent();

        assertThat(
                roomRepository.findById(roomId)
        ).isPresent();

        assertThat(
                householdObjectRepository
                        .findById(objectId)
        ).isPresent();

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isPresent();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(taskId)
        ).isTrue();

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}",
                                propertyId
                        )
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        assertThat(
                propertyRepository.findById(propertyId)
        ).isEmpty();

        assertThat(
                roomRepository.findById(roomId)
        ).isEmpty();

        assertThat(
                householdObjectRepository
                        .findById(objectId)
        ).isEmpty();

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isEmpty();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(taskId)
        ).isFalse();
    }

    @Test
    void shouldNotDeleteForeignProperty()
            throws Exception {

        MockHttpSession ownerSession =
                registerAndLogin(
                        "property-owner@test.de",
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

        completeMaintenanceTask(
                ownerSession,
                propertyId,
                roomId,
                objectId,
                taskId,
                "Durchgeführt"
        );

        MockHttpSession foreignSession =
                registerAndLogin(
                        "property-foreign@test.de",
                        "password123"
                );

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}",
                                propertyId
                        )
                                .session(foreignSession)
                )
                .andExpect(
                        status().isNotFound()
                );

        assertThat(
                propertyRepository.findById(propertyId)
        ).isPresent();

        assertThat(
                roomRepository.findById(roomId)
        ).isPresent();

        assertThat(
                householdObjectRepository
                        .findById(objectId)
        ).isPresent();

        assertThat(
                maintenanceTaskRepository
                        .findById(taskId)
        ).isPresent();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(taskId)
        ).isTrue();
    }

    @Test
    void shouldKeepSecondPropertyOfSameUser()
            throws Exception {

        MockHttpSession session =
                registerAndLogin(
                        "two-properties@test.de",
                        "password123"
                );

        Long propertyToDeleteId =
                createProperty(
                        session,
                        "Haus 1"
                );

        Long roomToDeleteId =
                createRoom(
                        session,
                        propertyToDeleteId,
                        "Keller"
                );

        Long objectToDeleteId =
                createHouseholdObject(
                        session,
                        propertyToDeleteId,
                        roomToDeleteId,
                        "Heizung Haus 1"
                );

        Long taskToDeleteId =
                createMaintenanceTask(
                        session,
                        propertyToDeleteId,
                        roomToDeleteId,
                        objectToDeleteId,
                        "Wartung Haus 1"
                );

        completeMaintenanceTask(
                session,
                propertyToDeleteId,
                roomToDeleteId,
                objectToDeleteId,
                taskToDeleteId,
                "Haus 1 erledigt"
        );

        Long secondPropertyId =
                createProperty(
                        session,
                        "Haus 2"
                );

        Long secondRoomId =
                createRoom(
                        session,
                        secondPropertyId,
                        "Garage"
                );

        Long secondObjectId =
                createHouseholdObject(
                        session,
                        secondPropertyId,
                        secondRoomId,
                        "Wallbox"
                );

        Long secondTaskId =
                createMaintenanceTask(
                        session,
                        secondPropertyId,
                        secondRoomId,
                        secondObjectId,
                        "Wallbox prüfen"
                );

        completeMaintenanceTask(
                session,
                secondPropertyId,
                secondRoomId,
                secondObjectId,
                secondTaskId,
                "Haus 2 erledigt"
        );

        mockMvc.perform(
                        delete(
                                "/api/properties/{propertyId}",
                                propertyToDeleteId
                        )
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        // Erste Property und ihre Daten sind gelöscht.

        assertThat(
                propertyRepository
                        .findById(propertyToDeleteId)
        ).isEmpty();

        assertThat(
                roomRepository
                        .findById(roomToDeleteId)
        ).isEmpty();

        assertThat(
                householdObjectRepository
                        .findById(objectToDeleteId)
        ).isEmpty();

        assertThat(
                maintenanceTaskRepository
                        .findById(taskToDeleteId)
        ).isEmpty();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(
                                taskToDeleteId
                        )
        ).isFalse();

        // Zweite Property und ihre Daten müssen bleiben.

        assertThat(
                propertyRepository
                        .findById(secondPropertyId)
        ).isPresent();

        assertThat(
                roomRepository
                        .findById(secondRoomId)
        ).isPresent();

        assertThat(
                householdObjectRepository
                        .findById(secondObjectId)
        ).isPresent();

        assertThat(
                maintenanceTaskRepository
                        .findById(secondTaskId)
        ).isPresent();

        assertThat(
                maintenanceRecordRepository
                        .existsByMaintenanceTask_Id(
                                secondTaskId
                        )
        ).isTrue();
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
                                .content(registerJson)
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
                                        .content(loginJson)
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

        assertThat(session).isNotNull();

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
