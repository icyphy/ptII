package ptolemy.data.properties.lattice;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class ConstraintAttribute extends StringAttribute {

    /** Construct a PropertyAttribute with the specified name, and container.
     * @param container Container
     * @param name Name
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public ConstraintAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    public Visibility getVisibility() {
        return _visibility;
    }

    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

//    private Visibility _visibility = Settable.NONE;
    private Visibility _visibility = Settable.FULL;
//    private Visibility _visibility = Settable.NOT_EDITABLE;   
}
