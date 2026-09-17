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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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

    @Test
    void shouldReturnHouseholdObjectsForOwnRoom() throws Exception {

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

        String objectOne = """
        {
          "name": "Waschmaschine"
        }
        """;

        String objectTwo = """
        {
          "name": "Trockner"
        }
        """;

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectOne))
                .andExpect(status().isCreated());

        mockMvc.perform(post(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectTwo))
                .andExpect(status().isCreated());

        mockMvc.perform(get(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void shouldUpdateOwnHouseholdObject() throws Exception {

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
                          "name": "Waschmaschine",
                          "manufacturer": "Bosch",
                          "model": "Serie 6"
                        }
                        """))
                .andExpect(status().isCreated());

        Long objectId =
                householdObjectRepository.findAll().get(0).getId();

        String updateRequest = """
        {
          "name": "Waschmaschine Neu",
          "description": "Neue Beschreibung",
          "manufacturer": "Bosch",
          "model": "Serie 8",
          "purchaseDate": "2025-01-10"
        }
        """;

        mockMvc.perform(put(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(objectId))
                .andExpect(jsonPath("$.name").value("Waschmaschine Neu"))
                .andExpect(jsonPath("$.model").value("Serie 8"))
                .andExpect(jsonPath("$.purchaseDate").value("2025-01-10"));

        HouseholdObject updatedObject =
                householdObjectRepository.findById(objectId).orElseThrow();

        assertEquals(
                "Waschmaschine Neu",
                updatedObject.getName()
        );

        assertEquals(
                "Serie 8",
                updatedObject.getModel()
        );
    }

    @Test
    void shouldNotAllowReadingHouseholdObjectsFromForeignRoom() throws Exception {

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

        mockMvc.perform(get(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects",
                        propertyId,
                        roomId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUpdatingForeignHouseholdObject() throws Exception {

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
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}",
                        propertyId,
                        roomId,
                        objectId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Manipulierte Waschmaschine"
                        }
                        """))
                .andExpect(status().isNotFound());

        HouseholdObject unchangedObject =
                householdObjectRepository.findById(objectId).orElseThrow();

        assertEquals(
                "Waschmaschine",
                unchangedObject.getName()
        );
    }

    @Test
    void shouldDeleteOwnHouseholdObject() throws Exception {

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

        mockMvc.perform(delete(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}",
                        propertyId,
                        roomId,
                        objectId)
                        .session(session))
                .andExpect(status().isNoContent());

        assertEquals(0, householdObjectRepository.count());
    }

    @Test
    void shouldNotAllowDeletingForeignHouseholdObject() throws Exception {

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

        // User 1 registrieren und einloggen
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

        // Property von User 1
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

        // Raum von User 1
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

        // Objekt von User 1
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

        // User 2 registrieren und einloggen
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

        // User 2 versucht das Objekt von User 1 zu löschen
        mockMvc.perform(delete(
                        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}",
                        propertyId,
                        roomId,
                        objectId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());

        assertEquals(1, householdObjectRepository.count());
    }


}
