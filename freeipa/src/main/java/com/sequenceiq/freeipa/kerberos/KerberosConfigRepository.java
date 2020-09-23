package com.sequenceiq.freeipa.kerberos;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional(TxType.REQUIRED)
public interface KerberosConfigRepository extends JpaRepository<KerberosConfig, Long> {

    Optional<KerberosConfig> findByAccountIdAndEnvironmentCrnAndClusterNameIsNullAndArchivedIsFalse(String accountId, String environmentCrn);

    List<KerberosConfig> findByAccountIdAndEnvironmentCrnAndArchivedIsFalse(String accountId, String environmentCrn);

    List<KerberosConfig> findByAccountIdAndEnvironmentCrn(String accountId, String environmentCrn);

    Optional<KerberosConfig> findByAccountIdAndEnvironmentCrnAndClusterNameAndArchivedIsFalse(String accountId, String environmentCrn, String clusterName);
}
