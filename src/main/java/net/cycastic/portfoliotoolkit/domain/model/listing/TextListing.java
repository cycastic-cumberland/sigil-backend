package net.cycastic.portfoliotoolkit.domain.model.listing;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "text_listings")
public class TextListing {
    @Id
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Listing listing;

    @Column(columnDefinition = "VARCHAR(255)")
    private String textNormal;

    @Column(columnDefinition = "TEXT")
    private String textLong;
}
