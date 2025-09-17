package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.repository.MpaRepository;

import java.util.Collection;

@Service
public class MpaService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MpaService.class);
    private final MpaRepository mpaRepository;
    
    public MpaService(MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public Collection<Mpa> findAll() {
        log.info("Find all mpa");
        return mpaRepository.findAll();
    }

    public Mpa findById(long mpaId) {
        log.info("Find mpa by id {}", mpaId);
        var mpa = mpaRepository.findById(mpaId);
        if (mpa == null)
            throw new NotFoundException("Mpa with id " + mpaId + " not found");
        return mpa;
    }

    public boolean exists(long mpaId) {
        return findById(mpaId) != null;
    }
}
