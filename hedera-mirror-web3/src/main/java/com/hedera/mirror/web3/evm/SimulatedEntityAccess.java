package com.hedera.mirror.web3.evm;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2022 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.datatypes.Address;

import com.hedera.mirror.web3.repository.ContractStateChangeRepository;
import com.hedera.mirror.web3.repository.EntityRepository;
import com.hedera.mirror.web3.repository.TokenRepository;
import com.hedera.services.transaction.store.contracts.EntityAccess;

//FUTURE WORK to be implemented in separate PR
@Named
@RequiredArgsConstructor
public class SimulatedEntityAccess implements EntityAccess {

    private final ContractStateChangeRepository contractStateChangeRepository;
    private final TokenRepository tokenRepository;
    private final EntityRepository entityRepository;

    @Override
    public long getBalance(Address address) {
        return entityRepository.findAccountBalanceByAddress(address.toArrayUnsafe()).orElse(0L);
    }

    @Override
    public boolean isDeleted(Address id) {
        return false;
    }

    @Override
    public boolean isDetached(Address id) {
        return false;
    }

    @Override
    public boolean isExtant(Address id) {
        return true;
    }

    @Override
    public boolean isTokenAccount(Address address) {
        return tokenRepository.findByAddress(address.toArrayUnsafe()).isPresent();
    }

    @Override
    public void putStorage(Address id, UInt256 key, UInt256 value) {

    }

    @Override
    public UInt256 getStorage(Address id, UInt256 key) {
        var value = contractStateChangeRepository.findStorageValue(id.toArray(), key.toArray()).orElse(null);
        return UInt256.fromHexString(value);
    }

    @Override
    public void flushStorage() {

    }

    @Override
    public void storeCode(Address id, Bytes code) {

    }

    @Override
    public Bytes fetchCodeIfPresent(Address id) {
        return null;
    }
}
