package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_company")
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class UserCompany {

    @EmbeddedId
    private UserCompanyId id;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public UserCompany(User user, Company company) {
        this.id = new UserCompanyId(user.getId(), company.getId());
        this.user = user;
        this.company = company;
    }
}
