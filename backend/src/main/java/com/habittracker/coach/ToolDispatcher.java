package com.habittracker.coach;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.habit.Habit;
import com.habittracker.habit.HabitCheckin;
import com.habittracker.habit.HabitCheckinService;
import com.habittracker.habit.HabitService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ToolDispatcher {

    private final HabitService habitService;
    private final HabitCheckinService habitCheckinService;
    private final ObjectMapper objectMapper;

    public ToolDispatcher(
            HabitService habitService, HabitCheckinService habitCheckinService, ObjectMapper objectMapper) {
        this.habitService = habitService;
        this.habitCheckinService = habitCheckinService;
        this.objectMapper = objectMapper;
    }

    public String dispatch(String toolName, Map<String, Object> input) {
        return switch (toolName) {
            case "get_habits" -> getHabits();
            case "add_habit" -> addHabit((String) input.get("name"));
            case "delete_habit" -> deleteHabit(toLong(input.get("habit_id")));
            case "set_checkin" -> setCheckin(toLong(input.get("habit_id")), (Boolean) input.get("done"));
            default -> "Unknown tool: " + toolName;
        };
    }

    private String setCheckin(Long habitId, boolean done) {
        HabitCheckin checkin = habitCheckinService.setToday(habitId, done);
        return writeJson(Map.of("habitId", habitId, "done", checkin.isDone()));
    }

    private String deleteHabit(Long habitId) {
        habitService.delete(habitId);
        return writeJson(Map.of("deleted", habitId));
    }

    private static Long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private String addHabit(String name) {
        Habit created = habitService.create(name);
        return writeJson(Map.of("id", created.getId(), "name", created.getName()));
    }

    private String getHabits() {
        List<Map<String, Object>> habits = habitService.listAll().stream()
                .map(habit -> Map.<String, Object>of(
                        "id", habit.getId(),
                        "name", habit.getName(),
                        "doneToday", habitCheckinService.getToday(habit.getId())))
                .toList();
        return writeJson(habits);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize tool result", e);
        }
    }
}
