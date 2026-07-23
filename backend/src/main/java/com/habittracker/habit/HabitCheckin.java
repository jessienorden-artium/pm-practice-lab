package com.habittracker.habit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "habit_checkins")
public class HabitCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "habit_id", nullable = false, updatable = false)
    private Long habitId;

    @Column(name = "checkin_date", nullable = false, updatable = false)
    private LocalDate checkinDate;

    @Column(nullable = false)
    private boolean done;

    protected HabitCheckin() {
        // for JPA
    }

    public HabitCheckin(Long habitId, LocalDate checkinDate, boolean done) {
        this.habitId = habitId;
        this.checkinDate = checkinDate;
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
