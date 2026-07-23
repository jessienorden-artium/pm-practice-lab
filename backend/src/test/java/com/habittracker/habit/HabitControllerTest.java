package com.habittracker.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.StreamSupport;
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
class HabitControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createHabit_withValidName_returns201WithCreatedHabit() throws Exception {
        var response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drink water\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(body.get("id").asLong()).isPositive();
        assertThat(body.get("name").asText()).isEqualTo("Drink water");
    }

    @Test
    void createHabit_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listHabits_includesPreviouslyCreatedHabits() throws Exception {
        createHabit("Meditate");

        var response = mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andReturn();

        List<JsonNode> habits = readList(response.getResponse().getContentAsString());
        boolean hasMeditate = habits.stream()
                .anyMatch(h -> h.get("name").asText().equals("Meditate"));
        assertThat(hasMeditate).isTrue();
    }

    @Test
    void deleteHabit_removesItFromSubsequentListing() throws Exception {
        long id = createHabit("Read");

        mockMvc.perform(delete("/api/habits/" + id))
                .andExpect(status().isNoContent());

        var response = mockMvc.perform(get("/api/habits")).andReturn();
        List<JsonNode> habits = readList(response.getResponse().getContentAsString());
        boolean stillPresent = habits.stream().anyMatch(h -> h.get("id").asLong() == id);
        assertThat(stillPresent).isFalse();
    }

    @Test
    void deleteHabit_nonexistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/habits/999999999"))
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

    private List<JsonNode> readList(String json) throws Exception {
        JsonNode array = objectMapper.readTree(json);
        return StreamSupport.stream(array.spliterator(), false).toList();
    }
}
