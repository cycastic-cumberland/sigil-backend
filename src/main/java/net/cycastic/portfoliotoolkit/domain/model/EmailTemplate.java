package net.cycastic.portfoliotoolkit.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import org.springframework.lang.Nullable;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_templates")
public class EmailTemplate{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @Getter
    @Setter
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id", referencedColumnName = "id")
    private AttachmentListing attachmentListing;

    private String parameterString;

    @SneakyThrows
    public @Nullable EmailParameters getParameters(){
        if (parameterString == null){
            return null;
        }

        return MAPPER.readValue(parameterString, EmailParameters.class);
    }

    @SneakyThrows
    public void setParameters(@Nullable EmailParameters emailParameters){
        if (emailParameters == null){
            parameterString = null;
            return;
        }

        parameterString = MAPPER.writeValueAsString(emailParameters);
    }
}
