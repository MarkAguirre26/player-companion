package com.virtual.app.sicbo.module.services;


import com.virtual.app.sicbo.module.data.TrailingStop;
import com.virtual.app.sicbo.module.repository.TrailingStopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrailingStopService {

    private final TrailingStopRepository trailingStopRepository;

    @Autowired
    public TrailingStopService(TrailingStopRepository trailingStopRepository) {
        this.trailingStopRepository = trailingStopRepository;
    }

    // Create or update a TrailingStop
    public TrailingStop saveOrUpdate(TrailingStop trailingStop) {
//        TrailingStop existingTrailingStop = trailingStopRepository.findByUserUuid(trailingStop.getUserUuid());
//        if (existingTrailingStop == null) {
//            return trailingStopRepository.save(trailingStop);
//        } else {
//            trailingStop.setInitial(existingTrailingStop.getInitial());
//            trailingStop.setTrailingStopId(existingTrailingStop.getTrailingStopId());
//            trailingStop.setStopProfit(existingTrailingStop.getStopProfit());
        return trailingStopRepository.save(trailingStop);
//        }
    }

    public TrailingStop getTrailingStopByUserUuid(String userUuid) {
        return trailingStopRepository.findByUserUuidLimit1(userUuid);
    }


    public void deleteTrailingStopByUserUuid(String userUuid) {
        TrailingStop trailingStop = trailingStopRepository.findByUserUuidLimit1(userUuid);
        if (trailingStop != null) {
            trailingStopRepository.deleteById(trailingStop.getTrailingStopId());
        }
    }

    // Get a TrailingStop by its ID
    public Optional<TrailingStop> getTrailingStopById(Integer id) {
        return trailingStopRepository.findById(id);
    }

    // Get all TrailingStop records
    public List<TrailingStop> getAllTrailingStops() {
        return trailingStopRepository.findAll();
    }

    // Delete a TrailingStop by its ID
    public void deleteTrailingStopById(Integer id) {
        trailingStopRepository.deleteById(id);
    }
}
