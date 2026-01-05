package org.example.entity.client;

import jakarta.persistence.*;
import lombok.*;
import org.example.entity.company.Company;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String address;
    private String city;
    private String country;
    private String phoneNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public static Client fromDTO(CreateClientDTO dto, Company company) {
        return Client.builder()
            .company(company)
            .firstName(dto.firstName())
            .lastName(dto.lastName())
            .email(dto.email())
            .address(dto.address())
            .city(dto.city())
            .country(dto.country())
            .phoneNumber(dto.phoneNumber())
            .build();
    }
}
