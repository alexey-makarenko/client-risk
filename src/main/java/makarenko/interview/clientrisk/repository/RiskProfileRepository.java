package makarenko.interview.clientrisk.repository;

import makarenko.interview.clientrisk.RiskProfile;

public interface RiskProfileRepository {
    long create();

    RiskProfile findRiskProfile(long id);

    OperationResult update(long id, RiskProfile riskProfile);

    OperationResult delete(long id);

    void clear();
}
