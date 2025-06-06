package net.cycastic.portfoliotoolkit.dto.paging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private Collection<T> items;
    private int page;
    private int pageSize;
    private int totalPages;
    private int totalElements;

    public static <T, U> PageResponseDto<U> fromDomain(Page<T> page, Function<? super T, ? extends U> converter){
        var items = page.getContent().stream()
                .map(r -> (U)converter.apply(r))
                .toList();
        return new PageResponseDto<U>(items, page.getNumber() + 1, page.getSize(), page.getTotalPages(), (int) page.getTotalElements());
    }

    public static <T> PageResponseDto<T> fromDomain(Page<T> page){
        return fromDomain(page, r -> r);
    }
}
