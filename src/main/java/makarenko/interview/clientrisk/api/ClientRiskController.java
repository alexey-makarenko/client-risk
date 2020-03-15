package makarenko.interview.clientrisk.api;

import makarenko.interview.clientrisk.RiskProfile;
import makarenko.interview.clientrisk.repository.OperationResult;
import makarenko.interview.clientrisk.repository.RiskProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/risk/profiles")
public class ClientRiskController {

    private final RiskProfileRepository riskRepository;

    public ClientRiskController(RiskProfileRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @PostMapping("/create")
    public Object createClient() {
        final long client = riskRepository.create();
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping("/{id}/value")
    public Object getRiskProfile(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        final RiskProfile value = riskRepository.findRiskProfile(id);
        if (value == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(value);
        }
    }

    @PostMapping("/{id}/update")
    public Object update(@PathVariable Long id,
                         @RequestBody RiskProfile riskProfile) {
        if (id == null || riskProfile == null) {
            return ResponseEntity.badRequest().build();
        }
        final OperationResult result = riskRepository.update(id, riskProfile);
        return response(result);
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        final OperationResult result = riskRepository.delete(id);
        return response(result);
    }

    private Object response(OperationResult result) {
        if (result.isSuccess()) {
            return ResponseEntity.ok().build();
        }
        if (result == OperationResult.NotFound) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
