package net.cycastic.portfoliotoolkit.domain.model.listing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.EmailTemplate;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attachment_listings")
public class AttachmentListing {
    @Id
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Listing listing;

    @Column(columnDefinition = "VARCHAR(32)")
    private String bucketName;

    @Column(columnDefinition = "VARCHAR(16)")
    private String bucketRegion;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String objectKey;

    @Column(columnDefinition = "VARCHAR(255)")
    private String mimeType;

    private boolean uploadCompleted;

    @Column(nullable = false)
    private UUID shareToken;

    @Version
    private long version;

    @OneToOne(mappedBy = "attachmentListing")
    private EmailTemplate emailTemplate;
}
