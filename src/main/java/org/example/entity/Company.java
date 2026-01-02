package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.entity.client.Client;
import org.example.entity.invoice.Invoice;
import org.example.entity.user.CompanyUser;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Company {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_num", nullable = false, unique = true)
    private String orgNum;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String name;
    private String address;
    private String city;
    private String country;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Client> clients = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Invoice> invoices = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<CompanyUser> companyUsers = new HashSet<>();

    public static Company fromDto(org.example.dto.CompanyDTO dto) {
        return Company.builder()
            .name(dto.name())
            .orgNum(dto.orgNum())
            .email(dto.email())
            .phoneNumber(dto.phoneNumber())
            .address(dto.address())
            .city(dto.city())
            .country(dto.country())
            .build();
    }
}
