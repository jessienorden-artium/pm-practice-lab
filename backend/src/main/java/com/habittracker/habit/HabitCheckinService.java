package com.habittracker.habit;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HabitCheckinService {

    private final HabitCheckinRepository habitCheckinRepository;
    private final HabitRepository habitRepository;

    public HabitCheckinService(HabitCheckinRepository habitCheckinRepository, HabitRepository habitRepository) {
        this.habitCheckinRepository = habitCheckinRepository;
        this.habitRepository = habitRepository;
    }

    public boolean getToday(Long habitId) {
        return habitCheckinRepository
                .findByHabitIdAndCheckinDate(habitId, LocalDate.now())
                .map(HabitCheckin::isDone)
                .orElse(false);
    }

    public HabitCheckin setToday(Long habitId, boolean done) {
        if (!habitRepository.existsById(habitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "habit not found: " + habitId);
        }
        LocalDate today = LocalDate.now();
        HabitCheckin checkin = habitCheckinRepository
                .findByHabitIdAndCheckinDate(habitId, today)
                .orElseGet(() -> new HabitCheckin(habitId, today, done));
        checkin.setDone(done);
        return habitCheckinRepository.save(checkin);
    }
}
