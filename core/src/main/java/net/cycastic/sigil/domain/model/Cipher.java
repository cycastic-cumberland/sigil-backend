package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.CryptographicUtilities;

import java.util.Arrays;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cipher_store")
public class Cipher {
    private static final int CIPHER_STANDARD_LENGTH = 65_535;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private CipherDecryptionMethod decryptionMethod;

    @Column(columnDefinition = "BINARY(12)")
    private byte[] iv;

    @Column(columnDefinition = "BLOB")
    private byte[] cipherStandard;

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] cipherLong;

    @Version
    private long version;

    public Cipher(CipherDecryptionMethod decryptionMethod, byte[] iv, byte[] cipher){
        this.decryptionMethod = decryptionMethod;
        this.iv = iv;
        setCipher(cipher);
    }

    public Cipher(CipherDecryptionMethod decryptionMethod, CryptographicUtilities.EncryptionResult encryptionResult){
        this(decryptionMethod, encryptionResult.getIv(), encryptionResult.getCipher());
    }

    public byte[] getCipher(){
        return Objects.requireNonNull(Objects.requireNonNullElse(cipherLong, cipherStandard));
    }

    public void setCipher(byte[] cipher){
        if (cipher.length <= CIPHER_STANDARD_LENGTH){
            cipherStandard = cipher;
            return;
        }

        cipherLong = cipher;
    }

    public boolean copyFrom(Cipher other){
        var updated = false;
        if (!other.getDecryptionMethod().equals(getDecryptionMethod())){
            updated = true;
            setDecryptionMethod(other.getDecryptionMethod());
        }
        if (!Arrays.equals(other.getIv(), getIv())){
            updated = true;
            setIv(other.getIv());
        }
        if (!Arrays.equals(other.getCipher(), getCipher())){
            updated = true;
            setCipher(other.getCipher());
        }

        return updated;
    }
}
