package net.cycastic.portfoliotoolkit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderItemsDto {
    private Collection<FolderItemDto> items;
}
