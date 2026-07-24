package com.habittracker.coach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.habit.Habit;
import com.habittracker.habit.HabitCheckin;
import com.habittracker.habit.HabitCheckinService;
import com.habittracker.habit.HabitService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToolDispatcherTest {

    @Mock
    private HabitService habitService;

    @Mock
    private HabitCheckinService habitCheckinService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getHabits_returnsEachHabitWithIdNameAndTodayStatus() throws Exception {
        Habit drinkWater = mock(Habit.class);
        when(drinkWater.getId()).thenReturn(1L);
        when(drinkWater.getName()).thenReturn("Drink water");
        when(habitService.listAll()).thenReturn(List.of(drinkWater));
        when(habitCheckinService.getToday(1L)).thenReturn(true);

        ToolDispatcher dispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);
        String result = dispatcher.dispatch("get_habits", Map.of());

        JsonNode habits = objectMapper.readTree(result);
        assertThat(habits.get(0).get("name").asText()).isEqualTo("Drink water");
        assertThat(habits.get(0).get("doneToday").asBoolean()).isTrue();
    }

    @Test
    void addHabit_callsHabitServiceCreateWithGivenName() {
        Habit created = mock(Habit.class);
        when(created.getId()).thenReturn(9L);
        when(created.getName()).thenReturn("Drink water");
        when(habitService.create("Drink water")).thenReturn(created);

        ToolDispatcher dispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);
        dispatcher.dispatch("add_habit", Map.of("name", "Drink water"));

        verify(habitService).create("Drink water");
    }

    @Test
    void deleteHabit_callsHabitServiceDeleteWithGivenId() {
        ToolDispatcher dispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);

        // habit_id arrives as an Integer here, matching how Jackson deserializes
        // a JSON number into a Map<String, Object> — not a Long.
        dispatcher.dispatch("delete_habit", Map.of("habit_id", 7));

        verify(habitService).delete(7L);
    }

    @Test
    void setCheckin_callsHabitCheckinServiceSetTodayWithGivenIdAndDone() {
        HabitCheckin checkin = mock(HabitCheckin.class);
        when(checkin.isDone()).thenReturn(true);
        when(habitCheckinService.setToday(3L, true)).thenReturn(checkin);

        ToolDispatcher dispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);
        dispatcher.dispatch("set_checkin", Map.of("habit_id", 3, "done", true));

        verify(habitCheckinService).setToday(3L, true);
    }
}
