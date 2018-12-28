package com.ood.libraryManagementSystem.provider;

import com.ood.libraryManagementSystem.model.User;
import com.ood.libraryManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FileAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials().toString();
        BCryptPasswordEncoder encoder = userService.getbCryptPasswordEncoder();
        User user = userService.findUserByUsername(username);

        if(user != null && user.getUsername().equals(username) &&  encoder.matches(password, user.getPassword())) {
            ArrayList<GrantedAuthority> roles = new ArrayList<>();
            roles.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return user.getRole();
                }
            });

            return new UsernamePasswordAuthenticationToken(username, password, roles);
        } else {
            throw new BadCredentialsException("Authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}
