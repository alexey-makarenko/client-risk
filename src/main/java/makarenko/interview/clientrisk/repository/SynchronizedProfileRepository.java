package makarenko.interview.clientrisk.repository;

import makarenko.interview.clientrisk.RiskProfile;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


@Repository
public class SynchronizedProfileRepository implements RiskProfileRepository {
    private final AtomicLong idNext = new AtomicLong(0);
    private final Map<Long, AtomicReference<RiskProfile>> profiles = Collections.synchronizedMap(new HashMap<>());

    @Override
    public long create() {
        final long id = idNext.incrementAndGet();
        profiles.put(id, new AtomicReference<>(RiskProfile.NORMAL));
        return id;
    }

    @Override
    public RiskProfile findRiskProfile(long id) {
        final AtomicReference<RiskProfile> existing = profiles.get(id);
        if (existing == null) {
            return null;
        }
        return existing.get();
    }

    @Override
    public OperationResult update(long id, RiskProfile riskProfile) {
        final AtomicReference<RiskProfile> existing = profiles.get(id);
        if (existing == null) {
            return OperationResult.NotFound;
        }
        existing.set(riskProfile);
        return OperationResult.Success;
    }

    @Override
    public OperationResult delete(long id) {
        return profiles.remove(id) != null ? OperationResult.Success : OperationResult.NotFound;
    }

    @Override
    public void clear() {
        idNext.set(0);
        profiles.clear();
    }
}
