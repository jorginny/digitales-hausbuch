package digitales_hausbuch_backend.room;

import digitales_hausbuch_backend.property.PropertyRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateRoomForOwnProperty() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Mein Haus",
          "address": "Musterstraße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId = propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Wohnzimmer"))
                .andExpect(jsonPath("$.propertyId").value(propertyId));

        assert(roomRepository.count() == 1);
    }

    @Test
    void shouldReturnRoomsForOwnProperty() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Mein Haus",
          "address": "Musterstraße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId = propertyRepository.findAll().get(0).getId();

        String roomOne = """
        {
          "name": "Wohnzimmer"
        }
        """;

        String roomTwo = """
        {
          "name": "Küche"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomOne))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomTwo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void shouldNotAllowCreatingRoomForForeignProperty() throws Exception {

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

        // User 1 registrieren
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isCreated());

        // User 1 einloggen
        MvcResult userOneLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userOneRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userOneSession =
                (MockHttpSession) userOneLogin.getRequest().getSession(false);

        // Property für User 1 anlegen
        String propertyRequest = """
        {
          "name": "Haus von User 1",
          "address": "Straße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        // User 2 registrieren
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isCreated());

        // User 2 einloggen
        MvcResult userTwoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userTwoRequest))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession userTwoSession =
                (MockHttpSession) userTwoLogin.getRequest().getSession(false);

        String roomRequest = """
        {
          "name": "Fremdes Wohnzimmer"
        }
        """;

        // User 2 versucht Raum an Property von User 1 anzulegen
        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isNotFound());

        assert(roomRepository.count() == 0);
    }

    @Test
    void shouldNotAllowReadingRoomsFromForeignProperty() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Haus von User 1",
          "address": "Straße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

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

        mockMvc.perform(get("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateOwnRoom() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Mein Haus",
          "address": "Musterstraße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        String updateRequest = """
        {
          "name": "Esszimmer"
        }
        """;

        mockMvc.perform(put("/api/properties/{propertyId}/rooms/{roomId}",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId))
                .andExpect(jsonPath("$.name").value("Esszimmer"))
                .andExpect(jsonPath("$.propertyId").value(propertyId));

        Room updatedRoom =
                roomRepository.findById(roomId).orElseThrow();

        assertEquals("Esszimmer", updatedRoom.getName());
    }

    @Test
    void shouldDeleteOwnRoom() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Mein Haus",
          "address": "Musterstraße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/properties/{propertyId}/rooms/{roomId}",
                        propertyId,
                        roomId)
                        .session(session))
                .andExpect(status().isNoContent());

        assertEquals(0, roomRepository.count());
    }

    @Test
    void shouldNotAllowUpdatingRoomFromForeignProperty() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Haus von User 1",
          "address": "Straße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

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

        String updateRequest = """
        {
          "name": "Manipulierter Raum"
        }
        """;

        mockMvc.perform(put("/api/properties/{propertyId}/rooms/{roomId}",
                        propertyId,
                        roomId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isNotFound());

        Room room = roomRepository.findById(roomId).orElseThrow();

        assertEquals("Wohnzimmer", room.getName());
    }

    @Test
    void shouldNotAllowDeletingRoomFromForeignProperty() throws Exception {

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

        String propertyRequest = """
        {
          "name": "Haus von User 1",
          "address": "Straße 1"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String roomRequest = """
        {
          "name": "Wohnzimmer"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(userOneSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

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

        mockMvc.perform(delete("/api/properties/{propertyId}/rooms/{roomId}",
                        propertyId,
                        roomId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());

        assertEquals(1, roomRepository.count());
    }
}
