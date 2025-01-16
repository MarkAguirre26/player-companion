package com.virtual.app.sicbo.module.controllers;

import com.virtual.app.sicbo.module.data.GameResponse;
import com.virtual.app.sicbo.module.services.impl.GameResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/game-responses")
public class GameResponseController {


    private final GameResponseService gameResponseService;

    @Autowired
    public GameResponseController(GameResponseService gameResponseService) {
        this.gameResponseService = gameResponseService;
    }

    @GetMapping
    public List<GameResponse> getAllGameResponses() {
        return gameResponseService.getAllGameResponses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGameResponseById(@PathVariable Integer id) {
        Optional<GameResponse> gameResponse = gameResponseService.getGameResponseById(id);
        return gameResponse.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<GameResponse> updateGameResponse(@PathVariable Integer id, @RequestBody GameResponse newResponse) {
        GameResponse updatedResponse = gameResponseService.updateGameResponse(id, newResponse);
        return ResponseEntity.ok(updatedResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameResponse(@PathVariable Integer id) {
        gameResponseService.deleteGameResponse(id);
        return ResponseEntity.noContent().build();
    }
}
