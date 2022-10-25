package com.hedera.mirror.web3.controller;

import static com.hedera.mirror.common.domain.entity.EntityType.TOKEN;
import static com.hedera.mirror.web3.evm.util.EntityUtils.accountIdFromEvmAddress;
import static com.hedera.mirror.web3.evm.util.EntityUtils.numFromEvmAddress;

import com.google.protobuf.ByteString;

import com.hedera.services.evm.store.contracts.HederaEvmEntityAccess;

import com.hederahashgraph.api.proto.java.AccountID;
import lombok.RequiredArgsConstructor;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.datatypes.Address;
import org.springframework.stereotype.Component;

import com.hedera.mirror.web3.repository.EntityAccessRepository;

@Component
@RequiredArgsConstructor
public class MirrorEntityAccess implements HederaEvmEntityAccess {
    private final EntityAccessRepository entityRepository;

    @Override
    public long getBalance(Address address) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        final var balance = entityRepository.getBalance(accountId);
        return balance.orElse(0L);
    }

    public boolean isDeleted(Address address) {
        final var accountID = accountIdFromEvmAddress(address);
        final var realm = accountID.getRealmNum();
        final var shard = accountID.getShardNum();
        final var accountNum = accountID.getAccountNum();

        return entityRepository.isDeleted(accountNum).orElse(true);
    }

    @Override
    public boolean isExtant(Address address) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        return entityRepository.existsById(accountId);
    }

    @Override
    public boolean isTokenAccount(Address address) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        final var type = entityRepository.getType(accountId);
        return type.isPresent() && type.get().equals(TOKEN);
    }

    @Override
    public ByteString alias(Address address) {
        //TODO implement repo logic here
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        return null;
    }

    @Override
    public UInt256 getStorage(Address address, UInt256 key) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        final var storage = entityRepository.getStorage(accountId, key.toArrayUnsafe());
        return UInt256.fromBytes(Bytes.wrap(storage));
    }

    @Override
    public Bytes fetchCodeIfPresent(Address address) {
        final var accountId = numFromEvmAddress(address.toArrayUnsafe());
        final var runtimeCode = entityRepository.fetchContractCode(accountId);
        return runtimeCode.map(Bytes::wrap).orElse(Bytes.EMPTY);
    }
}
