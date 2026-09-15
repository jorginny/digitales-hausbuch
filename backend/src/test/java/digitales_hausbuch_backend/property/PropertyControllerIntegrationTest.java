package digitales_hausbuch_backend.property;

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
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldCreatePropertyForLoggedInUser() throws Exception {

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
          "address": "Musterstraße 12"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mein Haus"))
                .andExpect(jsonPath("$.address").value("Musterstraße 12"));

        Property savedProperty = propertyRepository.findAll().get(0);

        assertEquals(
                "test@example.de",
                savedProperty.getOwner().getEmail()
        );
    }

    @Test
    void shouldRejectPropertyCreationWhenUserIsNotLoggedIn() throws Exception {

        String propertyRequest = """
        {
          "name": "Mein Haus",
          "address": "Musterstraße 12"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnOwnProperty() throws Exception {

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
          "address": "Musterstraße 12"
        }
        """;

        MvcResult propertyResult = mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated())
                .andReturn();

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/properties/{id}", propertyId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propertyId))
                .andExpect(jsonPath("$.name").value("Mein Haus"))
                .andExpect(jsonPath("$.address").value("Musterstraße 12"));
    }

    @Test
    void shouldUpdateOwnProperty() throws Exception {

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
          "address": "Musterstraße 12"
        }
        """;

        mockMvc.perform(post("/api/properties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequest))
                .andExpect(status().isCreated());

        Long propertyId =
                propertyRepository.findAll().get(0).getId();

        String updateRequest = """
        {
          "name": "Unser Haus",
          "address": "Neue Straße 5"
        }
        """;

        mockMvc.perform(put("/api/properties/{id}", propertyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Unser Haus"))
                .andExpect(jsonPath("$.address").value("Neue Straße 5"));
    }

    @Test
    void shouldNotAllowAccessToForeignProperty() throws Exception {

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

        // User 1 registrieren + einloggen
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

        // User 2 registrieren + einloggen
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

        // User 2 versucht, Property von User 1 zu laden
        mockMvc.perform(get("/api/properties/{id}", propertyId)
                        .session(userTwoSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUpdatingForeignProperty() throws Exception {

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
          "name": "Manipuliertes Haus",
          "address": "Fremde Straße 99"
        }
        """;

        mockMvc.perform(put("/api/properties/{id}", propertyId)
                        .session(userTwoSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isNotFound());
    }


}
