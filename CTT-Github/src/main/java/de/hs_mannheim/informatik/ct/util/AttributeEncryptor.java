package de.hs_mannheim.informatik.ct.util;



import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import java.security.InvalidKeyException;
import java.util.Base64;

/**
 * Encryptor class which encrypts and decrypts column values tagged with @Convert using the AES algorithm provided by JDK.
 */
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String AES = "AES";
    private static final String SECRET = "corona-ctt-20201";

    private final Key key;
    private final Cipher cipher;

    public AttributeEncryptor() throws Exception {
        key = new SecretKeySpec(SECRET.getBytes(), AES);
        cipher = Cipher.getInstance(AES);
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }
}

