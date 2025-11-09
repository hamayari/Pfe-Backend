package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TwoFactorAuthService {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP = 30; // 30 seconds

    public TwoFactorAuthService() {
    }

    /**
     * Génère un secret pour l'authentification 2FA
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replaceAll("[^A-Z2-7]", "");
    }

    /**
     * Génère un QR Code pour l'authentification 2FA
     * @param username Nom d'utilisateur
     * @param secret Secret TOTP
     * @return URL du QR Code en Base64
     */
    public String generateQrCodeUrl(String username, String secret) {
        try {
            String otpAuthUrl = generateGoogleAuthenticatorUrl(username, secret);
            return generateQrCodeWithZXing(otpAuthUrl, 250, 250);
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération QR Code", e);
        }
    }

    /**
     * Vérifie un code TOTP
     * @param secret Secret TOTP de l'utilisateur
     * @param code Code à 6 chiffres fourni par l'utilisateur
     * @return true si le code est valide
     */
    public boolean verifyCode(String secret, String code) {
        try {
            long currentTime = System.currentTimeMillis() / 1000L / TIME_STEP;
            
            // Vérifier le code actuel et les 2 fenêtres précédentes/suivantes (tolérance)
            for (int i = -2; i <= 2; i++) {
                String generatedCode = generateTOTPCode(secret, currentTime + i);
                if (generatedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Génère un code TOTP pour un temps donné
     */
    private String generateTOTPCode(String secret, long timeCounter) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] secretBytes = Base64.getDecoder().decode(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeCounter).array();
        
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secretBytes, ALGORITHM));
        byte[] hash = mac.doFinal(timeBytes);
        
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        
        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }

    /**
     * Génère un QR Code avec ZXing (alternative)
     */
    public String generateQrCodeWithZXing(String text, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        return "data:image/png;base64," + base64Image;
    }

    /**
     * Génère l'URL pour Google Authenticator
     */
    public String generateGoogleAuthenticatorUrl(String username, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            "GestionPro",
            username,
            secret,
            "GestionPro"
        );
    }
}
