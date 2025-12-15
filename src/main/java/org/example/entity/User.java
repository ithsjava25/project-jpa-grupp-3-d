package org.example.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String username;


    public User(String username){
        this.username = username;
    }

    public User() {

    }

    public UUID getId() {
        return id;
    }
}
