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
/**
 * REST controller responsible for user management operations.
 *
 * <p>Endpoints exposed by this controller:
 * <ul>
 *   <li>{@code POST /user/v1/user} - create a new user</li>
 *   <li>{@code PUT  /user/v1/user/{guid}} - update an existing user identified by {@code guid}</li>
 *   <li>{@code GET  /user/v1/user} - list users with optional filtering, ordering and pagination</li>
 * </ul>
 *
 * <p>Request validation is performed through Jakarta Bean Validation annotations
 * on {@link AddUserRequest} and activated by {@code @Valid}.
 *
 * <p>Error handling: business/validation errors are raised as {@link GenericException}
 * and are expected to be converted to a {@link it.sara.demo.web.response.GenericResponse}
 * by the centralized exception handler (e.g. {@code GlobalExceptionHandler}).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AddUserAssembler addUserAssembler;

    /**
     * Updates an existing user identified by GUID.
     *
     * <p>The request body is validated via {@code @Valid}. If validation fails, Spring raises
     * {@code MethodArgumentNotValidException} before the method is executed.
     *
     * <p>On success, this endpoint returns HTTP 200 with a {@link GenericResponse} having:
     * <ul>
     *   <li>{@code status.code = 200}</li>
     *   <li>{@code status.message = "User update."}</li>
     * </ul>
     *
     * @param guid    unique identifier of the user to update (path variable)
     * @param request updated user attributes (validated request body)
     * @return a {@link ResponseEntity} containing a success {@link GenericResponse}
     * @throws GenericException if the GUID is invalid, the user does not exist, or business rules fail
     */
    @RequestMapping(value = {"/v1/user/{guid}"}, method = RequestMethod.PUT)
    public ResponseEntity<GenericResponse> addOrUpdateUser(@PathVariable String guid, @Valid @RequestBody AddUserRequest request) throws GenericException {
        CriteriaAddUser criteria = addUserAssembler.toCriteria(request);
        userService.updateUser(criteria, guid);
        return ResponseEntity.ok(GenericResponse.success("User update." ));
    }
    /**
     * Updates an existing user identified by GUID.
     *
     * <p>The request body is validated via {@code @Valid}. If validation fails, Spring raises
     * {@code MethodArgumentNotValidException} before the method is executed.
     *
     * <p>On success, this endpoint returns HTTP 200 with a {@link GenericResponse} having:
     * <ul>
     *   <li>{@code status.code = 200}</li>
     *   <li>{@code status.message = "User update."}</li>
     * </ul>
     *
     * @param request updated user attributes (validated request body)
     * @return a {@link ResponseEntity} containing a success {@link GenericResponse}
     * @throws GenericException if the GUID is invalid, the user does not exist, or business rules fail
     */
    @RequestMapping(value = {"/v1/user"}, method = RequestMethod.POST)
    public ResponseEntity<GenericResponse> addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        CriteriaAddUser criteria = addUserAssembler.toCriteria(request);
        userService.addUser(criteria);
        return ResponseEntity.ok(GenericResponse.success("User added." ));
    }
    /**
     * Updates an existing user identified by GUID.
     *
     * <p>The request body is validated via {@code @Valid}. If validation fails, Spring raises
     * {@code MethodArgumentNotValidException} before the method is executed.
     *
     * <p>On success, this endpoint returns HTTP 200 with a {@link GenericResponse} having:
     * <ul>
     *   <li>{@code status.code = 200}</li>
     *   <li>{@code status.message = "User update."}</li>
     * </ul>
     *
     * @param request updated user attributes (validated request body)
     * @return a {@link ResponseEntity} containing a success {@link GenericResponse}
     * @throws GenericException if the GUID is invalid, the user does not exist, or business rules fail
     */
    @RequestMapping(value = {"/v1/user"}, method = RequestMethod.GET)
    public ResponseEntity<GetUsersResponse> getUsers(@ModelAttribute CriteriaGetUsers request) throws GenericException {
        GetUsersResult result = userService.getUsers(request);

        GetUsersResponse response = new GetUsersResponse();
        response.setTotal(result.getTotal());
        response.setUsers(result.getUsers());

        return ResponseEntity.ok(response);
    }

}
