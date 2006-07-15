package ptolemy.actor.ptalon;

import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PtalonRelation extends TypedIORelation implements PtalonObject {

    public PtalonRelation() {
        super();
        // TODO Auto-generated constructor stub
    }

    public PtalonRelation(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public PtalonRelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

}
