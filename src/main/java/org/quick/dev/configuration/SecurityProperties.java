package org.quick.dev.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

    private boolean enable;
    private String ignoringUrls;
    private AdminUser adminUser;

}
