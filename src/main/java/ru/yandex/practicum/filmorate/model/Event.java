package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Event {
    private long eventId;
    private long timestamp;
    @JsonProperty("eventType")
    private EventType type;
    private OperationType operation;
    private long userId;
    private long entityId;

    public Event(long timestamp, EventType type, OperationType operation, long userId, long entityId) {
        this.timestamp = timestamp;
        this.type = type;
        this.operation = operation;
        this.userId = userId;
        this.entityId = entityId;
    }
}
