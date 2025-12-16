package org.example.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
public class UserCompanyId implements Serializable {

    private UUID userId;
    private UUID companyId;
}
