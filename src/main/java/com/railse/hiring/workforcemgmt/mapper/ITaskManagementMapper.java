package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskActivityDto;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.enums.TaskActivity;
import com.railse.hiring.workforcemgmt.model.enums.TaskManagement;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;


import java.util.List;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskManagementMapper {
    ITaskManagementMapper INSTANCE = Mappers.getMapper(ITaskManagementMapper.class);

    TaskActivityDto toDto(TaskActivity model);
    List<TaskActivityDto> toDtoList(List<TaskActivity> models);

    TaskManagementDto modelToDto(TaskManagement model);


    TaskManagement dtoToModel(TaskManagementDto dto);


    List<TaskManagementDto> modelListToDtoList(List<TaskManagement> models);

}
