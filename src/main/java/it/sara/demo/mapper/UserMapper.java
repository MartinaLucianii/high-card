package it.sara.demo.mapper;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.mapper.operations.MapperOperations1To1;
import it.sara.demo.service.database.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between {@link User} entity and {@link UserDTO}.
 *
 * <p>This mapper is managed by Spring ({@code componentModel = "spring"}), therefore it can be injected
 * into services and controllers.
 *
 * <p>The mapping methods are inherited from {@link MapperOperations1To1}.
 * MapStruct generates the concrete implementation at compile time.
 *
 * <p>The class is declared as {@code abstract} because MapStruct will generate a subclass implementation
 * (e.g. {@code UserMapperImpl}) containing the actual mapping logic.
 */

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserMapper implements MapperOperations1To1<UserDTO, User> {
}
