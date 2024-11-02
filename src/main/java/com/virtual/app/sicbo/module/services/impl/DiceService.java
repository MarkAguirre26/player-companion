package com.virtual.app.sicbo.module.services.impl;


import com.virtual.app.sicbo.module.data.sicbo.Dice;
import com.virtual.app.sicbo.module.repository.DiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiceService {

    private final DiceRepository diceRepository;

    @Autowired
    public DiceService(DiceRepository diceRepository) {
        this.diceRepository = diceRepository;
    }

    public List<Dice> getAllDice() {
        return diceRepository.findAll();
    }

    public Optional<Dice> getDiceById(Integer id) {
        return diceRepository.findById(id);
    }

    public Optional<Dice> getDiceByWhen(String when) {
        return diceRepository.findByWhen(when);
    }

    public Dice saveDice(Dice dice) {
        return diceRepository.save(dice);
    }


    public void deleteDice(Integer id) {
        diceRepository.deleteById(id);
    }

    public Integer getActiveUsers() {
        return diceRepository.getActiveUsers();
    }

    public List<String> findDiceSizeLimit20() {
        List<Dice> diceList = diceRepository.findDiceToday();
        List<String> dices = new ArrayList<>();


        diceList.forEach(dice -> {
            String[] d = dice.getSize().split(":");
            dices.add(d[0]);
        });

        return dices;
    }
}
