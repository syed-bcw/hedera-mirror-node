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

import javax.inject.Named;
import lombok.RequiredArgsConstructor;

import com.hedera.mirror.common.domain.contract.ContractLog;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hederahashgraph.api.proto.java.TokenWipeAccountTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hedera.mirror.importer.util.Utility;

@Named
@RequiredArgsConstructor
public class SynthEventServiceImpl implements SynthEventService {

    private final EntityListener entityListener;

    private static final String TRANSFER_SIGNATURE = "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    private static final String APPROVAL_SIGNATURE = "8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925";
    private static final String APPROVE_FOR_ALL_SIGNATURE = "17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31";

    @Override
    public void createSyntheticApproveAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId,
                                        long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        String data = Long.toHexString(amount);
        String topic1 = Long.toHexString(ownerId);
        String topic2 = Long.toHexString(spenderId);

        processLog(recordItem, logIndex, tokenId, data, APPROVAL_SIGNATURE, topic1, topic2);
    }

    @Override
    public void createSyntheticApproveForAllAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId,
                                              int approved, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        String data = Integer.toHexString(approved);
        String topic1 = Long.toHexString(ownerId);
        String topic2 = Long.toHexString(spenderId);

        processLog(recordItem, logIndex, tokenId, data, APPROVE_FOR_ALL_SIGNATURE, topic1, topic2);
    }

    @Override
    public void createSyntheticTokenTransfer(RecordItem recordItem, EntityId senderId, EntityId receiverId,
                                             TokenID tokenId, long amount, int logIndex) {
        TransactionBody body = recordItem.getTransactionBody();
        boolean isMintWipeBurn = body.hasTokenMint() || body.hasTokenWipe() || body.hasTokenBurn();
        if (senderId.getId() == 0 || isMintWipeBurn || isContract(recordItem)) {
            return;
        }

        String amountHex = Long.toHexString(amount);
        String senderIdHex = Long.toHexString(senderId.getId());
        String receiverIdHex = Long.toHexString(receiverId.getId());

        EntityId token = EntityId.of(tokenId);
        processLog(recordItem, logIndex, token, amountHex, TRANSFER_SIGNATURE, senderIdHex, receiverIdHex);
    }

    @Override
    public void createSyntheticFungibleTokenMint(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenMint(recordItem, tokenId, amount, logIndex);
    }

    @Override
    public void createSyntheticNonFungibleTokenMint(RecordItem recordItem, EntityId tokenId, long serialNumber,
                                                    int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenMint(recordItem, tokenId, serialNumber, logIndex);
    }

    @Override
    public void createSyntheticFungibleTokenWipe(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenWipe(recordItem, tokenId, amount, logIndex);
    }

    @Override
    public void createSyntheticNonFungibleTokenWipe(RecordItem recordItem, EntityId tokenId, long serialNumber,
                                                    int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenWipe(recordItem, tokenId, serialNumber, logIndex);
    }

    @Override
    public void createSyntheticFungibleTokenBurn(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenBurn(recordItem, tokenId, amount, logIndex);
    }

    @Override
    public void createSyntheticNonFungibleTokenBurn(RecordItem recordItem, EntityId tokenId, long serialNumber, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        createSyntheticTokenBurn(recordItem, tokenId, serialNumber, logIndex);
    }

    private void createSyntheticTokenMint(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        String amountHex = Long.toHexString(amount);
        String accountId = recordItem.getPayerAccountId().getId().toString();

        processLog(recordItem, logIndex, tokenId, amountHex, TRANSFER_SIGNATURE, "0", accountId);
    }

    private void createSyntheticTokenWipe(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        TokenWipeAccountTransactionBody tokenWipeAccountTransactionBody = recordItem.getTransactionBody()
                .getTokenWipe();
        long accountNum = tokenWipeAccountTransactionBody.getAccount().getAccountNum();

        String accountHex = Long.toHexString(accountNum);
        String amountHex = Long.toHexString(amount);

        processLog(recordItem, logIndex, tokenId, amountHex, TRANSFER_SIGNATURE, accountHex, "0");
    }

    private void createSyntheticTokenBurn(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        String amountHex = Long.toHexString(amount);
        String accountId = recordItem.getPayerAccountId().getId().toString();

        processLog(recordItem, logIndex, tokenId, amountHex, TRANSFER_SIGNATURE, accountId, "0");
    }

    private void processLog(RecordItem recordItem, int index, EntityId tokenId, String data, String topic0,
                            String topic1, String topic2) {
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();
        byte[] bloom = { 0 };

        ContractLog contractLog = new ContractLog();

        contractLog.setBloom(bloom);
        contractLog.setConsensusTimestamp(consensusTimestamp);
        contractLog.setContractId(tokenId);
        contractLog.setData(Utility.hexToByte(data));
        contractLog.setIndex(index);
        contractLog.setRootContractId(tokenId);
        contractLog.setPayerAccountId(payerAccountId);
        contractLog.setTopic0(Utility.hexToByte(topic0));
        contractLog.setTopic1(Utility.hexToByte(topic1));
        contractLog.setTopic2(Utility.hexToByte(topic2));

        entityListener.onContractLog(contractLog);
    }

    private boolean isContract(RecordItem recordItem) {
        return recordItem.getTransactionRecord().hasContractCallResult() || recordItem.getTransactionRecord().hasContractCreateResult();
    }
}
