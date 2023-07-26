package com.example.spitest.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Editor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Editor() {

    }

}
