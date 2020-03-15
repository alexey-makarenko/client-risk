package makarenko.interview.clientrisk.repository;

import makarenko.interview.clientrisk.RiskProfile;

import java.util.Set;

public interface RiskProfileRepository {
    long create();

    RiskProfile findRiskProfile(long id);

    OperationResult update(long id, RiskProfile riskProfile);

    OperationResult delete(long id);

    OperationResult merge(long id, Set<Long> merging);

    void clear();
}
