package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;

import java.util.List;

@Data
@Builder
public class PartitionUserDto {
    private String email;
    private String firstName;
    private String lastName;
    private String avatarToken;
    private List<String> permissions;

    public static PartitionUserDto fromDomain(PartitionUserRepository.PartitionUserResult p){
        return PartitionUserDto.builder()
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .email(p.getEmail())
                .avatarToken(p.getAvatarToken())
                .permissions(ApplicationConstants.PartitionPermissions.toReadablePermissions(p.getPermissions()))
                .build();
    }
}
