package it.sara.demo.service.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;

/**
 * Service contract for user management operations.
 *
 * <p>This interface defines the business use cases related to users,
 * independent from the web/controller layer.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param addUserRequest data required to create a user
     * @throws GenericException if validation fails or an unexpected error occurs
     */
    void addUser(CriteriaAddUser addUserRequest) throws GenericException;

    /**
     * Updates an existing user identified by its GUID.
     *
     * @param updateUserRequest new values to apply
     * @param guid unique identifier of the user to update
     * @throws GenericException if guid is missing, user is not found, or an unexpected error occurs
     */
    void updateUser(CriteriaAddUser updateUserRequest, String guid) throws GenericException;

    /**
     * Retrieves a list of users using filter/sort/pagination criteria.
     *
     * @param criteriaGetUsers filtering, sorting and pagination criteria
     * @return paged result containing the users list and total count
     * @throws GenericException if criteria are invalid or an unexpected error occurs
     */
    GetUsersResult getUsers(CriteriaGetUsers criteriaGetUsers) throws GenericException;
}