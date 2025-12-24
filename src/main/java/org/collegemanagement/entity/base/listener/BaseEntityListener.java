package org.collegemanagement.entity.base.listener;


import jakarta.persistence.PrePersist;
import org.collegemanagement.entity.base.BaseEntity;

import java.util.UUID;

public class BaseEntityListener {

    @PrePersist
    public void onCreate(BaseEntity entity) {
        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID().toString());
        }
    }
}

