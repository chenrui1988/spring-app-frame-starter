package org.quick.dev.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PageQueryParams {

    private int startPage;
    private int size;
    private Map<String, Object> conditions;

}
