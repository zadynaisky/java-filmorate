package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class EventRepository extends BaseRepository<Event> {
    private final EventRowMapper eventRowMapper;

    public EventRepository(JdbcTemplate jdbcTemplate, EventRowMapper eventRowMapper) {
        super(jdbcTemplate);
        this.eventRowMapper = eventRowMapper;
    }

    public Collection<Event> findEventsByUserId(long userId) {
        String sql = "SELECT * FROM \"EVENT\" WHERE user_id = ? ORDER BY timestamp ASC";
        return findMany(sql, eventRowMapper, userId);
    }

    public Event create(Event event) {
        log.debug("Create new event: {}", event);
        String sql = "INSERT INTO \"EVENT\" (`timestamp`, user_id, type, operation, entity_id) VALUES (?, ?, ?, ?, ?);";
        var eventId = insert(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getType().name(),
                event.getOperation().name(),
                event.getEntityId());
        event.setEventId(eventId);
        return event;
    }
}
