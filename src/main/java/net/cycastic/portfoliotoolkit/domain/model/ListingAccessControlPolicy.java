package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.NsoUtilities;

import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "listing_access_control_policies")
public class ListingAccessControlPolicy {
    private static final byte[] highestSearchKey;

    static {
        highestSearchKey = new byte[NsoUtilities.KEY_LENGTH];
        Arrays.fill(highestSearchKey, (byte)1);
    }

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Null
    @Column(columnDefinition = "VARBINARY(255)")
    private byte[] lowSearchKey;

    @Null
    @Column(columnDefinition = "VARBINARY(255)")
    private byte[] highSearchKey = highestSearchKey.clone();

    private int priority;

    private boolean isAllowed;

    @NotNull
    @ManyToOne
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Null
    @ManyToOne
    @JoinColumn(name="apply_to_id")
    private User applyTo; // null for everyone
}
