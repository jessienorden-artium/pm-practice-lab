package com.habittracker.coach;

import java.util.List;

public record CoachRequest(String message, List<ChatTurn> history) {

    public List<ChatTurn> historyOrEmpty() {
        return history == null ? List.of() : history;
    }
}
