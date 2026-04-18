package com.cpt202.group8.heritage.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagId")
    private Long id;

    @Column(name = "tagName", nullable = false, unique = true)
    private String tagName;

    public Long getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}