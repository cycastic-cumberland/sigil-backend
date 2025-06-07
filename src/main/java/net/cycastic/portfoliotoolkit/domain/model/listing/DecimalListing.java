package net.cycastic.portfoliotoolkit.domain.model.listing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "decimal_listings")
public class DecimalListing {
    @Id
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Listing listing;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal number;
}
