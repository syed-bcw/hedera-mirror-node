package com.hedera.mirror.test.e2e.acceptance.steps;

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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.CustomFee;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;
import com.hedera.mirror.test.e2e.acceptance.client.ContractClient;

import com.hedera.mirror.test.e2e.acceptance.client.FileClient;

import com.hedera.mirror.test.e2e.acceptance.client.MirrorNodeClient;

import com.hedera.mirror.test.e2e.acceptance.client.TokenClient;
import com.hedera.mirror.test.e2e.acceptance.props.CompiledSolidityArtifact;

import com.hedera.mirror.test.e2e.acceptance.props.ExpandedAccountId;

import io.cucumber.java.en.Given;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

@Log4j2
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ERCContractFeature extends AbstractFeature {

    private static final int INITIAL_SUPPLY = 1_000_000;
    private static final int MAX_SUPPLY = 1;

    private final List<TokenId> tokenIds = new ArrayList<>();
    private final Map<TokenId, List<CustomFee>> tokenCustomFees = new HashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public static final String NAME_SELECTOR = "01984892";
    public static final String SYMBOL_SELECTOR = "a86e3576";
    public static final String DECIMALS_SELECTOR = "d449a832";
    public static final String TOTAL_SUPPLY_SELECTOR = "e4dc2aa4";
    public static final String BALANCE_OF_SELECTOR = "f7888aec";
    public static final String ALLOWANCE_SELECTOR = "927da105";
    public static final String GET_APPROVED_SELECTOR = "098f2366";
    public static final String IS_APPROVED_FOR_ALL_SELECTOR = "f49f40db";
    public static final String GET_OWNER_OF_SELECTOR = "d5d03e21";
    public static final String TOKEN_URI_SELECTOR = "e9dc6375";
    private static final String WRONG_SELECTOR = "000000";

    private final ContractClient contractClient;
    private final FileClient fileClient;
    private final MirrorNodeClient mirrorClient;
    private final TokenClient tokenClient;

    @Value("classpath:solidity/artifacts/contracts/ERCTestContract.sol/ERCTestContract.json")
    private Path ercContract;

    private ContractId contractId;
    private FileId fileId;
    private CompiledSolidityArtifact compiledSolidityArtifact;

    @Given("I successfully create an erc contract from contract bytes with {int} balance")
    public void createNewContract(int initialBalance) throws IOException {
        compiledSolidityArtifact = MAPPER.readValue(
                ResourceUtils.getFile(ercContract.toUri()),
                CompiledSolidityArtifact.class);
        createContract(compiledSolidityArtifact.getBytecode(), initialBalance);
    }

    @Given("I  create a new token")
    public void createNewToken() {
        createNewToken(
                RandomStringUtils.randomAlphabetic(4).toUpperCase(),
                TokenFreezeStatus.FreezeNotApplicable_VALUE,
                TokenKycStatus.KycNotApplicable_VALUE
        );
    }

    private void createContract(String byteCode, int initialBalance) {
        persistContractBytes(byteCode.replaceFirst("0x", ""));
        networkTransactionResponse = contractClient.createContract(
                fileId,
                contractClient.getSdkClient().getAcceptanceTestProperties().getFeatureProperties()
                        .getMaxContractFunctionGas(),
                initialBalance == 0 ? null : Hbar.fromTinybars(initialBalance),
                null);

        verifyCreateContractNetworkResponse();
    }

    private void persistContractBytes(String contractContents) {
        // rely on SDK chunking feature to upload larger files
        networkTransactionResponse = fileClient.createFile(new byte[] {});
        assertNotNull(networkTransactionResponse.getTransactionId());
        assertNotNull(networkTransactionResponse.getReceipt());
        fileId = networkTransactionResponse.getReceipt().fileId;
        assertNotNull(fileId);
        log.info("Created file {} to hold contract init code", fileId);

        networkTransactionResponse = fileClient.appendFile(fileId, contractContents.getBytes(StandardCharsets.UTF_8));
        assertNotNull(networkTransactionResponse.getTransactionId());
        assertNotNull(networkTransactionResponse.getReceipt());
    }

    private void verifyCreateContractNetworkResponse() {
        assertNotNull(networkTransactionResponse.getTransactionId());
        assertNotNull(networkTransactionResponse.getReceipt());
        contractId = networkTransactionResponse.getReceipt().contractId;
        assertNotNull(contractId);
    }

    private void createNewToken(String symbol, int freezeStatus, int kycStatus) {
        createNewToken(symbol, freezeStatus, kycStatus, TokenType.FUNGIBLE_COMMON, TokenSupplyType.INFINITE, Collections
                .emptyList());
    }

    private TokenId createNewToken(String symbol, int freezeStatus, int kycStatus, TokenType tokenType,
            TokenSupplyType tokenSupplyType, List<CustomFee> customFees) {
        ExpandedAccountId admin = tokenClient.getSdkClient().getExpandedOperatorAccountId();
        networkTransactionResponse = tokenClient.createToken(
                admin,
                symbol,
                freezeStatus,
                kycStatus,
                admin,
                INITIAL_SUPPLY,
                tokenSupplyType,
                MAX_SUPPLY,
                tokenType,
                customFees);
        assertNotNull(networkTransactionResponse.getTransactionId());
        assertNotNull(networkTransactionResponse.getReceipt());
        TokenId tokenId = networkTransactionResponse.getReceipt().tokenId;
        assertNotNull(tokenId);
        tokenIds.add(tokenId);
        tokenCustomFees.put(tokenId, customFees);

        return tokenId;
    }

}
