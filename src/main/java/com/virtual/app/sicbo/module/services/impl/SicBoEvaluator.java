package com.virtual.app.sicbo.module.services.impl;

public class SicBoEvaluator {
    public static String evaluateResult(int[] dice) {

        int sum = dice[0] + dice[1] + dice[2];
        boolean isTriple = (dice[0] == dice[1]) && (dice[1] == dice[2]);

        if (isTriple) {
            return "t:"+sum; // All dice are the same (Triple)
        } else if (sum >= 4 && sum <= 10) {
            return "s:"+sum; // Sum between 4 and 10
        } else if (sum >= 11 && sum <= 17) {
            return "b:"+sum; // Sum between 11 and 17
        } else {
            return "INVALID"; // Should not happen based on Sic Bo rules
        }
    }
}
