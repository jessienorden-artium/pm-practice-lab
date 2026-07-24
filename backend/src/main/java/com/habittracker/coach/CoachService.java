package com.habittracker.coach;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CoachService {

    private static final String MODEL = "claude-sonnet-5";
    private static final String SYSTEM_PROMPT = "You are a helpful habit-tracking coach. "
            + "Use the provided tools to actually view, add, delete, and check off the user's "
            + "habits on their behalf -- don't just describe what you would do.";

    private final AnthropicClient anthropicClient;
    private final ToolDispatcher toolDispatcher;

    public CoachService(AnthropicClient anthropicClient, ToolDispatcher toolDispatcher) {
        this.anthropicClient = anthropicClient;
        this.toolDispatcher = toolDispatcher;
    }

    public CoachResponse chat(CoachRequest request) {
        List<MessageParam> messages = new ArrayList<>();
        for (ChatTurn turn : request.historyOrEmpty()) {
            messages.add(MessageParam.builder()
                    .role("assistant".equals(turn.role()) ? MessageParam.Role.ASSISTANT : MessageParam.Role.USER)
                    .content(turn.content())
                    .build());
        }
        messages.add(MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(request.message())
                .build());

        Message response = createMessage(messages);

        while (response.stopReason().isPresent() && response.stopReason().get().equals(StopReason.TOOL_USE)) {
            messages.add(response.toParam());

            List<ContentBlockParam> toolResults = new ArrayList<>();
            for (ContentBlock block : response.content()) {
                block.toolUse().ifPresent(toolUse -> toolResults.add(toToolResultParam(toolUse)));
            }
            messages.add(MessageParam.builder()
                    .role(MessageParam.Role.USER)
                    .contentOfBlockParams(toolResults)
                    .build());

            response = createMessage(messages);
        }

        String replyText = extractText(response);

        List<ChatTurn> updatedHistory = new ArrayList<>(request.historyOrEmpty());
        updatedHistory.add(new ChatTurn("user", request.message()));
        updatedHistory.add(new ChatTurn("assistant", replyText));

        return new CoachResponse(replyText, updatedHistory);
    }

    private Message createMessage(List<MessageParam> messages) {
        MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(1024)
                .system(SYSTEM_PROMPT)
                .messages(messages);
        ToolDefinitions.ALL.forEach(paramsBuilder::addTool);
        MessageCreateParams params = paramsBuilder.build();
        try {
            return anthropicClient.messages().create(params);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Habit Coach is unavailable: " + e.getMessage(), e);
        }
    }

    private ContentBlockParam toToolResultParam(ToolUseBlock toolUse) {
        Map<String, Object> input = toolUse._input().convert(Map.class);
        String result = toolDispatcher.dispatch(toolUse.name(), input);
        return ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                .toolUseId(toolUse.id())
                .content(result)
                .build());
    }

    private String extractText(Message message) {
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    }
}
