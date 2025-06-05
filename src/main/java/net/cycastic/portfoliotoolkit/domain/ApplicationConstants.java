package net.cycastic.portfoliotoolkit.domain;

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
}
