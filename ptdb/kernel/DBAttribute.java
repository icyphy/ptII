package ptdb.kernel;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
////DBAttribute

/**
 * An extended StringParameter for searchable database attributes.
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 * 
 */

public class DBAttribute extends StringParameter {

    /** Create a DBAttribute.
     * 
     * @param container
     *          The container of the attribute.
     * @param name
     *          The name of the attribute.
     * @throws IllegalActionException
     *          Thrown for IllegalActionException.
     * @throws NameDuplicationException
     *          Thrown for NameDuplicationExeption.
     */
    public DBAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        
        super(container, name);
        setVisibility(Settable.FULL);
        
    }
    
}
