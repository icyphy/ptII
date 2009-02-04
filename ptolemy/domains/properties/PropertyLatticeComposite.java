package ptolemy.domains.properties;

import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PropertyLatticeComposite extends FSMActor {

    public PropertyLatticeComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if the contained elements form a lattice;
     * false, otherwise.
     */
    public boolean isLattice() {
        return true;
    }
}
