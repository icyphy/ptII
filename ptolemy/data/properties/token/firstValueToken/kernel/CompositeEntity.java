/* A property constraint helper for composite actor.

 Copyright (c) 2005-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.data.properties.token.firstValueToken.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenCompositeHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CompositeActor

/**
 A property constraint helper for composite actor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class CompositeEntity extends PropertyTokenCompositeHelper {

    /** 
     * Construct a property constraint helper for the given
     * CompositeActor. This is the helper class for any 
     * CompositeActor that does not have a specific defined
     * helper class.
     * @param actor The given CompositeActor.
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public CompositeEntity(PropertyTokenSolver solver, 
            ptolemy.kernel.CompositeEntity entity)
        throws IllegalActionException {

        super(solver, entity);
    }

    public void determineProperty() throws IllegalActionException, NameDuplicationException {
        super.determineProperty();
        
        Iterator ports = getPropertyables(IOPort.class).iterator();
        while (ports.hasNext()) {

            IOPort port = (IOPort) ports.next();
                        
            // get port value from firstValueToken
            PropertyToken pt = null;
            pt = (PropertyToken) getSolver().getProperty(port);

            if (pt == null) {
                setEquals(port, new PropertyToken(Token.NIL));
            } 
        }
    }

    protected List<PropertyHelper> _getASTNodeHelpers() {
        List<PropertyHelper> astHelpers = new ArrayList<PropertyHelper>();
        return astHelpers;
    }
}
