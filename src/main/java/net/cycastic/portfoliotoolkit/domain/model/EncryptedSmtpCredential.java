package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "encrypted_smtp_credentials")
public class EncryptedSmtpCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @NotNull
    private String serverAddress;

    @NotNull
    private String secureSmtp;

    @NotNull
    private int port;

    @NotNull
    private int timeout;

    @NotNull
    private String fromAddress;

    @NotNull
    private String password;

    @NotNull
    private String fromName;

    @Version
    private long version;
}
