package com.virtual.app.sicbo.module.services;

import com.virtual.app.sicbo.module.data.Session;

import java.util.List;
import java.util.Optional;

public interface SessionService {
    List<Session> getAllSessions();
    Optional<Session> getSessionById(Integer id);
    Session createOrUpdateSession(Session session);
    void deleteSession(Integer id);
}
