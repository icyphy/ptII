package ptolemy.domains.metroII.kernel;

public interface ConstraintSolver {

    public void presentM2Event(int id);

    public boolean isSatisfied(int id);
}
