package com.habittracker.coach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.ObjectMappers;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.services.blocking.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.habit.Habit;
import com.habittracker.habit.HabitCheckinService;
import com.habittracker.habit.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Canned Message responses are built by deserializing realistic API JSON via the SDK's own
 * ObjectMappers.jsonMapper() rather than the response builders directly: the builders require
 * every field (including deeply nested, effectively-optional ones like Usage.cacheCreation) to
 * be explicitly touched before build() succeeds, which doesn't reflect how a real API response
 * is shaped. Deserializing from JSON matches what actually crosses the wire.
 */
@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private AnthropicClient anthropicClient;

    @Mock
    private MessageService messageService;

    @Mock
    private HabitService habitService;

    @Mock
    private HabitCheckinService habitCheckinService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void chat_whenModelRequestsAddHabit_dispatchesToHabitServiceAndReturnsFinalText() throws Exception {
        when(anthropicClient.messages()).thenReturn(messageService);

        Message toolUseResponse = message("""
                {
                  "id": "msg_1",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-5",
                  "content": [
                    {"type": "tool_use", "id": "toolu_1", "name": "add_habit", "input": {"name": "Drink Water"}}
                  ],
                  "stop_reason": "tool_use",
                  "stop_sequence": null,
                  "usage": {"input_tokens": 10, "output_tokens": 10}
                }
                """);

        Message finalResponse = message("""
                {
                  "id": "msg_2",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-5",
                  "content": [
                    {"type": "text", "text": "Added Drink Water for you!"}
                  ],
                  "stop_reason": "end_turn",
                  "stop_sequence": null,
                  "usage": {"input_tokens": 10, "output_tokens": 10}
                }
                """);

        when(messageService.create(any(MessageCreateParams.class)))
                .thenReturn(toolUseResponse)
                .thenReturn(finalResponse);

        Habit created = mock(Habit.class);
        when(created.getId()).thenReturn(5L);
        when(created.getName()).thenReturn("Drink Water");
        when(habitService.create("Drink Water")).thenReturn(created);

        ToolDispatcher toolDispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);
        CoachService coachService = new CoachService(anthropicClient, toolDispatcher);

        CoachResponse response = coachService.chat(new CoachRequest("add a habit called Drink Water", null));

        verify(habitService).create("Drink Water");
        assertThat(response.reply()).isEqualTo("Added Drink Water for you!");
    }

    @Test
    void chat_whenAnthropicClientFails_throwsClearErrorInsteadOfSilentlyDoingNothing() {
        when(anthropicClient.messages()).thenReturn(messageService);
        when(messageService.create(any(MessageCreateParams.class)))
                .thenThrow(new RuntimeException("authentication_error: invalid x-api-key"));

        ToolDispatcher toolDispatcher = new ToolDispatcher(habitService, habitCheckinService, objectMapper);
        CoachService coachService = new CoachService(anthropicClient, toolDispatcher);

        assertThatThrownBy(() -> coachService.chat(new CoachRequest("what habits do I have", null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Habit Coach is unavailable");
    }

    private static Message message(String json) throws Exception {
        return ObjectMappers.jsonMapper().readValue(json, Message.class);
    }
}
