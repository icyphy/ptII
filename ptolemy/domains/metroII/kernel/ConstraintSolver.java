package ptolemy.domains.metroII.kernel;

public interface ConstraintSolver {

    public void presentMetroIIEvent(int id);

    public boolean isSatisfied(int id);
}
