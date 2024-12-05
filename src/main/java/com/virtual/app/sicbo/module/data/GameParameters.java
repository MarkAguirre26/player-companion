package com.virtual.app.sicbo.module.data;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_parameters")
@Data // Lombok annotation to generate getters, setters, equals, hashcode, and toString
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Enables the builder pattern
public class GameParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameter_id")
    private Integer parameterId;

    @Column(name = "user_uuid", length = 254)
    private String userUuid;

    @Column(name = "stop_loss")
    private Integer stopLoss;

    @Column(name = "stop_profit")
    private Integer stopProfit;

    @Column(name = "trailing_stop_spread")
    private Integer trailingStopSpread;

    @Column(name = "trailing_stop_activation")
    private Integer trailingStopActivation;

    @Column(name = "money_management", length = 254)
    private String moneyManagement;

    @Column(name = "strategy", length = 100)
    private String strategy;

    @Column(name = "is_shield")
    private Integer isShield;

    @Column(name = "sensitivity")
    private Integer sensitivity;

    @Column(name = "stop_trigger")
    private Integer stopTrigger;

    @Column(name = "virtual_win")
    private Integer virtualWin;


    @Column(name = "is_compounding")
    private Integer isCompounding;


    @Column(name = "starting_fund")
    private Integer startingFund;

    @Column(name = "current_fund")
    private Integer currentFund;

    @Column(name = "daily_goal_percentage")
    private Integer dailyGoalPercentage;

    @Column(name = "bet_amount")
    private Integer betAmount;

    @Column(name = "created_at", columnDefinition = "datetime(2)")
    private LocalDateTime createdAt;
}
