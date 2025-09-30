package com.example.demo.store.entity;

import com.example.demo.benefit.application.dto.ChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channelType;

    @Column(nullable = false)
    private boolean isActive = true;
    
    
    @Builder
    public Store(Long id, ChannelType channelType, boolean isActive) {
        this.id = id;
        this.channelType = channelType;
        this.isActive = isActive;
    }
}

