package makarenko.interview.clientrisk.repository;

public enum OperationResult {
    Success, NotFound;

    public boolean isSuccess() {
        return this == Success;
    }
}
