package com.hedera.mirror.web3.evm.account;

import static com.google.protobuf.ByteString.EMPTY;
import static com.google.protobuf.ByteString.copyFrom;
import static com.hedera.mirror.common.domain.entity.EntityType.TOKEN;
import static com.hedera.mirror.common.domain.entity.EntityType.UNKNOWN;
import static com.hedera.mirror.web3.evm.util.EntityUtils.numFromEvmAddress;

import com.google.protobuf.ByteString;

import com.hedera.services.evm.accounts.AccountAccessor;

import lombok.RequiredArgsConstructor;
import org.hyperledger.besu.datatypes.Address;
import org.springframework.stereotype.Component;

import com.hedera.mirror.web3.repository.AccountRepository;

@Component
@RequiredArgsConstructor
public class AccountAccessorImpl implements AccountAccessor {
    private final AccountRepository accountRepository;

    //TODO
    @Override
    public Address exists(Address address) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        return accountRepository.existsById(accountId) ? address : Address.ZERO;
    }

    @Override
    public boolean isTokenTreasury(Address addressOrAlias) {
        final var account = numFromEvmAddress(addressOrAlias.toArrayUnsafe());
        return accountRepository.isTokenTreasury(account).orElse(false);
    }

    @Override
    public boolean hasAnyBalance(Address addressOrAlias) {
        final var account = numFromEvmAddress(addressOrAlias.toArrayUnsafe());
        final var accountBalance = accountRepository.getBalance(account)
                .orElse(0L);

        return accountBalance > 0;
    }

    @Override
    public boolean ownsNfts(Address addressOrAlias) {
        final var account = numFromEvmAddress(addressOrAlias.toArrayUnsafe());
        return accountRepository.ownsNfts(account).orElse(false);
    }

    @Override
    public boolean isTokenAddress(Address address) {
        final var account = numFromEvmAddress(address.toArrayUnsafe());
        final var type = accountRepository.getType(account)
                .orElse(UNKNOWN);

        return type.equals(TOKEN);
    }

    @Override
    public ByteString getAlias(Address address) {
        final var account = numFromEvmAddress(address.toArrayUnsafe());
        final var accountAlias = accountRepository.getAlias(account);

        return accountAlias != null ? copyFrom(accountAlias) : EMPTY;
    }
}
