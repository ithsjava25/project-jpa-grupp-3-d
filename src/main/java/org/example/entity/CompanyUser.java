package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_user")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CompanyUser {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CompanyUserId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    private Company company;

    public CompanyUser(User user, Company company) {
        if(user.getId() == null || company.getId() == null) {
            throw new IllegalArgumentException("User and Company must have IDs set");}
        this.id = new CompanyUserId(user.getId(), company.getId());
        this.user = user;
        this.company = company;
    }
}
