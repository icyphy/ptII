/* A TypedIOPort with parameters specific to the Giotto domain.

Copyright (c) 1997-2004 The Regents of the University of California.
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

review sendInside
*/

package ptolemy.apps.cgc;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TypedIOPort
/**

   @author N.Vinay Krishnan, Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (vkris)
   @Pt.AcceptedRating Red (cxh)
*/

public class CPort extends TypedIOPort {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a CPort with no container and no name that is
     *  neither an input nor an output.
     */
    public CPort() {
        super();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public CPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a CPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the CActor interface, or an exception will be
     *  thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public CPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a CPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the CActor interface or an exception will be thrown.
     *  This port class features additional parameters specific to the
     *  Giotto domain. If the port is specified as an output port, then
     *  it shall have two parameters specified by default. 
     *  a) The parameter "_init" which specifies the value of the port
     *     until the container has fired and assigned it a value. The
     *     default value of this parameter is 0.
     *  b) The parameter "_length", which is used only if the type is
     *     an array. This specifies the length of the array. The default
     *     value of this parameter is 1.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public CPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);

        if (isOutput) {
            _init = new Parameter(this, "_init");
            _init.setExpression("0");

            _length = new Parameter(this, "_length");
            _length.setExpression("1");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Overrides the base class to add the functionality that if the
     *  port if made an output port, then we create two parameters 
     *  _init and _length
     *  @param isOutput True to make the port an output.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (not thrown in this base class).
     */
    public void setOutput(boolean isOutput) throws IllegalActionException {
        super.setOutput(isOutput);

        try {
            if (isOutput) {
                _init = new Parameter(this, "_init");
                _init.setExpression("0");
    
                _length = new Parameter(this, "_length");
                _length.setExpression("1");
            }
            else {
                if(_init != null) { // Since both_init and _length are being made null together
                    _init.setVisibility(Settable.NONE);
                    _length.setVisibility(Settable.NONE);
                    _init = null;
                    _length = null;
                }
            }
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, ex, null);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The initial value of the port */
    public Parameter _init = null;
    
    /** The length of the array if the type is an array */
    public Parameter _length = null;
    
}
