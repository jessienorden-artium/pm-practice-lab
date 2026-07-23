ALTER TABLE habit_checkins DROP FOREIGN KEY fk_habit_checkins_habit;

ALTER TABLE habit_checkins
    ADD CONSTRAINT fk_habit_checkins_habit
    FOREIGN KEY (habit_id) REFERENCES habits (id)
    ON DELETE CASCADE;
