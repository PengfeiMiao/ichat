package com.mafiadev.ichat.service;

import com.mafiadev.ichat.dao.SessionRepository;
import com.mafiadev.ichat.model.GptSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionService {
    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();

    private final SessionRepository sessionRepository = new SessionRepository();

    public void loadSessions() {
        List<GptSession> sessions = sessionRepository.findSessions();
        sessions.forEach(session -> sessionHashMap.putIfAbsent(session.getUserName(), session));
    }

    public Map<String, GptSession> getSessions() {
        return sessionHashMap;
    }

    public GptSession getSession(String userName) {
        return sessionHashMap.get(userName);
    }

    public void saveSession(GptSession session) {
        sessionHashMap.putIfAbsent(session.getUserName(), session);
        if(!session.getUserName().startsWith("@")) {
            if (sessionRepository.isExistSessionByUserName(session.getUserName())) {
                sessionRepository.updateSession(session);
            } else {
                sessionRepository.saveSession(session);
            }
        }
    }

    public void updateSession(GptSession session) {
        sessionHashMap.put(session.getUserName(), session);
        if(!session.getUserName().startsWith("@")) {
            sessionRepository.updateSession(session);
        }
    }
}
