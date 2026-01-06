package it.sara.demo.service.user;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.mapper.UserMapper;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.impl.UserServiceImpl;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.service.util.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {


    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserServiceImpl service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        StringUtil str = new StringUtil();

        service = new UserServiceImpl(str, userRepository, userMapper );
    }

    private User user(String first, String last, String email) {
        User u = new User();
        u.setGuid(UUID.randomUUID().toString());
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPhoneNumber("+39123456789");
        return u;
    }

    @Test
    void getUsers_filtersByQuery() throws Exception {
        List<User> db = List.of(
                user("Martina", "Luciani", "martina@test.it"),
                user("Mario", "Rossi", "mario@test.it")
        );
        when(userRepository.getAll()).thenReturn(db);

        when(userMapper.entityToDto1(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            UserDTO dto = new UserDTO();
            dto.setEmail(u.getEmail());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            return dto;
        });

        CriteriaGetUsers c = new CriteriaGetUsers();
        c.setOffset(0);
        c.setLimit(10);
        c.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
        c.setQuery("Mar");

        GetUsersResult res = service.getUsers(c);

        assertNotNull(res);
        assertEquals(2, res.getUsers().size());
        assertEquals(2, res.getTotal());

        c.setQuery("Martina");
        GetUsersResult res2 = service.getUsers(c);
        assertEquals(1, res2.getUsers().size());
        assertEquals(1, res2.getTotal());
    }

    @Test
    void getUsers_NotMatchQuery() throws Exception {
        when(userRepository.getAll()).thenReturn(List.of(
                user("A", "A", "a@test.it"),
                user("B", "B", "b@test.it")
        ));

        CriteriaGetUsers c = new CriteriaGetUsers();
        c.setOffset(0);
        c.setLimit(10);
        c.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
        c.setQuery("C");

        GetUsersResult res = service.getUsers(c);

        assertEquals(0, res.getUsers().size());
        assertEquals(0, res.getTotal());
    }

    @Test
    void getUsers_BySetOrder() throws Exception {
        when(userRepository.getAll()).thenReturn(List.of(
                user("AB", "A", "a@test.it"),
                user("AC", "B", "b@test.it")
        ));

        CriteriaGetUsers c = new CriteriaGetUsers();
        c.setOffset(0);
        c.setLimit(10);
        c.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME_DESC);
        c.setQuery("A");

        when(userMapper.entityToDto1(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            UserDTO dto = new UserDTO();
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setEmail(u.getEmail());
            return dto;
        });

        GetUsersResult res = service.getUsers(c);
        assertEquals(2, res.getUsers().size());
        assertEquals("AC", res.getUsers().get(0).getFirstName());
        assertEquals("AB", res.getUsers().get(1).getFirstName());

    }


    @Test
    void getUsers_offsetTooHigh() throws Exception {
        when(userRepository.getAll()).thenReturn(List.of(
                user("A", "A", "a@test.it"),
                user("B", "B", "b@test.it")
        ));

        CriteriaGetUsers c = new CriteriaGetUsers();
        c.setOffset(50);
        c.setLimit(10);
        c.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
        c.setQuery("");

        GetUsersResult res = service.getUsers(c);

        assertEquals(0, res.getUsers().size());
        assertEquals(0, res.getTotal());
    }

    @Test
    void getUsers_nullCriteria() {
        GenericException ex = assertThrows(GenericException.class, () -> service.getUsers(null));
        assertEquals(400, ex.getStatus().getCode());
    }


    @Test
    void getUsers_queryNull() throws Exception {
        when(userRepository.getAll()).thenReturn(List.of(
                user("A", "A", "a@test.it"),
                user("B", "B", "b@test.it")
        ));

        CriteriaGetUsers c = new CriteriaGetUsers();
        c.setOffset(0);
        c.setLimit(10);
        c.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
        c.setQuery(null);

        GetUsersResult res = service.getUsers(c);

        assertEquals(2, res.getUsers().size());
    }
}


