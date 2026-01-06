package it.sara.demo.service.user.impl;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.mapper.UserMapper;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.service.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create a new user with basic validation</li>
 *   <li>Update an existing user identified by GUID</li>
 *   <li>Retrieve users with filtering (query), sorting and pagination</li>
 * </ul>
 *
 * <p>Error handling:
 * <ul>
 *   <li>Business/validation errors are thrown as {@link GenericException} with a specific code/message</li>
 *   <li>Unexpected errors are logged and wrapped into {@link GenericException#GENERIC_ERROR}</li>
 * </ul>
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final StringUtil stringUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    /**
     * Creates a new user.
     *
     * <p>Validations:
     * <ul>
     *   <li>firstName, lastName, email, phoneNumber must be present</li>
     *   <li>email must match a basic email format</li>
     *   <li>phoneNumber must match Italian format {@code +39} followed by 8-11 digits</li>
     * </ul>
     *
     * @param criteria input data required to create a user
     * @throws GenericException if validation fails or a persistence error occurs
     */
    @Override
    public void addUser(CriteriaAddUser criteria) throws GenericException {
        User user;

        try {
            if (stringUtil.isNullOrEmpty(criteria.getFirstName())) {
                throw new GenericException(400, "First name is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getLastName())) {
                throw new GenericException(400, "Last name is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getEmail())) {
                throw new GenericException(400, "Email is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getPhoneNumber())) {
                throw new GenericException(400, "Phone is required");
            }

            if (!isEmailValid(criteria.getEmail())){
                throw new GenericException(400, "Email is not valid");
            }

            if (!isPhoneNumberValid(criteria.getPhoneNumber())){
                throw new GenericException(400, "Phone is not valid");
            }


            user = new User();
            user.setFirstName(criteria.getFirstName());
            user.setLastName(criteria.getLastName());
            user.setEmail(criteria.getEmail());
            user.setPhoneNumber(criteria.getPhoneNumber());

            userRepository.save(user);

        } catch (GenericException e) {
            throw e;
        }catch (Exception e){
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }

    /**
     * Updates an existing user identified by GUID.
     *
     * <p>Notes:
     * <ul>
     *   <li>The user is retrieved with {@link UserRepository#getByGuid(String)}</li>
     *   <li>If no user is found, a {@link GenericException} is thrown</li>
     *   <li>This method updates the in-memory object fields directly</li>
     * </ul>
     *
     * @param updateUserRequest new values to apply to the user
     * @param guid identifier of the user to update
     * @throws GenericException if guid is missing, user is not found, or an unexpected error occurs
     */
    @Override
    public void updateUser(CriteriaAddUser updateUserRequest, String guid) throws GenericException {
        try {
            if (guid == null || guid.isBlank()) {
                throw new GenericException(400, "Guid is required");
            }
            User user = userRepository.getByGuid(guid).orElseThrow(() -> new GenericException(400, "User not found"));
            user.setFirstName(updateUserRequest.getFirstName());
            user.setLastName(updateUserRequest.getLastName());
            user.setEmail(updateUserRequest.getEmail());
            user.setPhoneNumber(updateUserRequest.getPhoneNumber());

        } catch (GenericException e) {
            throw e;
        }catch (Exception e){
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }

    /**
     * Retrieves a paginated list of users according to criteria:
     * query filtering, ordering and pagination parameters.
     *
     * <p>Behavior:
     * <ul>
     *   <li>{@code query} is matched (case-insensitive) against email, firstName and lastName</li>
     *   <li>{@code order} defines sorting; if null, a default is applied</li>
     *   <li>{@code offset} must be &gt;= 0</li>
     *   <li>{@code limit} is forced to be at least 1 and capped to 100 (safe limit)</li>
     * </ul>
     *
     * @param criteriaGetUsers criteria for retrieving users
     * @return a {@link GetUsersResult} containing the page of users and a total field
     * @throws GenericException if criteria are invalid or an unexpected error occurs
     */
    @Override
    public GetUsersResult getUsers(CriteriaGetUsers criteriaGetUsers) throws GenericException {
        try{
            if (criteriaGetUsers == null){
                throw new GenericException(400, "Criteria is required");
            }
            int offset = criteriaGetUsers.getOffset();
            int limit = criteriaGetUsers.getLimit();
            CriteriaGetUsers.OrderType order = criteriaGetUsers.getOrder();
            String query = criteriaGetUsers.getQuery();


            if (offset < 0){
                throw new GenericException(400, "Offset < 0 ");
            }
            if (limit <= 0){ criteriaGetUsers.setLimit(1); }

            String q = (query == null) ? "" : query.trim().toUpperCase(Locale.ROOT);


            List<User> users = userRepository.getAll();
           List<User> userFiltered = users.stream().filter(u -> u.getEmail().toUpperCase().contains(q)
           || u.getFirstName().toUpperCase().contains(q) || u.getLastName().toUpperCase().contains(q)).toList();

            Comparator<User> comparator = getUserComparator(order);
            List<User> usersComp = userFiltered.stream().sorted(comparator).toList();

            int size = usersComp.size();


            int safeLimit  = Math.min(Math.max(1, limit), 100);

            List<User> finalUser;
            if (offset >= size) {
                finalUser = List.of();
            } else {
                int toIndex = Math.min(size, offset + safeLimit);
                finalUser = usersComp.subList(offset, toIndex);
            }
            List<UserDTO> userDTO = finalUser.stream()
                    .map(userMapper::entityToDto1)
                    .toList();

            int total = userDTO.size();
            GetUsersResult result = new GetUsersResult();
            result.setTotal(total);
            result.setUsers(userDTO);
            return result;


        } catch (GenericException ge) {
            throw ge;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }
    /**
     * Retrieves a paginated list of users according to criteria:
     * query filtering, ordering and pagination parameters.
     *
     * <p>Behavior:
     * <ul>
     *   <li>{@code query} is matched (case-insensitive) against email, firstName and lastName</li>
     *   <li>{@code order} defines sorting; if null, a default is applied</li>
     *   <li>{@code offset} must be &gt;= 0</li>
     *   <li>{@code limit} is forced to be at least 1 and capped to 100 (safe limit)</li>
     * </ul>
     *
     */
    private static Comparator<User> getUserComparator(CriteriaGetUsers.OrderType order) {
        Comparator<User> comparator;

        CriteriaGetUsers.OrderType ord =
                (order == null) ? CriteriaGetUsers.OrderType.BY_LASTNAME_DESC : order;

        if (ord == CriteriaGetUsers.OrderType.BY_FIRSTNAME) {
            comparator = Comparator.comparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER);
        } else if (ord == CriteriaGetUsers.OrderType.BY_FIRSTNAME_DESC) {
            comparator = Comparator.comparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER).reversed();
        } else if (ord == CriteriaGetUsers.OrderType.BY_LASTNAME) {
            comparator = Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER).reversed();
        }
        return comparator;
    }


    /**
     * Validates an email address using a basic regex.
     *
     * @param email email to validate
     * @return true if email is not null and matches the regex, false otherwise
     */
    public boolean isEmailValid (String email){
       return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }


    /**
     * Validates an Italian phone number in the format {@code +39} followed by 8-11 digits.
     *
     * @param phoneNumber phone number to validate
     * @return true if phone number is not null and matches the regex, false otherwise
     */
    public boolean isPhoneNumberValid (String phoneNumber){
        return phoneNumber != null && phoneNumber.matches("^\\+39\\d{8,11}$");
    }
}
