package ec.com.softka.client_service.mapper;

import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import ec.com.softka.client_service.model.Client;
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
public interface ClientMapper {

    Client toModel(ClientRequest request);

    ClientResponse toResponse(Client client);

    void updateModel(ClientRequest request, @MappingTarget Client client);
}