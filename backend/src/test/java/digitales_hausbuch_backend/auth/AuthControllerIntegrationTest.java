package digitales_hausbuch_backend.auth;

import digitales_hausbuch_backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String requestBody = """
                {
                  "email": "test@example.de",
                  "password": "MeinPasswort123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        assertTrue(userRepository.existsByEmail("test@example.de"));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {

        String requestBody = """
            {
              "email": "keine-gueltige-email",
              "password": "123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        String requestBody = """
            {
              "email": "test@example.de",
              "password": "MeinPasswort123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginValidUserSuccessfully() throws Exception{

        String requestBody = """
            {
              "email": "test@example.de",
              "password": "MeinPasswort123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturnUnauthorizedWhenWrongPasswordUsed() throws Exception{

        String requestBody = """
            {
              "email": "test@example.de",
              "password": "MeinPasswort123"
            }
            """;

        String requestBodyWrong = """
            {
              "email": "test@example.de",
              "password": "MeinPasswort321"
            }
            """;

        mockMvc.perform(post("/api/auth/register")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyWrong))
                .andExpect(status().isUnauthorized());


    }

    @Test
    void shouldReturnUnauthorizedWhenUnknownEmailUsed() throws Exception{

        String requestBody = """
            {
              "email": "test@example.de",
              "password": "MeinPasswort123"
            }
            """;

        String requestBodyWrong = """
            {
              "email": "test1@example.de",
              "password": "MeinPasswort123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyWrong))
                .andExpect(status().isUnauthorized());


    }

    @Test
    void shouldReturnCurrentUserWhenLoggedIn() throws Exception {

        String requestBody = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.de"));
    }

    @Test
    void shouldRejectMeEndpointWhenNotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLogoutValidUserSuccessfully() throws Exception {

        String requestBody = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLogoutUserAndInvalidateSession() throws Exception {

        String requestBody = """
        {
          "email": "test@example.de",
          "password": "MeinPasswort123"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isForbidden());
    }
}