package ec.com.sofka.account_service.mapper;

import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import ec.com.sofka.account_service.model.Movement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MovementMapper {
    Movement toModel(MovementRequest request);

    MovementResponse toResponse(Movement entity);

    Movement updateModel(MovementRequest request, @MappingTarget Movement entity);

    Movement updateModelV2(MovementPartialUpdateRequest request, @MappingTarget Movement entity);
}