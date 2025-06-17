package net.cycastic.portfoliotoolkit.domain.dto.paging;

import jakarta.validation.constraints.Null;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {
    private record Selector(String key, boolean isDescending){}
    private int page = 1;
    private int pageSize = 500;
    private @Null String orderBy;

    private static @Null Selector toSelector(@NonNull String string){
        var split = string.split(":");
        if (split.length != 2){
            return null;
        }

        return new Selector(split[0], split[1].equalsIgnoreCase("desc"));
    }

    private static @Null Sort getOrders(@NonNull String orderBy){
        var selectors = Arrays.stream(orderBy.split(","))
                .map(String::trim)
                .map(PageRequestDto::toSelector)
                .filter(Objects::nonNull)
                .map(s -> {
                    var sort = Sort.by(s.key());
                    return s.isDescending() ? sort.descending() : sort.ascending();
                })
                .toList();
        if (selectors.isEmpty()){
            return Sort.by("id").ascending();
        }

        return selectors.stream().reduce(Sort::and).orElse(null);
    }

    public Pageable toPageable(){
        Sort sort = null;
        if (orderBy != null){
            sort = getOrders(orderBy);
        }

        if (sort != null){
            return PageRequest.of(page - 1, pageSize, sort);
        }

        return PageRequest.of(page - 1, pageSize);
    }
}
