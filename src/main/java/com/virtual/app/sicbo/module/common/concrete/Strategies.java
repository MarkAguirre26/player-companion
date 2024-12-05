package com.virtual.app.sicbo.module.common.concrete;

import lombok.Getter;

@Getter
public enum Strategies {


    RLIZA("RLIZA"),
    TREND_OF_TWO("TREND_OF_TWO"),
    TREND_OF_THREE("TREND_OF_THREE"),
    FREEHAND("FREEHAND"),
    STOCHASTIC("STOCHASTIC"),
    KISS_123("KISS_123"),
    ALL_RED("ALL_RED"),
    KISS_MODIFIED("KISS_MODIFIED"),
    FLAT("FLAT"),
    HIGH("HIGH"),
    DUMMY("DUMMY"),
    hybrid("hybrid"),
    RGP("RGP"),
    REVERSE_LABOUCHERE("REVERSE_LABOUCHERE"),
    STRATEGY("STRATEGY");
   
//    BASE_BET("BASE_BET");

    private final String value;

    Strategies(String value) {
        this.value = value;
    }

}
