package org.quick.dev.repository.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends BaseEntity {
    private String userName;
    private String nickname;
    private String loginId;
    private String password;
    private String mobileNumber;
    private String contactNumber;
    private String addPic;
    private String email;
    private String sex;
    private String remark;
    private String departmentId;
    private String companyId;

}
