package org.quick.dev.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class SecurityKey extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private String publicKey;
    private String privateKey;
    private String algorithm;
    private String keySize;
    private String type;
    private Boolean isSystem;
}
