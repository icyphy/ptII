/* Low-level output for JOP.

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

import ptolemy.actor.lib.Sink;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JopWritePort

/**
 <p>
 Write to a low-level I/O port on the Java processor JOP (see http://www.jopdesign.com).
 <p>
 In the simulation the values are just printed to stdout.

 @author Martin Schoeberl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mschoebe)
 @Pt.AcceptedRating Red (mschoebe)
 */
public class JopWritePort extends Sink {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public JopWritePort(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.INT);
        input.setMultiport(false);

        portAddress = new Parameter(this, "portAddress");
        // use the watch dog LED as default
        portAddress.setExpression("-125");
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
        JopWritePort newObject = (JopWritePort) super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.INT);
        return newObject;
    }

    /** If there is at least one token on the input ports save it for
     * output in postfire().
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _last_val = _val;
        if (input.hasToken(0)) {
            _last_val = ((IntToken) input.get(0)).intValue();
        }
    }

    /** Record the most recent token for the output value.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _val = _last_val;
        System.out.print(_val);
        return super.postfire();
    }

    private int _last_val;
    private int _val;
}
