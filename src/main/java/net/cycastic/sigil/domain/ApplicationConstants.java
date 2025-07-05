package net.cycastic.sigil.domain;

import java.util.HashSet;
import java.util.Set;

public class ApplicationConstants {
    public static class Roles {
        public static final String COMMON;
        public static final String ADMIN;
        public static final Set<String> ALL_ROLES;

        static {
            COMMON = "COMMON";
            ADMIN = "ADMIN";
            ALL_ROLES = new HashSet<>();
            ALL_ROLES.add(COMMON);
            ALL_ROLES.add(ADMIN);
        }
    }

    public static final String PROJECT_ID_HEADER = "X-Project-Id";
    public static final String ROLES_ENTRY = "roles";
    public static final String SECURITY_STAMP_ENTRY = "security_stamp";
    public static final String PresignSignatureEntry = "x-pt-signature";
    public static final String PresignSignatureAlgorithmEntry = "x-pt-algorithm";
    public static final long REFRESH_TOKEN_TIME_MILLISECONDS = 1_209_600_000;
}
