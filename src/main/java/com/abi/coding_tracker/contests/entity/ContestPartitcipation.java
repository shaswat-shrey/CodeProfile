package com.abi.coding_tracker.contests.entity;

import com.abi.coding_tracker.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contest_participations")
@Getter
@Setter
@NoArgsConstructor
public class ContestPartitcipation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "contest_id")
    private String contestId;
    
    @Column(name = "platform")
    private String platform;
    
    @Column(name = "rank")
    private Integer rank;
    
    @Column(name = "rating_change")
    private Integer ratingChange;
    
    @Column(name = "attended")
    private boolean attended;
}
