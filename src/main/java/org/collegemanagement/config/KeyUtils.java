package org.collegemanagement.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


@Configuration
@Slf4j
public class KeyUtils {
    final Environment environment;

    @Value("${access-token.private}")
    private String accessTokenPrivateKeyPath;

    @Value("${access-token.public}")
    private String accessTokenPublicKeyPath;

    @Value("${refresh-token.private}")
    private String refreshTokenPrivateKeyPath;

    @Value("${refresh-token.public}")
    private String refreshTokenPublicKeyPath;

    private KeyPair _accessTokenKeyPair;
    private KeyPair _refreshTokenKeyPair;

    public KeyUtils(Environment environment) {
        this.environment = environment;
    }


    // ---------------- PUBLIC API ----------------

    public RSAPublicKey getAccessTokenPublicKey() {
        return (RSAPublicKey) getAccessTokenKeyPair().getPublic();
    }

    public RSAPrivateKey getAccessTokenPrivateKey() {
        return (RSAPrivateKey) getAccessTokenKeyPair().getPrivate();
    }

    public RSAPublicKey getRefreshTokenPublicKey() {
        return (RSAPublicKey) getRefreshTokenKeyPair().getPublic();
    }

    public RSAPrivateKey getRefreshTokenPrivateKey() {
        return (RSAPrivateKey) getRefreshTokenKeyPair().getPrivate();
    }

    // ---------------- INTERNAL LOGIC ----------------

    private KeyPair getAccessTokenKeyPair() {
        if (_accessTokenKeyPair == null) {
            _accessTokenKeyPair = loadOrGenerate(accessTokenPublicKeyPath, accessTokenPrivateKeyPath);
        }
        return _accessTokenKeyPair;
    }

    private KeyPair getRefreshTokenKeyPair() {
        if (_refreshTokenKeyPair == null) {
            _refreshTokenKeyPair = loadOrGenerate(refreshTokenPublicKeyPath, refreshTokenPrivateKeyPath);
        }
        return _refreshTokenKeyPair;
    }


    private KeyPair loadOrGenerate(String publicPath, String privatePath) {
        File publicFile = new File(publicPath);
        File privateFile = new File(privatePath);

        // If present, load keys from disk
        if (publicFile.exists() && privateFile.exists()) {
            log.info("Loading RSA key pair from files.");
            return readKeyPair(publicFile, privateFile);
        }

        // If in production and missing → FAIL HARD (security)
        if (isProd()) {
            throw new RuntimeException("❌ RSA key files missing in PRODUCTION environment.");
        }

        // Dev mode: auto-generate keys
        log.warn("⚠ RSA keys missing → Generating new ones...");

        return generateAndSaveKeyPair(publicFile, privateFile);
    }

    private KeyPair readKeyPair(File pubFile, File privFile) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");

            byte[] publicBytes = Files.readAllBytes(pubFile.toPath());
            byte[] privateBytes = Files.readAllBytes(privFile.toPath());

            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load RSA key files", e);
        }
    }

    private KeyPair generateAndSaveKeyPair(File pubFile, File privFile) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            KeyPair keyPair = generator.generateKeyPair();

            File publicDir = pubFile.getParentFile();
            File privateDir = privFile.getParentFile();

            if (!publicDir.exists() && !publicDir.mkdirs()) {
                throw new IOException("Failed to create public key directory: " + publicDir.getAbsolutePath());
            }

            if (!privateDir.exists() && !privateDir.mkdirs()) {
                throw new IOException("Failed to create private key directory: " + privateDir.getAbsolutePath());
            }


            Files.write(pubFile.toPath(), new X509EncodedKeySpec(keyPair.getPublic().getEncoded()).getEncoded());
            Files.write(privFile.toPath(), new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()).getEncoded());

            return keyPair;

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA keys", e);
        }
    }

    private boolean isProd() {
        return environment != null && environment.acceptsProfiles(Profiles.of("prod"));
    }
}