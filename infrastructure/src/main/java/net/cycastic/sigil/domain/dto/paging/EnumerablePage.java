package net.cycastic.sigil.domain.dto.paging;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumerablePage<T> {
    private List<T> items;
    private String prevToken;
    private String nextToken;
}
