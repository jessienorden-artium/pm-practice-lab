package com.habittracker.habit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HabitCheckinController {

    private final HabitCheckinService habitCheckinService;

    public HabitCheckinController(HabitCheckinService habitCheckinService) {
        this.habitCheckinService = habitCheckinService;
    }

    @PutMapping("/api/habits/{id}/checkins/today")
    public CheckinResponse putToday(@PathVariable Long id, @RequestBody CheckinRequest request) {
        HabitCheckin checkin = habitCheckinService.setToday(id, request.done());
        return new CheckinResponse(checkin.isDone());
    }

    @GetMapping("/api/habits/{id}/checkins/today")
    public CheckinResponse getToday(@PathVariable Long id) {
        return new CheckinResponse(habitCheckinService.getToday(id));
    }
}
