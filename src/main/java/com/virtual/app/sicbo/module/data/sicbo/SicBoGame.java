package com.virtual.app.sicbo.module.data.sicbo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Model class to represent a SicBo game
public class SicBoGame {
    @JsonProperty("when")
    private long when;

    @JsonProperty("dice")
    private int[] dice;

    @JsonProperty("lightnings")
    private List<Lightning> lightnings;

    @JsonProperty("total_winners")
    private int totalWinners;

    @JsonProperty("total_payout")
    private double totalPayout;

    public int[] getDice() {
        return dice;
    }

    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        return "When: " + when + ", Dice: [" + dice[0] + ", " + dice[1] + ", " + dice[2] + "]";
    }
}

