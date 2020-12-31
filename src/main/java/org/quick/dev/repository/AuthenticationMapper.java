package org.quick.dev.repository;

import org.apache.ibatis.annotations.*;
import org.quick.dev.repository.entity.SecurityKey;
import org.quick.dev.repository.entity.User;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AuthenticationMapper {
    @Select("select id, user_name as userName, login_id as loginId, department_id as departmentId, company_id as companyId from base_user where deleted = false and enabled=true and login_id = #{loginId}")
    User getUserByLoginId(@Param("loginId") String loginId);

    @Select("select password from base_user where deleted = false and login_id = #{loginId}")
    String getPasswordByLoginId(@Param("loginId") String loginId);

    @Insert("insert base_user(id, user_name, login_id, password, company_id, department_id, enabled, deleted) values(#{id}, #{username}, #{loginId}, #{password}, '-1', '-1', true, false)")
    void insertAdminUser(@Param("id") Long id, @Param("username") String username, @Param("loginId") String loginId, @Param("password") String password);

    @Insert("insert into base_security_key(id, public_key, private_key, algorithm, key_size, type, is_system, deleted) values(replace(uuid(),'-',''), #{publicKey}, #{privateKey}, #{algorithm}, #{keySize}, 'auth', true, false)")
    void initAuthRSAKey(@Param("publicKey") String publicKey, @Param("privateKey") String privateKey, @Param("algorithm") String algorithm, @Param("keySize") Integer keySize);

    @Select("select * from base_security_key where type = 'auth'")
    @Results({
            @Result(property = "publicKey", column = "public_key"),
            @Result(property = "privateKey", column = "private_key"),
            @Result(property = "keySize", column = "key_size")
    })
    SecurityKey getAuthRSAKeyBy();
}
