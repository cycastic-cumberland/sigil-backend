package net.cycastic.portfoliotoolkit.service.impl;

import jakarta.transaction.NotSupportedException;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.service.Presigner;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UriPresigner {
    private final List<Presigner> presigners;

    public UriPresigner(Presigner presigner){
        this.presigners = Collections.singletonList(presigner);
    }

    public UriPresigner(List<Presigner> presigners){
        if (presigners.isEmpty()){
            throw new IllegalStateException();
        }
        this.presigners = presigners;
    }

    @SneakyThrows
    public URI signUri(URI uri){
        var query = uri.getQuery();
        if (query != null) {
            for (var param : query.split("&")) {
                if (param.startsWith(ApplicationConstants.PresignSignatureEntry + "=")) {
                    throw new IllegalStateException("URI already contains a signature parameter");
                }
                if (param.startsWith(ApplicationConstants.PresignSignatureAlgorithmEntry + "=")) {
                    throw new IllegalStateException("URI already contains a signature algorithm parameter");
                }
            }
        }

        var baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, null);
        var dataToSign = baseUri.toString();
        var presigner = presigners.getFirst();
        var signature = presigner.getSignature(dataToSign, presigner.getDefaultAlgorithm());
        var encodedSignature = URLEncoder.encode(signature, StandardCharsets.UTF_8);
        var encodedAlgorithm = URLEncoder.encode(presigner.getDefaultAlgorithm(), StandardCharsets.UTF_8);

        var newQuery = ((query == null || query.isEmpty()) ?
                ApplicationConstants.PresignSignatureEntry + "=" + encodedSignature :
                query + "&"+ ApplicationConstants.PresignSignatureEntry + "=" + encodedSignature)
                + "&" + ApplicationConstants.PresignSignatureAlgorithmEntry + "=" + encodedAlgorithm;

        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
    }

    @SneakyThrows
    public boolean verifyUri(URI uri){
        String query = uri.getQuery();
        if (query == null) {
            return false;
        }

        var otherParams = new ArrayList<String>();
        var signatures = new ArrayList<String>();
        var algorithms = new ArrayList<String>();
        for (var param : query.split("&")) {
            if (param.startsWith(ApplicationConstants.PresignSignatureEntry + "=")) {
                var parts = param.split("=", 2);
                if (parts.length == 2) {
                    var decodedSig = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    signatures.add(decodedSig);
                }
            } else if (param.startsWith(ApplicationConstants.PresignSignatureAlgorithmEntry + "=")){
                var parts = param.split("=", 2);
                if (parts.length == 2) {
                    var decodedAlgo = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    algorithms.add(decodedAlgo);
                }
            } else {
                otherParams.add(param);
            }
        }

        if (signatures.isEmpty() || algorithms.isEmpty()) {
            return false;
        }
        if (signatures.size() > 1 || algorithms.size() > 1) {
            return false;
        }
        var foundSignature = signatures.getFirst();
        var foundAlgo = algorithms.getFirst();

        var newQuery = otherParams.isEmpty() ? null : String.join("&", otherParams);
        var baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, null);
        var dataToVerify = baseUri.toString();

        var presigner = presigners.stream()
                .filter(p -> p.canSupport(foundAlgo))
                .findFirst();
        return presigner.orElseThrow(() -> new NotSupportedException(String.format("Unsupported presign algorithm: %s", foundAlgo)))
                .verifySignature(dataToVerify, foundSignature, foundAlgo);
    }
}
