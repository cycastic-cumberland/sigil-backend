package net.cycastic.sigil.domain.model.pm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.VersionedMetadataEntity;
import net.cycastic.sigil.domain.model.tenant.User;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_comments", indexes = {
        @Index(name = "task_comments_task_id_created_at_index", columnList = "task_id,created_at")
})
@EqualsAndHashCode(callSuper = true)
public class TaskComment extends VersionedMetadataEntity {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private byte[] encryptedContent;

    @Column(columnDefinition = "BINARY(12)", nullable = false)
    private byte[] iv;

    @ManyToOne
    @JoinColumn(name = "cipher_id", nullable = false)
    private Cipher cipher;
}
