package org.acme.util;

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

public final class JWTUtils {
    private static final int TEN_MINUTES = 60 * 10;
    public static final int SEVEN_DAYS = 60 * 60 * 24 * 7;

    private JWTUtils() {
    }

    public static String generateJWT(String user, Set<String> roles) {
        return generate(user, roles, TEN_MINUTES);
    }

    public static String generateRefreshToken(String user) {
        // FIXME Use different privateKey
        return generate(user, null, SEVEN_DAYS);
    }

    private static String generate(String name, Set<String> roles, int expirationSeconds) {
        PrivateKey pk;
        try {
            pk = readPrivateKey("/privateKey.pem");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setClaim(Claims.exp.name(), Instant.now().getEpochSecond() + expirationSeconds);
        jwtClaims.setClaim(Claims.iat.name(), Instant.now().getEpochSecond());
        jwtClaims.setClaim(Claims.iss.name(), "ddevs");
        jwtClaims.setClaim(Claims.upn.name(), name);
        jwtClaims.setClaim(Claims.groups.name(), roles);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(pk);
        jws.setKeyIdHeaderValue("key");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static PrivateKey readPrivateKey(final String name) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        InputStream in = JWTUtils.class.getResourceAsStream(name);
        byte[] tmp = new byte[8182];
        int length = in.read(tmp);
        String pkEncoded = new String(tmp, 0, length, StandardCharsets.UTF_8);
        pkEncoded = pkEncoded.replace("-----BEGIN PRIVATE KEY-----", "");
        pkEncoded = pkEncoded.replace("-----END PRIVATE KEY-----", "");
        pkEncoded = pkEncoded.replace("\r\n", "");
        byte[] decoded = Base64.getDecoder().decode(pkEncoded);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);

    }
}
