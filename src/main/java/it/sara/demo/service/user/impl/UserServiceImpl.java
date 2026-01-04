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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final StringUtil stringUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;



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

            String q = query.toUpperCase(Locale.ROOT);

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



    public boolean isEmailValid (String email){
       return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean isPhoneNumberValid (String phoneNumber){
        return phoneNumber != null && phoneNumber.matches("^\\+39\\d{8,11}$");
    }
}
