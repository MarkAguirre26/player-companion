package com.virtual.app.sicbo.module.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class SecondOrderMarkovChainPrediction {

//    public static void main(String[] args) {
//        List<Integer> sequenceList = List.of(
//                12, 13, 13, 6, 9, 4, 15, 8, 3, 12, 11, 10, 11, 17, 12, 11, 7, 11, 8, 12, 10, 9, 8, 8, 7,
//                10, 11, 9, 11, 9, 11, 9, 7, 6, 9, 16, 7, 15, 9, 11, 7, 12, 13, 10, 7, 11, 10, 9, 10, 12, 17, 4, 10, 11, 11, 6, 12, 13, 8, 11, 11, 10,
//                16, 9, 12, 11, 17, 11, 6, 11, 11, 4, 7, 13, 12, 6, 10, 8, 10, 10, 9, 14, 15, 12, 11, 9, 11, 9,
//                7, 8, 11, 8, 8, 10, 11, 12, 13, 13, 6
//        );
//
//        // Convert List<Integer> to int[]
//        int[] sequence = convertListToArray(sequenceList);
//
//        String result = getTopThreeNumbers(sequence);
//        System.out.println(result);
//    }

    public static int[] convertListToArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static String getTopThreeNumbers(List<Integer> list) {

        int[] sequence = convertListToArray(list);

        // Step 1: Build the transition matrix for second-order Markov Chain
        Map<Integer, Map<Integer, Map<Integer, Integer>>> transitionCounts = new HashMap<>();

        for (int i = 0; i < sequence.length - 2; i++) {
            int prev1 = sequence[i];
            int prev2 = sequence[i + 1];
            int next = sequence[i + 2];

            // Ensure nested maps are initialized properly
            transitionCounts
                    .putIfAbsent(prev1, new HashMap<>());
            Map<Integer, Map<Integer, Integer>> secondMap = transitionCounts.get(prev1);
            secondMap.putIfAbsent(prev2, new HashMap<>());
            Map<Integer, Integer> counts = secondMap.get(prev2);

            // Increment the count for the next number
            counts.put(next, counts.getOrDefault(next, 0) + 1);
        }

        // Debugging: Print transition counts
//        System.out.println("Transition Counts: " + transitionCounts);

        // Step 2: Calculate probabilities
        Map<Integer, Map<Integer, Map<Integer, Double>>> transitionProbabilities = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Integer>>> entry : transitionCounts.entrySet()) {
            int prev1 = entry.getKey();
            Map<Integer, Map<Integer, Integer>> secondLevelMap = entry.getValue();

            for (Map.Entry<Integer, Map<Integer, Integer>> subEntry : secondLevelMap.entrySet()) {
                int prev2 = subEntry.getKey();
                Map<Integer, Integer> counts = subEntry.getValue();
                int total = counts.values().stream().mapToInt(Integer::intValue).sum();

                Map<Integer, Double> probabilities = new HashMap<>();
                for (Map.Entry<Integer, Integer> countEntry : counts.entrySet()) {
                    probabilities.put(countEntry.getKey(), (double) countEntry.getValue() / total);
                }

                // Properly initialize the nested map in transitionProbabilities
                transitionProbabilities
                        .putIfAbsent(prev1, new HashMap<>());
                transitionProbabilities.get(prev1).put(prev2, probabilities);
            }
        }

        // Debugging: Print transition probabilities
//        System.out.println("Transition Probabilities: " + transitionProbabilities);

        // Step 3: Predict the top 3 next states based on the last two numbers in the sequence
        int lastNumber1 = sequence[sequence.length - 2];
        int lastNumber2 = sequence[sequence.length - 1];

        PriorityQueue<Map.Entry<Integer, Double>> maxHeap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue())); // Max heap for top probabilities

        if (transitionProbabilities.containsKey(lastNumber1) &&
                transitionProbabilities.get(lastNumber1).containsKey(lastNumber2)) {
            for (Map.Entry<Integer, Double> entry : transitionProbabilities.get(lastNumber1).get(lastNumber2).entrySet()) {
                maxHeap.offer(entry);
            }
        }

        // Debugging: Check if the maxHeap is empty
        if (maxHeap.isEmpty()) {
            return "";
        }

        // Step 4: Get the top 3 predictions and build the response string
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && !maxHeap.isEmpty(); i++) {
            Map.Entry<Integer, Double> entry = maxHeap.poll();
            response.append(String.format("%d,", entry.getKey()));
        }

        return response.toString();
    }
}
