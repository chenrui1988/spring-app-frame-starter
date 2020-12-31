package org.quick.dev.security;

import org.quick.dev.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    AuthenticationService authService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException  {
        if(usernamePasswordAuthenticationToken.getCredentials().equals("Anonymous")) {
            return new User("Anonymous", "Anonymous", true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
        }
        Object token = usernamePasswordAuthenticationToken.getCredentials();
        Optional<TokenUserDetail> tokenInDB = authService.validateAndRetrieveUserFromToken((String) token);
        if(!tokenInDB.isPresent()) {
            throw new UsernameNotFoundException("Cannot find user with authentication token = " + token);
        }
        return tokenInDB.get();
    }

}
