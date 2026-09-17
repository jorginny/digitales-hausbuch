package digitales_hausbuch_backend.householdobject;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HouseholdObjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
        householdObjectRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateHouseholdObjectForOwnRoom() throws Exception {

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
          "name": "Keller"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        String objectRequest = """
        {
          "name": "Waschmaschine",
          "description": "Waschmaschine im Keller",
          "manufacturer": "Bosch",
          "model": "Serie 6",
          "purchaseDate": "2024-03-15"
        }
        """;

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Waschmaschine"))
                .andExpect(jsonPath("$.manufacturer").value("Bosch"))
                .andExpect(jsonPath("$.model").value("Serie 6"))
                .andExpect(jsonPath("$.purchaseDate").value("2024-03-15"))
                .andExpect(jsonPath("$.roomId").value(roomId));

        assertEquals(1, householdObjectRepository.count());
    }

    @Test
    void shouldRejectHouseholdObjectWithoutName() throws Exception {

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
          "name": "Mein Haus"
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
          "name": "Keller"
        }
        """;

        mockMvc.perform(post("/api/properties/{propertyId}/rooms", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomRequest))
                .andExpect(status().isCreated());

        Long roomId =
                roomRepository.findAll().get(0).getId();

        String invalidObjectRequest = """
        {
          "name": ""
        }
        """;

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidObjectRequest))
                .andExpect(status().isBadRequest());

        assertEquals(0, householdObjectRepository.count());
    }

    @Test
    void shouldNotAllowCreatingHouseholdObjectForForeignRoom() throws Exception {

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
          "name": "Haus von User 1"
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
          "name": "Keller"
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

        String objectRequest = """
        {
          "name": "Fremde Waschmaschine"
        }
        """;

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectRequest))
                .andExpect(status().isNotFound());

        assertEquals(0, householdObjectRepository.count());
    }
}
