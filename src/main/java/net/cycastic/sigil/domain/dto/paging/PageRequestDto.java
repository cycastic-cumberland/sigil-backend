package net.cycastic.sigil.domain.dto.paging;

import jakarta.validation.constraints.Null;
import lombok.*;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {
    private static final int MAX_PAGE_SIZE = 1000;

    protected record Selector(String key, boolean isDescending){}
    private int page = 1;
    private int pageSize = 500;
    private @Null String orderBy;

    protected static @Null Selector toSelector(@NonNull String orderFragment){
        var split = orderFragment.split(":");
        if (split.length != 2){
            return null;
        }

        return new Selector(split[0], split[1].equalsIgnoreCase("desc"));
    }

    protected static Sort toSort(Selector selector){
        var sort = Sort.by(selector.key());
        return selector.isDescending() ? sort.descending() : sort.ascending();
    }

    protected @Null Sort getOrders(@NonNull String orderBy){
        return Arrays.stream(orderBy.split(","))
                .map(String::trim)
                .map(PageRequestDto::toSelector)
                .filter(Objects::nonNull)
                .map(PageRequestDto::toSort)
                .reduce(Sort::and)
                .orElse(getDefaultSort());
    }

    protected Sort getDefaultSort(){
        return Sort.by("id").ascending();
    }

    public Pageable toPageable(){
        if (pageSize > MAX_PAGE_SIZE){
            throw new RequestException(400, "Page size limit exceeded");
        }
        var sort = getOrders(orderBy == null ? "" : orderBy);

        if (sort != null){
            return PageRequest.of(page - 1, pageSize, sort);
        }

        return PageRequest.of(page - 1, pageSize);
    }
}
