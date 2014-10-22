/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package doc.tutorial;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Precocious

/**
This actor adaptively establishes connections by searching for an
unused input port in the model and connecting to it. If the output
is connected to something (the width of the output port is greater
than zero), then the actor sends an integer on the output port and
requests a refiring at a time in the future determined by the
<i>firingPeriod</i> parameter. The value of the output is simply
the count of the firing, starting at 1.
<p>
If the output is not connected to anything, then the actor will
attempt to connect it. It does this by issuing a change request
that, when executed, will search for an unused input port (any
unused input port) in and actor in the same container as this actor,
and then will connect to it.
<p>
Note that getWidth() is used rather than numberOfSinks() to determine
whether the output is connected. This way, this actors search for an
input port can be silenced by just connecting it to a relation.
<p>
This actor is an illustration of the capability actors can have to affect
their environment, to detect faults (in this case, missing connections),
and to repair the model.  It is designed to be used in the DE domain,
or any domain that respects fireAt() calls.

@author Edward A. Lee
@see IOPort#getWidth()
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (eal)
 */
public class Precocious extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Precocious(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT);

        firingPeriod = new Parameter(this, "firingPeriod");
        firingPeriod.setTypeEquals(BaseType.DOUBLE);
        firingPeriod.setExpression("0.1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The output port, which has type int. */
    public TypedIOPort output;

    /** The period at which this actor will execute.  This is a double
     *  with a default value of 0.1.
     */
    public Parameter firingPeriod;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the output port is connected, then send a count of the firing
     *  to the output; otherwise, issue a change request that will search
     *  for an input port to connect to.
     *  @exception IllegalActionException If there is no director or if
     *   producing the output causes an exception.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _count++;
        if (output.isOutsideConnected()) {
            output.send(0, new IntToken(_count));
        } else {
            ChangeRequest request = new ChangeRequest(this, "Find recipient") {
                @Override
                protected void _execute() throws IllegalActionException {
                    CompositeEntity container = (CompositeEntity) getContainer();
                    List<Entity> entities = container.entityList();
                    for (Entity entity : entities) {
                        List<IOPort> ports = entity.portList();
                        for (IOPort port : ports) {
                            if (port.isInput() && !port.isOutsideConnected()) {
                                container.connect(output, port);
                                return;
                            }
                        }
                    }
                }
            };
            requestChange(request);
        }

        double period = ((DoubleToken) firingPeriod.getToken()).doubleValue();
        getDirector().fireAt(this, getDirector().getModelTime().add(period));
    }

    /** Initialize this actor, which in this case requests a firing at
     *  the current time.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 0;
        getDirector().fireAtCurrentTime(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Count of the number of firings. */
    private int _count;
}
