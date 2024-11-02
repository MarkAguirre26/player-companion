package com.virtual.app.sicbo.module.data.sicbo;

import com.fasterxml.jackson.annotation.JsonProperty;

// Class to represent lightning information
class Lightning {
    @JsonProperty("number")
    private int number;

    @JsonProperty("multiplier")
    private int multiplier;

    public Lightning(int number, int multiplier) {
        this.number = number;
        this.multiplier = multiplier;
    }

    public int getNumber() {
        return number;
    }

    public int getMultiplier() {
        return multiplier;
    }
}