package ec.com.sofka.account_service.mapper;

import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.model.Account;
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
public interface AccountMapper {

    Account toModel(AccountRequest request);

    AccountResponse toResponse(Account account);

    void updateModel(AccountRequest request, @MappingTarget Account entity);

    void partialUpdate(Account entity, @MappingTarget AccountPartialUpdateRequest patch);
}