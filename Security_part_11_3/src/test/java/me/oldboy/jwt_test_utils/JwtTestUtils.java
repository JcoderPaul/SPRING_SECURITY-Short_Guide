package me.oldboy.jwt_test_utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.Map;

public class JwtTestUtils {
    public static String generateJWT(String mainUser,
                                     RsaJsonWebKey rsaJsonWebKey,
                                     WireMockServer wireMockServer,
                                     Map<String, Object> realmAccessClaims) throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("http://localhost:" + wireMockServer.port() + "/auth/realms/test-realm");
        claims.setSubject(mainUser);
        claims.setAudience("test-client");
        claims.setClaim("username", mainUser);
        claims.setClaim("preferred_username", mainUser);
        claims.setClaim("realm_access", realmAccessClaims);
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setIssuedAtToNow();

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        return jws.getCompactSerialization();
    }
}