package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.CryptographicUtilities;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cipher_store", indexes = @Index(name = "cipher_store_kid_uindex", columnList = "kid"))
public class Cipher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(32)", nullable = false)
    private byte[] kid;

    @Column(nullable = false)
    private CipherDecryptionMethod decryptionMethod;

    @Column(columnDefinition = "BINARY(12)")
    private byte[] iv;

    @Column(columnDefinition = "VARBINARY(512)")
    private byte[] cipherStandard;

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] cipherLong;

    public Cipher(byte[] kid, CipherDecryptionMethod decryptionMethod, byte[] iv, byte[] cipher){
        this.kid = kid;
        this.decryptionMethod = decryptionMethod;
        this.iv = iv;
        setCipher(cipher);
    }

    public Cipher(byte[] kid, CipherDecryptionMethod decryptionMethod, CryptographicUtilities.EncryptionResult encryptionResult){
        this(kid, decryptionMethod, encryptionResult.getIv(), encryptionResult.getCipher());
    }

    public byte[] getCipher(){
        return Objects.requireNonNull(Objects.requireNonNullElse(cipherLong, cipherStandard));
    }

    public void setCipher(byte[] cipher){
        if (cipher.length <= 512){
            cipherStandard = cipher;
            return;
        }

        cipherLong = cipher;
    }
}
