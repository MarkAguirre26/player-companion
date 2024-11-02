package com.virtual.app.sicbo.module.controllers;

import com.virtual.app.sicbo.module.services.impl.DiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActiveUserController {

    //    private final SessionListener sessionListener;
    private final DiceService diceService;

    public ActiveUserController(DiceService diceService) {
        this.diceService = diceService;

    }

    @GetMapping("/active-users")
    public int getActiveUsers() {
        return diceService.getActiveUsers();
    }
}
