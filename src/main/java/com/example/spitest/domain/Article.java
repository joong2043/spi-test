package com.example.spitest.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    public Article() {

    }

    public Article(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateContent(String editContent) {
        this.content = editContent;
    }

    public String getContent() {
        return this.content;
    }
}
