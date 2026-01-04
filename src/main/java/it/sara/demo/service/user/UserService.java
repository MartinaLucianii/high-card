package it.sara.demo.service.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void addUser(CriteriaAddUser addUserRequest) throws GenericException;

    void updateUser(CriteriaAddUser updateUserRequest, String guid) throws GenericException;

    GetUsersResult getUsers(CriteriaGetUsers criteriaGetUsers) throws GenericException;

}