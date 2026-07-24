package com.habittracker.coach;

import java.util.List;

public record CoachResponse(String reply, List<ChatTurn> history) {
}
