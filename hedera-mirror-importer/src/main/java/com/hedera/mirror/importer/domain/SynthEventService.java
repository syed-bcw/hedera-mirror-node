package com.hedera.mirror.importer.domain;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2023 Hedera Hashgraph, LLC
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

import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hedera.mirror.common.domain.entity.EntityId;

/**
 * This service is used to centralize the conversion logic from record stream
 * items to separate
 * synthetic events for HAPI token transactions
 */
public interface SynthEventService {
    void createSyntheticApproveAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId, long amount,
                                 int logIndex);

    void createSyntheticApproveForAllAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId,
                                       int approved, int logIndex);

    void createSyntheticFungibleTokenMint(RecordItem recordItem, EntityId tokenId, long amount, int logIndex);
    void createSyntheticNonFungibleTokenMint(RecordItem recordItem, EntityId tokenId, long serialNumber, int logIndex);
    void createSyntheticFungibleTokenWipe(RecordItem recordItem, EntityId tokenId, long amount, int logIndex);
    void createSyntheticNonFungibleTokenWipe(RecordItem recordItem, EntityId tokenId, long serialNumber, int logIndex);
    void createSyntheticFungibleTokenBurn(RecordItem recordItem, EntityId tokenId, long amount, int logIndex);
    void createSyntheticNonFungibleTokenBurn(RecordItem recordItem, EntityId tokenId, long serialNumber, int logIndex);
    void createSyntheticTokenTransfer(RecordItem recordItem, EntityId senderId, EntityId receiverId,
                              TokenID tokenId, long amount, int logIndex);
}
