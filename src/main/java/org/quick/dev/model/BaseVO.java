package org.quick.dev.model;

import lombok.Getter;
import lombok.Setter;
import org.quick.dev.repository.entity.BaseEntity;

@Getter
@Setter
public class BaseVO extends BaseEntity {

    private String createUserName;
    private String createOrgName;
    private String updateUserName;
    private String updateOrgName;

}
