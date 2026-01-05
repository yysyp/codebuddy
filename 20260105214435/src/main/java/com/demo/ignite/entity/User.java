package com.demo.ignite.entity;

import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cache.query.annotations.QueryTextField;

import java.io.Serializable;

/**
 * User Entity for Ignite Cache
 * This class represents a user that will be stored in the in-memory SQL cache
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @QuerySqlField(index = true)
    private Long id;

    @QuerySqlField
    private String username;

    @QuerySqlField
    private String email;

    @QuerySqlField
    private String fullName;

    @QueryTextField
    private String bio;

    @QuerySqlField
    private Integer age;

    @QuerySqlField
    private Boolean active;

    @QuerySqlField(index = true)
    private Long createdAt;

    /**
     * Default constructor required by Ignite
     */
    public User() {
    }

    /**
     * Constructor with all fields
     */
    public User(Long id, String username, String email, String fullName, String bio, Integer age, Boolean active, Long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.bio = bio;
        this.age = age;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", bio='" + bio + '\'' +
                ", age=" + age +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
