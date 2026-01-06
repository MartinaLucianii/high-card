package it.sara.demo.security;

import it.sara.demo.service.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef",
        "jwt.expiration=2000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_ok() {
        String token = jwtUtil.generateToken("martina@test.it");
        assertNotNull(token);
        assertTrue(jwtUtil.validateJwtToken(token));
        assertEquals("martina@test.it", jwtUtil.getUsernameFromToken(token));
    }

}
