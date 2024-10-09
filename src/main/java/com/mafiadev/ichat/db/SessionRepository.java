package com.mafiadev.ichat.db;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.entity.mapper.ModelEntityMapper;
import com.mafiadev.ichat.model.GptSession;

import java.util.List;

public class SessionRepository {
    public SessionEntity saveSession(GptSession session) {
        SessionEntity sessionEntity = ModelEntityMapper.MAPPER.convertSessionModelToEntity(session);
        SqliteHelper.insert(sessionEntity);
        return sessionEntity;
    }

    public List<SessionEntity> findSessions() {
        return SqliteHelper.select(SessionEntity.class);
    }
}
