package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private CipherEncryptionMethod decryptionMethod;

    @Column(columnDefinition = "BINARY(12)")
    private byte[] iv;

    @Column(columnDefinition = "VARBINARY(512)")
    private byte[] cipherStandard;

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] cipherLong;

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
