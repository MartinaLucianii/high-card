package it.sara.demo.mapper.operations;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;



public interface MapperOperations1To1<D1, E> {

    /**
     * Convert a DTO of type D1 to an Entity of type E.
     * @param dto to convert
     * @return the entity
     */
    @BeanMapping(builder = @Builder(disableBuilder = true))
    E dto1toEntity(D1 dto);

    /**
     * Convert an Entity of type E to a DTO of type D1.
     * @param entity to convert
     * @return the DTO
     */
    @BeanMapping(builder = @Builder(disableBuilder = true))
    D1 entityToDto1(E entity);


}
