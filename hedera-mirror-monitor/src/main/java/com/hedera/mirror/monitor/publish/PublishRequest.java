package com.hedera.mirror.monitor.publish;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
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

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

import com.hedera.datagenerator.sdk.supplier.TransactionType;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionId;

@Builder
@Value
public class PublishRequest {
    private final boolean logResponse;
    private final boolean receipt;
    private final boolean record;
    private final Instant timestamp = Instant.now();
    private final TransactionBuilder<TransactionId, ?, ?> transactionBuilder;
    private final TransactionType type;
}
