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
public class SicBoPragmaticPlayApiService {

    private final DiceService diceService;
    private final WebSocketMessageService messageService;
    private final RestTemplate restTemplate;
    private JsonNode lastData; // To store the last fetched data

    public SicBoPragmaticPlayApiService(DiceService diceService, WebSocketMessageService messageService, RestTemplate restTemplate) {
        this.diceService = diceService;
        this.messageService = messageService;
        this.restTemplate = restTemplate;
    }

    @Async
    public void fetchData() {
        // Replace with the actual URL and query parameters
        String url = "https://games.pragmaticplaylive.net/api/ui/statisticHistory?tableId=lc419kkmr2sxfpbk&numberOfGames=1&JSESSIONID=0-4Gx38P_cTvHTegYf9T3Kj6f9uT9CeAsycKFhxJ_BJRtFbvOlGw!-660677457-ffe9d71b&ck=1730807733278&game_mode=sicbo_desktop";

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "AWSALB=T1w8aKpGMb5DqH3g3P9LWBuMxy477K/G0HaEM5Shr6g5S/V+jjgBSqJsC3uQLfJRq+DSCcVtGS2MmFY962r8TjvOh2LBl2HKZf5bnyP42tHBuyyHWwissCM76hw6; AWSALBCORS=T1w8aKpGMb5DqH3g3P9LWBuMxy477K/G0HaEM5Shr6g5S/V+jjgBSqJsC3uQLfJRq+DSCcVtGS2MmFY962r8TjvOh2LBl2HKZf5bnyP42tHBuyyHWwissCM76hw6");

        // Create HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Make GET request to the API using RestTemplate
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());

            // Check if the request was successful
            if (rootNode.get("errorCode").asText().equals("0")) {
                JsonNode historyData = rootNode.get("history");

                // Process the first entry in the history array
                if (historyData != null && historyData.size() > 0) {
                    JsonNode gameData = historyData.get(0);

                    // Extract dice values
                    int die1 = gameData.get("die1").asInt();
                    int die2 = gameData.get("die2").asInt();
                    int die3 = gameData.get("die3").asInt();

                    int diceSum = die1 + die2 + die3;
                    String diceResult = SicBoEvaluator.evaluateResult(new int[]{die1, die2, die3});

                    // Check if the dice entry already exists in the database
                    Optional<Dice> diceResponse = diceService.getDiceByWhen(gameData.get("gameId").asText());

                    if (diceResponse.isEmpty()) {
                        try {
                            // Send message through WebSocket service
                            String message = "SicBo:" + diceResult;
                            messageService.sendMessage(message);
                            System.out.println("Message sent: " + message); // Debug logging
                        } catch (Exception e) {
                            System.err.println("Error sending message: " + e.getMessage());
                        }

                        // Create and save a new Dice object
                        Dice newDice = new Dice();
                        newDice.setWhen(gameData.get("gameId").asText());
                        newDice.setSum(diceSum);
                        newDice.setSize(diceResult);
                        newDice.setDiceOne(die1);
                        newDice.setDiceTwo(die2);
                        newDice.setDiceThree(die3);
                        newDice.setCreatedAt(LocalDateTime.now());
                        diceService.saveDice(newDice);

                        System.out.println(diceResult + " -> " + Arrays.toString(new int[]{die1, die2, die3}));
                    }
                }
            } else {
                System.err.println("Error in response: " + rootNode.get("description").asText());
            }
        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
        }
    }
}
