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

    public static class TenantPermissions {
        public static int MEMBER = 0;
        public static int CREATE_PARTITIONS = 1;
        public static int DELETE_PARTITIONS = 2;
        public static int MODERATOR = 4;
    }

    public static class PartitionPermissions {
        public static int READ = 0;
        public static int WRITE = 1;
        public static int MODERATE = 2;
    }

    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String PARTITION_ID_HEADER = "X-Partition-Id";
    public static final String ENCRYPTION_KEY_HEADER = "X-Encryption-Key";
    public static final String ROLES_ENTRY = "roles";
    public static final String SECURITY_STAMP_ENTRY = "security_stamp";
    public static final String PresignSignatureEntry = "x-pt-signature";
    public static final String PresignSignatureAlgorithmEntry = "x-pt-algorithm";
    public static final long REFRESH_TOKEN_TIME_MILLISECONDS = 1_209_600_000;
}
