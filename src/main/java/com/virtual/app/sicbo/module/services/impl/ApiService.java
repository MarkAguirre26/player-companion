package com.virtual.app.sicbo.module.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual.app.sicbo.module.data.sicbo.Dice;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class ApiService {

    private final DiceService diceService;
    private final WebSocketMessageService messageService;
    private final RestTemplate restTemplate;
    private JsonNode lastData; // To store the last fetched data

    public ApiService(DiceService diceService, WebSocketMessageService messageService, RestTemplate restTemplate) {
        this.diceService = diceService;
        this.messageService = messageService;
        this.restTemplate = restTemplate;
    }

    @Async
    public void fetchData() {


        String url = "https://api.tracksino.com/sicbo_history?sort_by=&sort_desc=false&page_num=1&per_page=10&period=24hours&table_id=149";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer 35423482-f852-453c-97a4-4f5763f4796f");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode currentData = mapper.readTree(response.getBody()).get("data");

            if (lastData == null || !lastData.equals(currentData)) {

                lastData = currentData;

                // Iterate through the JsonNode array and extract dice values
                for (JsonNode game : currentData) {
                    // Get the dice values as an array
                    JsonNode diceWhen = game.get("when");

                    JsonNode diceNode = game.get("dice");
                    int[] dice = new int[diceNode.size()];

                    // Fill the array with dice values
                    for (int i = 0; i < diceNode.size(); i++) {
                        dice[i] = diceNode.get(i).asInt();
                    }

                    int diceSum = dice[0] + dice[1] + dice[2];
                    Optional<Dice> diceResponse = diceService.getDiceByWhen(diceWhen.asText());

                    if (diceResponse.isEmpty()) {
                        String diceResult = "";
                        try {
                            diceResult = SicBoEvaluator.evaluateResult(dice);
                            System.out.println("Evaluated result: " + diceResult); // Debug logging

                            String message = "SicBo:" + diceResult;
                            messageService.sendMessage(message);
                            System.out.println("Message sent: " + message); // Debug logging
                        } catch (Exception e) {
                            System.err.println("Error evaluating result or sending message: " + e.getMessage());
                        }


                        // Create and save a new Dice object
                        Dice newDice = new Dice();
                        newDice.setWhen(diceWhen.asText());
                        newDice.setSum(diceSum);
                        newDice.setSize(diceResult);
                        newDice.setDiceOne(dice[0]);
                        newDice.setDiceTwo(dice[1]);
                        newDice.setDiceThree(dice[2]);
                        newDice.setCreatedAt(LocalDateTime.now());
                        diceService.saveDice(newDice);

                        System.out.println(diceResult + " -> " + Arrays.toString(dice));

                        break;
                    }
                }


            }
        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
        }
    }


}
