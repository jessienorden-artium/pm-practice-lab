package com.habittracker.coach;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import java.util.List;
import java.util.Map;

final class ToolDefinitions {

    private ToolDefinitions() {
    }

    static final Tool GET_HABITS = Tool.builder()
            .name("get_habits")
            .description("List all of the user's habits, each with its id, name, "
                    + "and whether it's been done today.")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder().build())
                    .build())
            .build();

    static final Tool ADD_HABIT = Tool.builder()
            .name("add_habit")
            .description("Add a new habit with the given name.")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder()
                            .putAdditionalProperty(
                                    "name",
                                    JsonValue.from(Map.of(
                                            "type", "string",
                                            "description", "The habit's name")))
                            .build())
                    .required(List.of("name"))
                    .build())
            .build();

    static final Tool DELETE_HABIT = Tool.builder()
            .name("delete_habit")
            .description("Delete a habit by its id.")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder()
                            .putAdditionalProperty(
                                    "habit_id",
                                    JsonValue.from(Map.of(
                                            "type", "integer",
                                            "description", "The habit's id")))
                            .build())
                    .required(List.of("habit_id"))
                    .build())
            .build();

    static final Tool SET_CHECKIN = Tool.builder()
            .name("set_checkin")
            .description("Mark a habit as done or not done for today.")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder()
                            .putAdditionalProperty(
                                    "habit_id",
                                    JsonValue.from(Map.of(
                                            "type", "integer",
                                            "description", "The habit's id")))
                            .putAdditionalProperty(
                                    "done",
                                    JsonValue.from(Map.of(
                                            "type", "boolean",
                                            "description", "Whether the habit is done today")))
                            .build())
                    .required(List.of("habit_id", "done"))
                    .build())
            .build();

    static final List<Tool> ALL = List.of(GET_HABITS, ADD_HABIT, DELETE_HABIT, SET_CHECKIN);
}
