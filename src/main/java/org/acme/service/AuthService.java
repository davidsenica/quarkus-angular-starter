package org.acme.service;

import org.acme.entity.dto.LoginDTO;
import org.acme.util.JWTUtils;
import org.acme.util.PasswordUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

@ApplicationScoped
public class AuthService {
    public String login(LoginDTO loginDTO) {
        if(!loginDTO.getUsername().equals("user") || !PasswordUtils.authenticate(loginDTO.getPassword().toCharArray(), "user")) {
            return null;
        }
        return JWTUtils.generateJWT(loginDTO.getUsername(), new HashSet<>());
    }

    public String refresh(Cookie cookie) {
        String json = new String(Base64.getUrlDecoder().decode(cookie.getValue().split("\\.")[1]),
                StandardCharsets.UTF_8);
        try {
            JwtClaims claims = JwtClaims.parse(json);
            String email = (String) claims.getClaimValue("upn");
            return JWTUtils.generateJWT(email, new HashSet<>());
        } catch (InvalidJwtException e) {
            e.printStackTrace();
        }

        return null;
    }
}
