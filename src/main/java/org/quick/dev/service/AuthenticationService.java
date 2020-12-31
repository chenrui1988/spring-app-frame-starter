package org.quick.dev.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.quick.dev.model.EntityResult;
import org.quick.dev.repository.AuthenticationMapper;
import org.quick.dev.repository.entity.SecurityKey;
import org.quick.dev.security.AccessToken;
import org.quick.dev.security.TokenUserDetail;
import org.quick.dev.utils.PasswordEncrypt;
import org.quick.dev.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;

@Service
public class AuthenticationService {

    @Autowired
    AuthenticationMapper authenticationMapper;

    private final String ISSUER = "https://arui.me";

    public boolean checkUserPassword(String username, String password) {
        Optional<String> passwordInDB = Optional.ofNullable(authenticationMapper.getPasswordByLoginId(username));
        if (passwordInDB.isPresent() && passwordInDB.get().equals(password)) {
            return true;
        }
        return false;
    }

    @Transactional
    public EntityResult issueToken(String username) {
        Optional<SecurityKey> op = Optional.ofNullable(authenticationMapper.getAuthRSAKeyBy());
        if (!op.isPresent())
            throw new AuthenticationServiceException("security key for auth not init!");

        try {
            org.quick.dev.repository.entity.User user = this.authenticationMapper.getUserByLoginId(username);

            SecurityKey securityKey = op.get();
            byte[] publicKey = Base64.getDecoder().decode(securityKey.getPublicKey());

            String jwtId  = UUID.randomUUID().toString();
            AccessToken accessToken = genAccessToken(jwtId, publicKey, user);
            return EntityResult.ok(accessToken);
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("issue token error, please contact admin support!");
        }
    }

    private AccessToken genAccessToken(String jwtId, byte[] publicKey, org.quick.dev.repository.entity.User user)  throws JOSEException {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + 1000 * 60 * 60);
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .subject(user.getLoginId())
            .expirationTime(expiredAt)
            .notBeforeTime(now)
            .issueTime(now)
            .jwtID(jwtId)
            .claim("loginId", user.getLoginId())
            .claim("companyId", user.getCompanyId())
            .claim("departmentId", user.getDepartmentId())
//            .claim("role", role.get())
            .build();

        JWSSigner signer = new MACSigner(publicKey);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
        signedJWT.sign(signer);

        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken(signedJWT.serialize());
        accessToken.setRefreshToken(this.genRefreshToken(jwtId, publicKey));
        accessToken.setExpiredAt(expiredAt.getTime());
        return accessToken;
    }

    private String genRefreshToken(String tokenId, byte[] publicKey) throws JOSEException {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + 7 * 24 * 1000 * 60 * 60);
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().issuer(ISSUER).subject("REFRESH TOKEN")
                .expirationTime(expiredAt).notBeforeTime(now).issueTime(now).claim("tid", tokenId)
                .jwtID(UUID.randomUUID().toString()).build();

        JWSSigner signer = new MACSigner(publicKey);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
        signedJWT.sign(signer);

        String jwtString = signedJWT.serialize();
        return jwtString;
    }

    public Optional<TokenUserDetail> validateAndRetrieveUserFromToken(String accessToken) {
        Optional<SecurityKey> op = Optional.ofNullable(authenticationMapper.getAuthRSAKeyBy());
        if (!op.isPresent())
            throw new AuthenticationServiceException("security key for auth not init!");

        try {
            SecurityKey securityKey = op.get();
            byte[] publicKey = Base64.getDecoder().decode(securityKey.getPublicKey());

            SignedJWT jwt = SignedJWT.parse(accessToken);
            JWSVerifier verifier = new MACVerifier(publicKey);

            if (!jwt.verify(verifier)) {
                throw new AuthenticationServiceException("invalid access token!");
            }

            String username = jwt.getJWTClaimsSet().getSubject();
            org.quick.dev.repository.entity.User userInDB = authenticationMapper.getUserByLoginId(username);
            if (userInDB == null)
                return Optional.empty();

            TokenUserDetail user = new TokenUserDetail(userInDB.getId(), userInDB.getLoginId(), accessToken, true, true,
                    !jwt.getJWTClaimsSet().getExpirationTime().before(new Date()), true,
                    AuthorityUtils.createAuthorityList("USER"));
            return Optional.of(user);
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("issue token error, please contact admin support!");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

	public EntityResult refreshToken(String accessToken, String refreshToken) {
        Optional<SecurityKey> op = Optional.ofNullable(authenticationMapper.getAuthRSAKeyBy());
        if (!op.isPresent())
            return EntityResult.build(401, "security key for auth not init!");

        try {
            SecurityKey securityKey = op.get();
            byte[] publicKey = Base64.getDecoder().decode(securityKey.getPublicKey());
            JWSVerifier verifier = new MACVerifier(publicKey);

            SignedJWT tokenJWT = SignedJWT.parse(accessToken);
            SignedJWT refreshTokenJWT = SignedJWT.parse(refreshToken);

            if (!tokenJWT.verify(verifier)) {
                throw new AuthenticationServiceException("invalid access token!");
            }

            if (!refreshTokenJWT.verify(verifier)) {
                throw new AuthenticationServiceException("invalid access token!");
            }

            if(!tokenJWT.getJWTClaimsSet().getJWTID().equals(refreshTokenJWT.getJWTClaimsSet().getClaim("tid"))) {
                throw new AuthenticationServiceException("invalid refresh token!");
            }

            String username = tokenJWT.getJWTClaimsSet().getSubject();
            org.quick.dev.repository.entity.User userInDB = authenticationMapper.getUserByLoginId(username);
            if (userInDB == null)
                throw new AuthenticationServiceException("invalid user!");

            String jwtId = UUID.randomUUID().toString();
            AccessToken token = genAccessToken(jwtId, publicKey, userInDB);
            return EntityResult.ok(token);
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("issue token error, please contact admin support!");
        } catch (ParseException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("issue token error, please contact admin support!");
        }
	}

    public void initAuthSecurityKey() {
        Optional<SecurityKey> op = Optional.ofNullable(authenticationMapper.getAuthRSAKeyBy());
        if(op.isPresent()) return;
        String algorithm = "HS256";
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);

        String b64PublicKey = Base64.getEncoder().encodeToString(sharedSecret);
        authenticationMapper.initAuthRSAKey(b64PublicKey, "", algorithm, 256);
    }

    public void initSystemUser(String username, String loginId, String password) {
        Optional<org.quick.dev.repository.entity.User> op = Optional.ofNullable(authenticationMapper.getUserByLoginId(loginId));
        if(op.isPresent()) return;
        authenticationMapper.insertAdminUser(SnowflakeIdWorker.getNextId(), username, loginId, PasswordEncrypt.encrypt(password));
    }
}
