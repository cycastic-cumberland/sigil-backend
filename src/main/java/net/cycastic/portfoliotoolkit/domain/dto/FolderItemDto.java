package net.cycastic.portfoliotoolkit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderItemDto {
    private String name;

    private FolderItemType type;
}
