package it.sara.demo.security;


import it.sara.demo.exception.GlobalExceptionHandler;
import it.sara.demo.service.database.FakeDatabase;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.util.JwtUtil;
import it.sara.demo.web.auth.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setupDb() {
        FakeDatabase.TABLE_USER.clear();

        User u = new User();
        u.setGuid(UUID.randomUUID().toString());
        u.setFirstName("Martina");
        u.setLastName("Luciani");
        u.setEmail("martina@test.it");
        u.setPhoneNumber("+393331112233");
        FakeDatabase.TABLE_USER.add(u);
    }

    @Test
    void login_success() throws Exception {
        when(jwtUtil.generateToken("martina@test.it")).thenReturn("FAKE.JWT.TOKEN");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"martina@test.it\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.status.message").value("FAKE.JWT.TOKEN"));
    }

    @Test
    void login_missingEmail() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(400));
    }

    @Test
    void login_unknownEmail() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notfound@test.it\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(401))
                .andExpect(jsonPath("$.status.message").value("Unauthorized"));
    }
}
