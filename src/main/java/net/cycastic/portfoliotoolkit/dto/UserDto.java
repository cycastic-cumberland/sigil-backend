package net.cycastic.portfoliotoolkit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.User;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String[] roles;
    private Integer projectLimit;
    private Integer lacpLimit;
    private OffsetDateTime joinedAt;

    public static UserDto fromDomain(@NotNull User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().split(","))
                .projectLimit(user.getProjectLimit())
                .lacpLimit(user.getLacpLimit())
                .joinedAt(user.getJoinedAt())
                .build();
    }
}
