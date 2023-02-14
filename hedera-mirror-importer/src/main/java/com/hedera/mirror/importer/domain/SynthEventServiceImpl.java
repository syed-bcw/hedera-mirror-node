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

    private final static String transferSignature = "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    private final static String approvalSignature = "8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925";
    private final static String approveForAllSignature = "17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31";

    private final byte[] bloom = { 0 };

    @Override
    public void processApproveAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId,
                                        long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        byte[] data = Utility.hexToByte(Long.toHexString(amount));
        byte[] topic0 = Utility.hexToByte(approvalSignature);
        byte[] topic1 = Utility.hexToByte(Long.toHexString(ownerId));
        byte[] topic2 = Utility.hexToByte(Long.toHexString(spenderId));
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();

        processLog(consensusTimestamp, logIndex, tokenId, payerAccountId, data, topic0, topic1, topic2);
    }

    @Override
    public void processApproveForAllAllowance(RecordItem recordItem, long ownerId, long spenderId, EntityId tokenId,
                                              int approved, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        byte[] data = Utility.hexToByte(Integer.toHexString(approved));
        byte[] topic0 = Utility.hexToByte(approveForAllSignature);
        byte[] topic1 = Utility.hexToByte(Long.toHexString(ownerId));
        byte[] topic2 = Utility.hexToByte(Long.toHexString(spenderId));
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();

        processLog(consensusTimestamp, logIndex, tokenId, payerAccountId, data, topic0, topic1, topic2);
    }

    @Override
    public void processTokenMint(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }

        String amountHex = Long.toHexString(amount);
        String accountId = recordItem.getPayerAccountId().getId().toString();

        byte[] data = Utility.hexToByte(amountHex);
        byte[] topic0 = Utility.hexToByte(transferSignature);
        byte[] topic1 = Utility.hexToByte("0");
        byte[] topic2 = Utility.hexToByte(accountId);
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();

        processLog(consensusTimestamp, logIndex, tokenId, payerAccountId, data, topic0, topic1, topic2);
    }

    @Override
    public void processTokenWipe(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }
        TokenWipeAccountTransactionBody tokenWipeAccountTransactionBody = recordItem.getTransactionBody()
                .getTokenWipe();
        long accountNum = tokenWipeAccountTransactionBody.getAccount().getAccountNum();

        String accountHex = Long.toHexString(accountNum);
        String amountHex = Long.toHexString(amount);

        byte[] data = Utility.hexToByte(amountHex);
        byte[] topic0 = Utility.hexToByte(transferSignature);
        byte[] topic1 = Utility.hexToByte(accountHex);
        byte[] topic2 = Utility.hexToByte("0");
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();

        processLog(consensusTimestamp, logIndex, tokenId, payerAccountId, data, topic0, topic1, topic2);
    }

    @Override
    public void processTokenBurn(RecordItem recordItem, EntityId tokenId, long amount, int logIndex) {
        if (isContract(recordItem)) {
            return;
        }

        String amountHex = Long.toHexString(amount);
        String accountId = recordItem.getPayerAccountId().getId().toString();

        byte[] data = Utility.hexToByte(amountHex);
        byte[] topic0 = Utility.hexToByte(transferSignature);
        byte[] topic1 = Utility.hexToByte(accountId);
        byte[] topic2 = Utility.hexToByte("0");
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        EntityId payerAccountId = recordItem.getPayerAccountId();

        processLog(consensusTimestamp, logIndex, tokenId, payerAccountId, data, topic0, topic1, topic2);
    }

    @Override
    public void processTokenTransfer(RecordItem recordItem, EntityId payerAccountId, EntityId senderId,
                                     EntityId receiverId, TokenID tokenId, long amount, int logIndex) {
        TransactionBody body = recordItem.getTransactionBody();
        boolean isMintWipeBurn = body.hasTokenMint() || body.hasTokenWipe() || body.hasTokenBurn();
        if (senderId.getId() == 0 || isMintWipeBurn || isContract(recordItem)) {
            return;
        }

        String amountHex = Long.toHexString(amount);
        String senderIdHex = Long.toHexString(senderId.getId());
        String receiverIdHex = Long.toHexString(receiverId.getId());

        byte[] data = Utility.hexToByte(amountHex);
        byte[] topic0 = Utility.hexToByte(transferSignature);
        byte[] topic1 = Utility.hexToByte(senderIdHex);
        byte[] topic2 = Utility.hexToByte(receiverIdHex);

        EntityId token = EntityId.of(tokenId);
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        processLog(consensusTimestamp, logIndex, token, payerAccountId, data, topic0, topic1, topic2);
    }

    private void processLog(long consensusTimestamp, int index, EntityId tokenId, EntityId payerAccountId, byte[] data,
                            byte[] topic0,
                            byte[] topic1, byte[] topic2) {
        ContractLog contractLog = new ContractLog();

        contractLog.setBloom(bloom);
        contractLog.setConsensusTimestamp(consensusTimestamp);
        contractLog.setContractId(tokenId);
        contractLog.setData(data);
        contractLog.setIndex(index);
        contractLog.setRootContractId(tokenId);
        contractLog.setPayerAccountId(payerAccountId);
        contractLog.setTopic0(topic0);
        contractLog.setTopic1(topic1);
        contractLog.setTopic2(topic2);

        entityListener.onContractLog(contractLog);
    }

    private boolean isContract(RecordItem recordItem) {
        return recordItem.getTransactionRecord().hasContractCallResult() || recordItem.getTransactionRecord().hasContractCreateResult();
    }
}
