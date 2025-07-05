package net.cycastic.sigil.configuration;

import net.cycastic.sigil.configuration.auth.JwtConfiguration;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.Presigner;
import net.cycastic.sigil.service.auth.AsymmetricJwtVerifier;
import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.auth.JwtVerifier;
import net.cycastic.sigil.service.impl.*;
import net.cycastic.sigil.service.impl.auth.StandardJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.util.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@org.springframework.context.annotation.Lazy
public class EncryptionConfigurations {
    private final Lazy<HashicorpVaultEncryptionProvider> hashicorpVaultEncryptionProvider;
    private final Lazy<SymmetricEncryptionProvider> symmetricEncryptionProvider;
    private final Lazy<EncryptionProvider> encryptionProvider;
    private final Lazy<DecryptionProvider> decryptionProvider;
    private final Lazy<StandardJwtService> jwtService;
    private final Lazy<HashicorpVaultPresigner> hashicorpVaultPresigner;
    private final Lazy<SymmetricPresigner> symmetricPresigner;
    private final Lazy<UriPresigner> uriPresigner;

    @Autowired
    public EncryptionConfigurations(HashicorpVaultConfiguration hashicorpVaultConfiguration,
                                    SymmetricEncryptionConfiguration symmetricEncryptionConfiguration,
                                    SymmetricPresignerConfiguration symmetricPresignerConfiguration,
                                    JwtConfiguration jwtConfiguration){
        var vaultEnabled = false;
        Lazy<HashicorpVaultEncryptionProvider> hashicorpVaultEncryptionProvider = null;
        Lazy<SymmetricEncryptionProvider> symmetricEncryptionProvider = null;
        Lazy<EncryptionProvider> encryptionProvider = null;
        Lazy<DecryptionProvider> decryptionProvider = null;
        Lazy<StandardJwtService> jwtService = null;
        Lazy<HashicorpVaultPresigner> hashicorpVaultPresigner = null;
        Lazy<SymmetricPresigner> symmetricPresigner = null;
        var presigners = new ArrayList<Lazy<?>>();
        if (hashicorpVaultConfiguration.isValid()){
            vaultEnabled = true;
            hashicorpVaultEncryptionProvider = Lazy.of(() -> new HashicorpVaultEncryptionProvider(hashicorpVaultConfiguration));
            encryptionProvider = Lazy.of(hashicorpVaultEncryptionProvider);
            decryptionProvider = Lazy.of(hashicorpVaultEncryptionProvider);

            hashicorpVaultPresigner = Lazy.of(() -> new HashicorpVaultPresigner(hashicorpVaultConfiguration));
            presigners.add(hashicorpVaultPresigner);
            if (hashicorpVaultConfiguration.getSigningPrivateKeyWrapped() != null &&
                    hashicorpVaultConfiguration.getSigningPublicKey() != null){
                jwtService = Lazy.of(() -> {
                    var privateKeyEncrypted = hashicorpVaultConfiguration.getSigningPrivateKeyWrapped();
                    var privateKeyBase64 = new HashicorpVaultEncryptionProvider(hashicorpVaultConfiguration).decrypt(privateKeyEncrypted);
                    var privateKey = StandardJwtService.decodePrivateKey(privateKeyBase64);
                    var publicKey = StandardJwtService.decodePublicKey(hashicorpVaultConfiguration.getSigningPublicKey());
                    return new StandardJwtService(jwtConfiguration, privateKey, publicKey);
                });
            }
        }
        if (symmetricEncryptionConfiguration.isValid()){
            symmetricEncryptionProvider = Lazy.of(() -> new SymmetricEncryptionProvider(symmetricEncryptionConfiguration));
            if (!vaultEnabled){
                encryptionProvider = Lazy.of(symmetricEncryptionProvider);
                decryptionProvider = Lazy.of(symmetricEncryptionProvider);
            }
        }
        if (symmetricPresignerConfiguration.isValid()){
            symmetricPresigner = Lazy.of(() -> new SymmetricPresigner(symmetricPresignerConfiguration));
            presigners.add(symmetricPresigner);
        }

        if (encryptionProvider == null || decryptionProvider == null){
            throw new IllegalStateException("No encryption setting configured");
        }

        this.encryptionProvider = encryptionProvider;
        this.decryptionProvider = decryptionProvider;
        this.hashicorpVaultEncryptionProvider = hashicorpVaultEncryptionProvider == null
                ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                : hashicorpVaultEncryptionProvider;

        this.symmetricEncryptionProvider = symmetricEncryptionProvider == null
                ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                : symmetricEncryptionProvider;

        this.jwtService = jwtService == null
                ? Lazy.of(() -> new StandardJwtService(jwtConfiguration))
                : jwtService;

        this.uriPresigner = Lazy.of(() -> new UriPresigner(presigners.stream()
                .map(l -> (Presigner)l.get())
                .toList()));
        this.hashicorpVaultPresigner = hashicorpVaultPresigner == null
                ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                : hashicorpVaultPresigner;
        this.symmetricPresigner = symmetricPresigner == null
                ? Lazy.of(() -> { throw new UnsupportedOperationException("This encryption provider is not supported");})
                : symmetricPresigner;
    }

    @Bean
    public synchronized EncryptionProvider encryptionProvider(){
        return encryptionProvider.get();
    }

    @Bean
    public synchronized DecryptionProvider decryptionProvider(){
        return decryptionProvider.get();
    }

    @Bean
    public synchronized HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider(){
        return hashicorpVaultEncryptionProvider.get();
    }

    @Bean
    public synchronized SymmetricEncryptionProvider symmetricEncryptionProvider(){
        return symmetricEncryptionProvider.get();
    }

    @Bean
    public synchronized JwtIssuer jwtIssuer(){
        return jwtService.get();
    }

    @Bean
    public synchronized JwtVerifier jwtVerifier(){
        return jwtService.get();
    }

    @Bean
    public synchronized AsymmetricJwtVerifier asymmetricJwtVerifier(){
        return jwtService.get();
    }

    @Bean
    public synchronized UriPresigner uriPresigner(){
        return uriPresigner.get();
    }

    @Bean
    public synchronized HashicorpVaultPresigner hashicorpVaultPresigner(){
        return hashicorpVaultPresigner.get();
    }

    @Bean
    public synchronized SymmetricPresigner symmetricPresigner(){
        return symmetricPresigner.get();
    }
}
