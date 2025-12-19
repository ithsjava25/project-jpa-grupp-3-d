package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_user")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CompanyUser {

    @EmbeddedId
    private CompanyUserId id;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public CompanyUser(User user, Company company) {
        this.id = new CompanyUserId(user.getId(), company.getId());
        this.user = user;
        this.company = company;
    }
}
