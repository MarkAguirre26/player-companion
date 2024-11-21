package com.virtual.app.sicbo.module.helper;

public class TriggerFinder {

    public static boolean isEntryTriggerExists(String handResult, String entry) {
        return handResult.contains(entry);
    }


    public static boolean isStopTriggerExists(String handResult, String entry, String stopKey) {
        int wIndex = handResult.indexOf(entry);
        return wIndex != -1 && handResult.indexOf(stopKey, wIndex + 1) != -1;
    }


    public static boolean isGoodToBet(String handResult, String virtualWin) {

        String lastPart = getLastPart(handResult, virtualWin);
        if (lastPart.equals(virtualWin)) {
            return true;
        }
        return false;
    }


//    public static String getLastPart(String input, String key) {
//
//        int count = key.length();
//        // Validate input
//        if (!isValidInput(input, count)) {
//            return "";
//        }
//
//        int length = input.length();
//        char lastChar = input.charAt(length - 1);
//        StringBuilder result = new StringBuilder();
//
//        // Iterate backwards through the string and collect characters
//        for (int i = length - 1; i >= 0; i--) {
//            if (input.charAt(i) == lastChar) {
//                result.append(lastChar);
//                // Stop if we have collected enough characters
//                if (result.length() == count) {
//                    break;
//                }
//            } else {
//                // If we encounter a different character, update lastChar
//                lastChar = input.charAt(i);
//            }
//        }
//
//        // Return the result in reverse order
//        return result.reverse().toString();
//    }

    public static String getLastPart(String sequence, String key) {
        if (!isValidInput(sequence, key.length())) {
            return "";
        }
        return sequence.substring(sequence.length() - key.length());
    }

    private static boolean isValidInput(String input, int count) {
        return input != null && !input.isEmpty() && input.length() > count;
    }


}