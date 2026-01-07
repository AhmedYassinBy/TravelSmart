package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Activity;
import com.ahmedyassin.TravelSmart.repositories.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    @Transactional
    public Activity create(Activity a){
        return activityRepository.save(a);
    }

    @Transactional
    public Activity update(UUID id, Activity updates){
        Activity existing = activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found"));
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setPrice(updates.getPrice());
        return activityRepository.save(existing);
    }

    public Optional<Activity> findById(UUID id){
        return activityRepository.findById(id);
    }

    public List<Activity> findAll(){
        return activityRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        activityRepository.deleteById(id);
    }
}

