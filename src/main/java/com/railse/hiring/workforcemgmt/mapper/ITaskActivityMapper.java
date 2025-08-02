package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskActivityDto;
import com.railse.hiring.workforcemgmt.model.enums.TaskActivity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskActivityMapper {
    ITaskActivityMapper INSTANCE = Mappers.getMapper(ITaskActivityMapper.class);

    TaskActivityDto toDto(TaskActivity model);
    List<TaskActivityDto> toDtoList(List<TaskActivity> models);
}
