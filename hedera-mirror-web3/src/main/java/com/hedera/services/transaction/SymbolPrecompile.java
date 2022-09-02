package com.hedera.services.transaction;

import com.hedera.mirror.web3.repository.TokenRepository;

import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hederahashgraph.api.proto.java.TransactionBody.Builder;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import org.apache.tuweni.bytes.Bytes;

@AllArgsConstructor
public class SymbolPrecompile implements Precompile {
    protected byte[] address;
    protected TokenRepository tokenRepository;
    protected EncodingFacade encoder;

    @Override
    public Builder body(
            Bytes input, UnaryOperator<byte[]> aliasResolver) {
        return null;
    }

    @Override
    public long getMinimumFeeInTinybars(Timestamp consensusTime) {
        return 0;
    }

    @Override
    public Bytes getSuccessResultFor() {
        final var token = tokenRepository.findByAddress(address);
        return encoder.encodeSymbol(token.get().getSymbol());
    }
}
