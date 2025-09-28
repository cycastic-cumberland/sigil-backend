package net.cycastic.sigil;

import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.service.impl.HashicorpVaultPresigner;
import net.cycastic.sigil.service.impl.SymmetricPresigner;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PresignerTests {
    private final UriPresigner presigner;
    private final HashicorpVaultPresigner hashicorpVaultPresigner;
    private final SymmetricPresigner symmetricPresigner;

    @Autowired
    public PresignerTests(UriPresigner presigner, HashicorpVaultPresigner hashicorpVaultPresigner, SymmetricPresigner symmetricPresigner) {
        this.presigner = presigner;
        this.hashicorpVaultPresigner = hashicorpVaultPresigner;
        this.symmetricPresigner = symmetricPresigner;
    }

    private void signAndAssert(UriPresigner presigner, String url){
        var signedUrl = presigner.signUri(URI.create(url));
        assertTrue(presigner.verifyUri(signedUrl));
    }

    private void testPresignerSimpleInternal(UriPresigner presigner){
        final var url1 = "https://example.com/someData";
        final var url2 = "http://localhost:8080/api/storage?projectId=1&userId=1&shareToken=67883b4a-51c5-11f0-8381-0242ac140002&path=%2Fcat.gif#somestuff";

        signAndAssert(presigner, url1);
        signAndAssert(presigner, url2);

        assertThrows(IllegalStateException.class, () -> presigner.signUri(URI.create("https://example.com/someData?" + ApplicationConstants.PRESIGN_SIGNATURE_ENTRY + "=some+signature")));
    }

    @Test
    public void testPresignerAutowired(){
        testPresignerSimpleInternal(presigner);
    }

    @Test
    public void testPresignerHashicorpVault(){
        testPresignerSimpleInternal(new UriPresigner(hashicorpVaultPresigner));
    }

    @Test
    public void testPresignerSymmetric(){
        testPresignerSimpleInternal(new UriPresigner(symmetricPresigner));
    }
}
