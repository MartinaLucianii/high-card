package it.sara.demo.mapper;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.mapper.operations.MapperOperations1To1;
import it.sara.demo.service.database.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserMapper implements MapperOperations1To1<UserDTO, User> {
}
