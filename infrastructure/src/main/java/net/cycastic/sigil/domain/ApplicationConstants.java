package net.cycastic.sigil.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        public static final int MEMBER = 0;
        public static final int CREATE_PARTITIONS = 1;
        public static final int DELETE_PARTITIONS = 2;
        public static final int MODERATE = 4;
        public static final int LIST_USERS = 8;

        public static List<String> toReadablePermissions(int permissions){
            var readablePermissions = new ArrayList<String>();
            readablePermissions.add("MEMBER");
            if ((permissions & CREATE_PARTITIONS) == CREATE_PARTITIONS){
                readablePermissions.add("CREATE_PARTITIONS");
            }
            if ((permissions & DELETE_PARTITIONS) == DELETE_PARTITIONS){
                readablePermissions.add("DELETE_PARTITIONS");
            }
            if ((permissions & MODERATE) == MODERATE){
                readablePermissions.add("MODERATE");
            }
            if ((permissions & LIST_USERS) == LIST_USERS){
                readablePermissions.add("LIST_USERS");
            }

            return readablePermissions;
        }
    }

    public static class PartitionPermissions {
        public static final int READ = 0;
        public static final int WRITE = 1;
        public static final int MODERATE = 2;

        public static List<String> toReadablePermissions(int permissions){
            var readablePermissions = new ArrayList<String>();
            readablePermissions.add("READ");
            if ((permissions & WRITE) == WRITE){
                readablePermissions.add("WRITE");
            }
            if ((permissions & MODERATE) == MODERATE){
                readablePermissions.add("MODERATE");
            }

            return readablePermissions;
        }
    }

    public enum Entitlements {
        PROJECT_PARTITION,
    }

    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String PARTITION_ID_HEADER = "X-Partition-Id";
    public static final String ENCRYPTION_KEY_HEADER = "X-Encryption-Key";
    public static final String ROLES_ENTRY = "roles";
    public static final String SECURITY_STAMP_ENTRY = "security_stamp";
    public static final String PRESIGN_SIGNATURE_ENTRY = "x-pt-signature";
    public static final String PRESIGN_SIGNATURE_ALG_ENTRY = "x-pt-algorithm";
    public static final String NEW_NOTIFICATION_EVENT_TYPE = "NEW_NOTIFICATION";
    public static final long REFRESH_TOKEN_TIME_MILLISECONDS = 1_209_600_000;
    public static final String REMOTE_SQS_DIRECTORY_ROOT = "email";
}
