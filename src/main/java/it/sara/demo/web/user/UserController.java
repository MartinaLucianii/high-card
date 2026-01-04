package it.sara.demo.web.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.response.GenericResponse;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.response.GetUsersResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AddUserAssembler addUserAssembler;

    @RequestMapping(value = {"/v1/user/{guid}"}, method = RequestMethod.PUT)
    public ResponseEntity<GenericResponse> addOrUpdateUser(@PathVariable String guid, @Valid @RequestBody AddUserRequest request) throws GenericException {
        CriteriaAddUser criteria = addUserAssembler.toCriteria(request);
        userService.updateUser(criteria, guid);
        return ResponseEntity.ok(GenericResponse.success("User update." ));
    }

    @RequestMapping(value = {"/v1/user"}, method = RequestMethod.POST)
    public ResponseEntity<GenericResponse> addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        CriteriaAddUser criteria = addUserAssembler.toCriteria(request);
        userService.addUser(criteria);
        return ResponseEntity.ok(GenericResponse.success("User added." ));
    }

    @RequestMapping(value = {"/v1/user"}, method = RequestMethod.GET)
    public ResponseEntity<GetUsersResponse> getUsers(@ModelAttribute CriteriaGetUsers request) throws GenericException {
        GetUsersResult result = userService.getUsers(request);

        GetUsersResponse response = new GetUsersResponse();
        response.setTotal(result.getTotal());
        response.setUsers(result.getUsers());

        return ResponseEntity.ok(response);
    }

}
