package ec.com.sofka.account_service.service;

import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    Page<AccountResponse> index(Pageable pageable);

    AccountResponse show(Long id);

    AccountResponse create(AccountRequest request);

    AccountResponse update(Long id, AccountRequest request);

    AccountResponse partialUpdate(Long id, AccountPartialUpdateRequest patch);

    void delete(Long id);

    Account showById(Long accountId);
}
