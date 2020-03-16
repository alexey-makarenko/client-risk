package makarenko.interview.clientrisk;

import makarenko.interview.clientrisk.repository.ConcurrentMapProfileRepository;
import makarenko.interview.clientrisk.repository.RiskProfileRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientRiskRepositoryTest {
    private static Stream<RiskProfileRepository> generator() {
        return Stream.of(new ConcurrentMapProfileRepository());
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void successTotalOfTwo(RiskProfileRepository riskProfileRepository) {
        final long client1 = riskProfileRepository.create();
        riskProfileRepository.update(client1, RiskProfile.HIGH);
        assertEquals(RiskProfile.HIGH, riskProfileRepository.findRiskProfile(client1));
    }

}

