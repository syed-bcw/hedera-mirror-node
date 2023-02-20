package com.hedera.mirror.importer.parser.record.transactionhandler;

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

import javax.inject.Named;

import com.hedera.mirror.importer.domain.SynthEventService;

import com.hederahashgraph.api.proto.java.NftRemoveAllowance;
import lombok.RequiredArgsConstructor;

import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.token.Nft;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;

import java.util.List;

@Named
@RequiredArgsConstructor
class CryptoDeleteAllowanceTransactionHandler implements TransactionHandler {

    private final EntityListener entityListener;

    private final SynthEventService synthEventService;

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return null;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.CRYPTODELETEALLOWANCE;
    }

    @Override
    public void updateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!recordItem.isSuccessful()) {
            return;
        }

        List<NftRemoveAllowance> nftAllowances = recordItem.getTransactionBody().getCryptoDeleteAllowance().getNftAllowancesList();
        for (int i = 0; i < recordItem.getTransactionBody().getCryptoDeleteAllowance().getNftAllowancesCount(); i++) {
            NftRemoveAllowance nftRemoveAllowance = nftAllowances.get(i);
            var tokenId = EntityId.of(nftRemoveAllowance.getTokenId());
            var ownerId = EntityId.of(nftRemoveAllowance.getOwner());
            for (var serialNumber : nftRemoveAllowance.getSerialNumbersList()) {
                var nft = new Nft(serialNumber, tokenId);
                nft.setModifiedTimestamp(recordItem.getConsensusTimestamp());
                entityListener.onNft(nft);
            }
            synthEventService.createSyntheticApproveForAllAllowance(recordItem, ownerId.getId(), 0, tokenId, 0, i);
        }
    }
}
