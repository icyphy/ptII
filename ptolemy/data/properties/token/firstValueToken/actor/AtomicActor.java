/* A helper class for ptolemy.actor.AtomicActor.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.token.firstValueToken.actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AtomicActor

/**
 A helper class for ptolemy.actor.AtomicActor.

 @author Thomas Mandl, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class AtomicActor extends PropertyTokenHelper {

    /**
     * Construct a helper for the given AtomicActor. This is the
     * helper class for any ActomicActor that does not have a
     * specific defined helper class. Default actor constraints
     * are set for this helper. 
     * @param actor The given ActomicActor.
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public AtomicActor(PropertyTokenSolver solver, 
            ptolemy.actor.AtomicActor actor)
            throws IllegalActionException {
        super(solver, actor);
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
