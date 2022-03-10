package org.acme.resource.auth;

import org.acme.entity.Message;
import org.acme.entity.dto.LoginDTO;
import org.acme.service.AuthService;
import org.acme.util.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {
    private static final Logger log = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    AuthService authService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginDTO loginDTO) {
        String jwt = authService.login(loginDTO);
        if(jwt == null) {
            return Response.status(401).entity(Message.withMessage("Incorrect username or password!")).build();
        }

        NewCookie cookie = new NewCookie("jwt",
                JWTUtils.generateRefreshToken(loginDTO.getUsername()),
                "/api/login/refresh",
                null,
                null,
                JWTUtils.SEVEN_DAYS,
                false,
                true);
        return Response.ok(Message.withMessage(jwt)).cookie(cookie).build();
    }

    @GET
    @Path("/refresh")
    public Response refreshToken(@CookieParam("jwt") Cookie cookie) {
        if (cookie == null) {
            return Response.status(401).build();
        }
        String newJwt = authService.refresh(cookie);
        if(newJwt != null) {
            return Response.ok(Message.withMessage(newJwt)).build();
        }
        return Response.status(500).build();
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout() {
        NewCookie cookie = new NewCookie("jwt",
                null,
                "/api/login/refresh",
                null,
                null,
                0,
                false,
                true);
        return Response.ok().cookie(cookie).build();
    }
}
