package org.quick.dev.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessToken {

    private String accessToken;
    private String refreshToken;
    private Long expiredAt;

}
