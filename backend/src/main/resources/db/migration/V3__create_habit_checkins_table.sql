CREATE TABLE habit_checkins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    habit_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    done BOOLEAN NOT NULL,
    CONSTRAINT fk_habit_checkins_habit FOREIGN KEY (habit_id) REFERENCES habits (id),
    CONSTRAINT uq_habit_checkins_habit_date UNIQUE (habit_id, checkin_date)
);
