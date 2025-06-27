package net.cycastic.portfoliotoolkit.service.impl;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.HashicorpVaultConfiguration;

public abstract class HashicorpVaultService {
    protected final Vault vault;
    protected final String keyName;

    protected HashicorpVaultService(VaultConfig vaultConfig, String keyName){
        this.vault = new Vault(vaultConfig);
        this.keyName = keyName;
    }

    @SneakyThrows
    protected static VaultConfig buildConfig(HashicorpVaultConfiguration configuration){
        return new VaultConfig()
                .address(configuration.getApiAddress())
                .token(configuration.getToken())
                .engineVersion(configuration.getApiVersion())
                .build();
    }
}
