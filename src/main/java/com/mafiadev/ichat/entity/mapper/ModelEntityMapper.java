package com.mafiadev.ichat.entity.mapper;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.model.GptSession;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ModelEntityMapper {
    ModelEntityMapper MAPPER = Mappers.getMapper(ModelEntityMapper.class);

    SessionEntity convertSessionModelToEntity(GptSession session);

    GptSession convertSessionEntityToModel(SessionEntity sessionEntity);
}
