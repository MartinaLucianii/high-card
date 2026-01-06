package it.sara.demo.exception;

import it.sara.demo.web.response.GenericResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class GlobalExceptionHandlerTest {
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        mvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(handler)
                .build();
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/generic")
        public GenericResponse generic() throws GenericException {
            throw new GenericException(400, "Bad request");
        }

        @PostMapping("/validation")
        public String validation(@Valid @RequestBody TestRequest req) {
            return "OK";
        }

        @PostMapping("/runtime")
        public String runtime() {
            throw new RuntimeException("Crash");
        }
    }

    static class TestRequest {
        @Pattern(regexp = "^\\+39\\d{8,11}$", message = "Phone is not valid")
        public String phoneNumber;
    }

    @Test
    void genericException() throws Exception {
        mvc.perform(post("/test/generic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(400))
                .andExpect(jsonPath("$.status.message").value("Bad request"));
    }

    @Test
    void validationException() throws Exception {
        mvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+3933311122334444\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(400))

                .andExpect(jsonPath("$.status.message").value("Phone is not valid"));
    }

    @Test
    void runtimeException() throws Exception {
        mvc.perform(post("/test/runtime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(500));
    }
}

