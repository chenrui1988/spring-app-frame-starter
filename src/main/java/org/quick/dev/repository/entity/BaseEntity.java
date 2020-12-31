package org.quick.dev.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BaseEntity {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String belongOrgId;
    private String createUserId;
    private String createOrgId;
    private Date createDate;
    private String updateUserId;
    private String updateOrgId;
    private Date updateDate;
    private Boolean deleted;

}
