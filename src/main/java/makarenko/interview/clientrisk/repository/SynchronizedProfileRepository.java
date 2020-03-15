package makarenko.interview.clientrisk.repository;

import makarenko.interview.clientrisk.RiskProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Repository
public class SynchronizedProfileRepository implements RiskProfileRepository {
    private final AtomicLong idNext = new AtomicLong(0);
    private final Map<Long, RiskProfile> profiles = new ConcurrentHashMap<>();

    @Override
    public OperationResult merge(long id, Set<Long> merging) {
        final RiskProfile existing = profiles.get(id);
        if (existing == null) {
            return OperationResult.NotFound;
        }
        merging.remove(id);
        for (Long merge : merging) {
            final RiskProfile profile = profiles.get(merge);
            if (profile == null) {
                return OperationResult.NotFound;
            }
        }
        if (merging.isEmpty()) {
            return OperationResult.NotFound;
        }

        final ArrayList<Long> locking = new ArrayList<>(merging);
//        locking.sort(Comparator.comparingLong(x -> x));
//        TODO: Will acquire only needed locks in sorted order to avoid deadlock

        synchronized (profiles) {
            RiskProfile max = RiskProfile.LOW;
            for (Long profile : locking) {
                final RiskProfile removed = profiles.remove(profile);
                if (profile == null) {
                    return OperationResult.NotFound;
                }
                if (max.compareTo(removed) < 0) {
                    max = removed;
                }
            }
            profiles.put(id, max);
            return OperationResult.Success;
        }
    }

    @Override
    public long create() {
        final long id = idNext.incrementAndGet();
        profiles.put(id, RiskProfile.NORMAL);
        return id;
    }

    @Override
    public RiskProfile findRiskProfile(long id) {
        return profiles.get(id);
    }

    @Override
    public OperationResult update(long id, RiskProfile riskProfile) {
        final RiskProfile existing = profiles.computeIfPresent(id, (k, exist) -> riskProfile);
        if (existing == null) {
            return OperationResult.NotFound;
        }
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
