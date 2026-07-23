package com.habittracker.habit;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitCheckinRepository extends JpaRepository<HabitCheckin, Long> {

    Optional<HabitCheckin> findByHabitIdAndCheckinDate(Long habitId, LocalDate checkinDate);
}
