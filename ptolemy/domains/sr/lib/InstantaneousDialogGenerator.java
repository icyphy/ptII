/* An actor that generates instantaneous dialog with a LookupTable.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// InstantaneousDialogGenerator
/**
An actor that generates instantaneous dialog with a LookupTable.  This actor
outputs incrementing integers on the <i>indexOutput</i> port.  A server is
expected to receive an index number and output a token associated with this
index, which the client receives on the <i>dataInput</i> port.  The client
then outputs this token on the <i>dataOutput</i> port.  All ports are single
ports.

@author Paul Whitaker
@version $Id$
@since Ptolemy II 2.0
*/

public class InstantaneousDialogGenerator extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public InstantaneousDialogGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Attribute(this, "_nonStrictMarker");

        dataInput = new TypedIOPort(this, "dataInput", true, false);
        indexOutput = new TypedIOPort(this, "indexOutput", false, true);
        dataOutput = new TypedIOPort(this, "dataOutput", false, true);

        indexOutput.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for data from a server.
     */
    public TypedIOPort dataInput;

    /** Output port for data.  The type is the same as dataInput.
     */
    public TypedIOPort dataOutput;

    /** Output port for data indices.  The type is integer.
     */
    public TypedIOPort indexOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output incrementing integers on the inputOutput.  Transfer tokens
     *  received on the dataInput port to the dataOutput port.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        indexOutput.send(0, new IntToken(_index));
        if (dataInput.isKnown(0)) {
            if (dataInput.hasToken(0)) {
                dataOutput.send(0, dataInput.get(0));
            } else {
                dataOutput.sendClear(0);
            }
        }
    }

    /** Initialize private variables.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _index = 0;
    }

    /** Increment the index number.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _index++;
        return super.postfire();
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(dataInput, dataOutput);
        removeDependency(dataInput, indexOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The index number to output on the indexOutput port.
    private int _index;

}






