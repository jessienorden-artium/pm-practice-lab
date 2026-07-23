package com.habittracker.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HabitCheckinControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void putDone_forHabitWithNoPriorCheckinToday_createsAndReturnsCheckin() throws Exception {
        long habitId = createHabit("Meditate");

        var response = mockMvc.perform(put("/api/habits/" + habitId + "/checkins/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(body.get("done").asBoolean()).isTrue();
    }

    @Test
    void puttingTodaysCheckinTwice_updatesTheSameRowInsteadOfCreatingASecondOne() throws Exception {
        long habitId = createHabit("Read");

        mockMvc.perform(put("/api/habits/" + habitId + "/checkins/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isOk());

        // If this created a second row instead of updating, the table's
        // unique (habit_id, checkin_date) constraint would throw here.
        var response = mockMvc.perform(put("/api/habits/" + habitId + "/checkins/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":false}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(body.get("done").asBoolean()).isFalse();
    }

    @Test
    void getToday_withNoCheckinYet_returnsDoneFalseNotNotFound() throws Exception {
        long habitId = createHabit("Journal");

        var response = mockMvc.perform(get("/api/habits/" + habitId + "/checkins/today"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(body.get("done").asBoolean()).isFalse();
    }

    @Test
    void putToday_forNonexistentHabitId_returns404() throws Exception {
        mockMvc.perform(put("/api/habits/999999999/checkins/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isNotFound());
    }

    private long createHabit(String name) throws Exception {
        var response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHabitRequest(name))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asLong();
    }
}
