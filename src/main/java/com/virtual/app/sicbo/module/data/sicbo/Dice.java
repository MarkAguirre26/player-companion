package com.virtual.app.sicbo.module.data.sicbo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "dice")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Dice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dice_id")
    private Integer diceId;

    @Column(name = "`when`", length = 50)
    private String when;

    @Column(name = "sum")
    private Integer sum;


    @Column(name = "`size`", length = 50)
    private String size;

    @Column(name = "dice_one")
    private Integer diceOne;

    @Column(name = "dice_two")
    private Integer diceTwo;

    @Column(name = "dice_three")
    private Integer diceThree;

    @Column(name = "`created_at`", length = 50)
    private LocalDateTime createdAt;
}
