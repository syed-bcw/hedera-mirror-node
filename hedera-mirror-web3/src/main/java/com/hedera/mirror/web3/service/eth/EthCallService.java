package com.hedera.mirror.web3.service.eth;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.time.Instant;
import java.util.HashSet;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;

import com.hedera.mirror.web3.evm.CallEvmTxProcessor;
import com.hedera.mirror.web3.evm.CodeCache;
import com.hedera.mirror.web3.evm.SimulatedAliasManager;
import com.hedera.mirror.web3.evm.SimulatedGasCalculator;
import com.hedera.mirror.web3.evm.SimulatedPricesSource;
import com.hedera.mirror.web3.evm.SimulatedWorldState;
import com.hedera.mirror.web3.evm.properties.BlockMetaSourceProvider;
import com.hedera.mirror.web3.evm.properties.EvmProperties;
import com.hedera.mirror.web3.repository.EntityRepository;
import com.hedera.mirror.web3.repository.TokenRepository;

@Named
@RequiredArgsConstructor
public class EthCallService implements ApiContractEthService<EthRpcCallBody, String> {

    static final String ETH_CALL_METHOD = "eth_call";

    private final EntityRepository entityRepository;
    private final EvmProperties evmProperties;
    private final SimulatedGasCalculator simulatedGasCalculator;
    private final SimulatedPricesSource simulatedPricesSource;
    private final BlockMetaSourceProvider blockMetaSourceProvider;
    private final SimulatedWorldState worldState;
    private final CodeCache codeCache;
    private final SimulatedAliasManager simulatedAliasManager;
    private final TokenRepository tokenRepository;

    @Override
    public String getMethod() {
        return ETH_CALL_METHOD;
    }

    @Override
    public String get(final EthRpcCallBody body) {
        //directly pass hex strings to CallEvmTxProcessor
        final var sender = body.getFrom();
        final var senderEvmAddress = Bytes.fromHexString(sender).toArray();
        final var receiverAddress = body.getTo() != null ? Address.wrap(Bytes.fromHexString(body.getTo())) : Address.ZERO;
        final var gasLimit = Integer.decode(body.getGas());
        final var value = body.getValue() != null ? Long.decode(body.getValue()) : Long.decode("0x00");
        final var payload = Bytes.fromHexString(body.getData());

        final var senderEntity = entityRepository.findAccountByAddress(senderEvmAddress).orElse(null);
        final var senderDto = senderEntity != null ? new AccountDto(senderEntity.getNum(), ByteString.copyFrom(senderEntity.getAlias())) : new AccountDto(0L, ByteString.EMPTY);

//        Map<String, PrecompiledContract> precompiledContractMap = new HashMap<>();
//        precompiledContractMap.put(HTS_PRECOMPILED_CONTRACT_ADDRESS, new HTSPrecompiledContract(tokenRepository));

        final CallEvmTxProcessor evmTxProcessor = new CallEvmTxProcessor(simulatedPricesSource, evmProperties,
                simulatedGasCalculator, new HashSet<>(), precompiledContractMap, codeCache, simulatedAliasManager);
//        evmTxProcessor.setWorldState(worldState);
//        evmTxProcessor.setBlockMetaSource(blockMetaSourceProvider);

        final var txnProcessingResult = evmTxProcessor.executeEth(
                senderDto,
                receiverAddress,
                gasLimit,
                value,
                payload,
                Instant.now(),
                BigInteger.valueOf(0L),
                senderDto,
                0L,
                true
        );

        return txnProcessingResult.getOutput().toHexString();
    }
}
