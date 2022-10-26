package com.hedera.mirror.web3.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends EntityAccessRepository {
    @Query(value = "select treasury_account_id > 0 from token where treasury_account_id = ?1", nativeQuery = true)
    Optional<Boolean> isTokenTreasury(final Long accountNum);

    @Query(value = "select account_id > 0 from nft where account_id = ?1"
            , nativeQuery = true)
    Optional<Boolean> ownsNfts(final Long accountNum);

    @Query(value = "select alias from entity where num = ?1 and deleted <> true",
            nativeQuery = true)
    byte[] getAlias(final Long accountNum);
}
