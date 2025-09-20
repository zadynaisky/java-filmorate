package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Event {
    long eventId;
    long timestamp;
    @JsonProperty("eventType")
    EventType type;
    OperationType operation;
    long userId;
    long entityId;

    public Event(long timestamp, EventType type, OperationType operation, long userId, long entityId) {
        this.timestamp = timestamp;
        this.type = type;
        this.operation = operation;
        this.userId = userId;
        this.entityId = entityId;
    }
}
