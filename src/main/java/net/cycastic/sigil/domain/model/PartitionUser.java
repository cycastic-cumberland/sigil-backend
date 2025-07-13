package net.cycastic.sigil.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import static net.cycastic.sigil.domain.ApplicationConstants.PartitionPermissions.MODERATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partition_users", indexes = @Index(name = "partition_users_partition_id_user_id_uindex", columnList = "partition_id,user_id", unique = true))
public class PartitionUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="partition_id", nullable = false)
    private Partition partition;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "partition_user_key_id", nullable = false)
    private Cipher partitionUserKey;

    private int permissions;

    @Formula("permissions & " + MODERATE + " = " + MODERATE)
    private boolean isModerator;

    @Version
    private long version;
}
