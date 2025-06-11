package net.cycastic.portfoliotoolkit.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveListingACPDto {
    @Nullable
    private String globPath;

    private boolean isAllowed;

    @Nullable
    private Integer project;

    @Nullable
    private Integer applyToId;
}
