package it.sara.demo.web;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.response.GenericResponse;
import it.sara.demo.web.user.UserController;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.response.GetUsersResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final AddUserAssembler addUserAssembler = mock(AddUserAssembler.class);

    private final UserController controller = new UserController(userService, addUserAssembler);

    @Test
    void testAddUser_ok() throws Exception {
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Martina");
        request.setLastName("Luciani");
        request.setEmail("martina@test.it");
        request.setPhoneNumber("+393331112233");

        CriteriaAddUser criteria = new CriteriaAddUser();
        when(addUserAssembler.toCriteria(request)).thenReturn(criteria);

        ResponseEntity<GenericResponse> result = controller.addUser(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getStatus());
        assertEquals(200, result.getBody().getStatus().getCode());
        assertEquals("User added.", result.getBody().getStatus().getMessage());

        verify(addUserAssembler, times(1)).toCriteria(request);
        verify(userService, times(1)).addUser(criteria);
    }

    @Test
    void testAddUser_error() throws Exception {
        AddUserRequest request = new AddUserRequest();
        CriteriaAddUser criteria = new CriteriaAddUser();
        when(addUserAssembler.toCriteria(request)).thenReturn(criteria);

        doThrow(new GenericException(400, "Email is required"))
                .when(userService).addUser(criteria);

        GenericException ex = assertThrows(GenericException.class, () -> controller.addUser(request));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("Email is required", ex.getStatus().getMessage());
    }

    @Test
    void testUpdateUser_ok() throws Exception {
        String guid = "abc-guid-123";

        AddUserRequest request = new AddUserRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setEmail("new@test.it");
        request.setPhoneNumber("+393331112233");

        CriteriaAddUser criteria = new CriteriaAddUser();
        when(addUserAssembler.toCriteria(request)).thenReturn(criteria);

        ResponseEntity<GenericResponse> result = controller.addOrUpdateUser(guid, request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(200, result.getBody().getStatus().getCode());
        assertEquals("User update.", result.getBody().getStatus().getMessage());

        verify(addUserAssembler, times(1)).toCriteria(request);
        verify(userService, times(1)).updateUser(criteria, guid);
    }

    @Test
    void testUpdateUser_error() throws Exception {
        String guid = "abc-guid-123";
        AddUserRequest request = new AddUserRequest();
        CriteriaAddUser criteria = new CriteriaAddUser();
        when(addUserAssembler.toCriteria(request)).thenReturn(criteria);

        doThrow(new GenericException(404, "User not found"))
                .when(userService).updateUser(criteria, guid);

        GenericException ex = assertThrows(GenericException.class, () -> controller.addOrUpdateUser(guid, request));
        assertEquals(404, ex.getStatus().getCode());
        assertEquals("User not found", ex.getStatus().getMessage());
    }

    @Test
    void testGetUsers_ok() throws Exception {
        CriteriaGetUsers request = new CriteriaGetUsers();
        request.setOffset(0);
        request.setLimit(10);
        request.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
        request.setQuery("Mar");

        UserDTO dto = new UserDTO();
        dto.setFirstName("Martina");
        dto.setLastName("Luciani");
        dto.setEmail("martina@test.it");

        GetUsersResult serviceResult = new GetUsersResult();
        serviceResult.setTotal(1);
        serviceResult.setUsers(List.of(dto));

        when(userService.getUsers(request)).thenReturn(serviceResult);

        ResponseEntity<GetUsersResponse> result = controller.getUsers(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotal());
        assertEquals(1, result.getBody().getUsers().size());
        assertEquals("Martina", result.getBody().getUsers().get(0).getFirstName());

        verify(userService, times(1)).getUsers(request);
    }

    @Test
    void testGetUsers_error() throws Exception {
        CriteriaGetUsers request = new CriteriaGetUsers();

        when(userService.getUsers(request)).thenThrow(new GenericException(400, "Criteria is required"));

        GenericException ex = assertThrows(GenericException.class, () -> controller.getUsers(request));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("Criteria is required", ex.getStatus().getMessage());
    }
}