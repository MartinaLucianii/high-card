package it.sara.demo.service.result;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenericPagedResult extends GenericResult {
    private int total;
}
