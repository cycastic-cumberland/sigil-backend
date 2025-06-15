package net.cycastic.portfoliotoolkit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentPresignedUploadDto {
    private Integer id;

    private String url;
}
