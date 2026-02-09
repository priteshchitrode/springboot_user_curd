package com.priteshchittrode.user_crud.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Basic Info
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // ✅ Login / Auth
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String password;

    // ✅ JWT Token (optional: useful for logout / tracking sessions)
    @Column(length = 500)
    @JsonIgnore
    private String refreshToken;

    // ✅ Contact Info
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    // ✅ Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
