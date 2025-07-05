package net.cycastic.sigil.service.impl;

import jakarta.transaction.NotSupportedException;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.service.Presigner;

import java.net.URI;
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

    private static URI buildUri(String scheme, String rawAuthority, String rawPath, String rawQuery, String rawFragment){
        var sb = new StringBuilder()
                .append(scheme)
                .append("://")
                .append(rawAuthority)
                .append(rawPath);
        if (rawQuery != null){
            sb.append('?').append(rawQuery);
        }
        if (rawFragment != null){
            sb.append('#').append(rawFragment);
        }

        return URI.create(sb.toString());
    }

    @SneakyThrows
    public URI signUri(URI uri){
        var query = uri.getRawQuery();
        var queryParts = new ArrayList<String>();
        if (query != null) {
            // TODO: Regex?
            for (var param : query.split("&")) {
                if (param.startsWith(ApplicationConstants.PresignSignatureEntry + "=")) {
                    throw new IllegalStateException("URI already contains a signature parameter");
                }
                if (param.startsWith(ApplicationConstants.PresignSignatureAlgorithmEntry + "=")) {
                    throw new IllegalStateException("URI already contains a signature algorithm parameter");
                }

                queryParts.add(param);
            }
        }

        var baseUri = buildUri(uri.getScheme(),
                uri.getRawAuthority(),
                uri.getRawPath(),
                query,
                uri.getRawFragment());
        var dataToSign = baseUri.toString();
        var presigner = presigners.getFirst();
        var signature = presigner.getSignature(dataToSign, presigner.getDefaultAlgorithm());

        queryParts.add(ApplicationConstants.PresignSignatureEntry + "=" + ApplicationUtilities.encodeURIComponent(signature));
        queryParts.add(ApplicationConstants.PresignSignatureAlgorithmEntry + "=" + ApplicationUtilities.encodeURIComponent(presigner.getDefaultAlgorithm()));

        query = String.join("&", queryParts);
        return buildUri(uri.getScheme(), uri.getRawAuthority(), uri.getRawPath(), query, uri.getFragment());
    }

    @SneakyThrows
    public boolean verifyUri(URI uri){
        var query = uri.getRawQuery();
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
                    signatures.add(ApplicationUtilities.decodeURIComponent(parts[1]));
                }
            } else if (param.startsWith(ApplicationConstants.PresignSignatureAlgorithmEntry + "=")){
                var parts = param.split("=", 2);
                if (parts.length == 2) {
                    algorithms.add(ApplicationUtilities.decodeURIComponent(parts[1]));
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
        var baseUri = buildUri(uri.getScheme(), uri.getRawAuthority(), uri.getRawPath(), newQuery, uri.getRawFragment());
        var dataToVerify = baseUri.toString();

        var presigner = presigners.stream()
                .filter(p -> p.canSupport(foundAlgo))
                .findFirst();
        return presigner.orElseThrow(() -> new NotSupportedException(String.format("Unsupported presign algorithm: %s", foundAlgo)))
                .verifySignature(dataToVerify, foundSignature, foundAlgo);
    }
}
