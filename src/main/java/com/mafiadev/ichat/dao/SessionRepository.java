package com.mafiadev.ichat.dao;

import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.ConditionBuilder;
import com.mafiadev.ichat.dao.helper.SqliteHelper;
import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.entity.mapper.ModelEntityMapper;
import com.mafiadev.ichat.model.GptSession;

import java.util.List;

public class SessionRepository {
    public boolean saveSession(GptSession session) {
        SessionEntity sessionEntity = ModelEntityMapper.MAPPER.convertSessionModelToEntity(session);
        SqliteHelper.insert(sessionEntity);
        return sessionEntity.getId() != null;
    }

    public boolean updateSession(GptSession session) {
        SessionEntity sessionEntity = ModelEntityMapper.MAPPER.convertSessionModelToEntity(session);
        SqliteHelper.updateBy(sessionEntity, ConditionBuilder.of(
                new Condition("USER_NAME", session.getUserName())
        ));
        return sessionEntity.getId() != null;
    }

    public boolean isExistSessionByUserName(String userName) {
        return SqliteHelper.countBy(SessionEntity.class,
                ConditionBuilder.of(
                        new Condition("USER_NAME", userName)
                )) > 0;
    }

    public List<GptSession> findSessions() {
        List<SessionEntity> sessionEntities = SqliteHelper.selectBy(SessionEntity.class,
                ConditionBuilder.of(
                        new Condition("LOGIN", true)
                ));
        return ModelEntityMapper.MAPPER.convertSessionEntitiesToModels(sessionEntities);
    }
}
