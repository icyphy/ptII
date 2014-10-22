/* Low-level input port for JOP.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jopio;

import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JopReadPort

/**
 <p>
 Read low-level I/O port on the Java processor JOP (see http://www.jopdesign.com).
 <p>
 In the simulation just dummy values are returned (the simulated us counter).

 @author Martin Schoeberl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mschoebe)
 @Pt.AcceptedRating Red (mschoebe)
 */
public class JopReadPort extends Source {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public JopReadPort(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.INT);
        output.setMultiport(false);

        portAddress = new Parameter(this, "portAddress");
        // use the us counter as default
        portAddress.setExpression("-127");
        portAddress.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The address of the I/O port. Has to be in an allowed range.
     */
    public Parameter portAddress;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The <i>portAddress</i> has to be in a valid range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == portAddress) {
            int addr = ((IntToken) portAddress.getToken()).intValue();

            if (addr > 0) {
                throw new IllegalActionException(this, "Illegal port address");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set type constraints on the ports.
     *  @param workspace The workspace into which to clone.
     *  @return A new instance of AddSubtract.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JopReadPort newObject = (JopReadPort) super.clone(workspace);
        newObject.output.setTypeEquals(BaseType.INT);
        return newObject;
    }

    /** Read the input port on the first invocation of fire() in the
     * current iteration and send the value on each fire().
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_firstFire) {
            // read the value - simulate the us counter on JOP
            // at address -127
            int v = (int) System.currentTimeMillis() * 1000;
            // I would like a _val.set() to avoid garbage
            _val = new IntToken(v);
            _firstFire = false;
        }
        output.send(0, _val);
    }

    /** Enable read on the next invocation of fire().
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _firstFire = true;
        return super.postfire();
    }

    private boolean _firstFire = true;
    private IntToken _val = new IntToken(0);
}
