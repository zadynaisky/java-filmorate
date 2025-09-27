package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.repository.EventRepository;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;

    public Collection<Event> getFeed(Long userId) {
        return eventRepository.findEventsByUserId(userId);
    }

    public Event create(Event event) {
        return eventRepository.create(event);
    }
}
