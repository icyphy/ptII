/* Base class for all code generation actors.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.kernel;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// CodeGenActor
/**
   Base class for all code generation actors.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class CodeGenActor extends TypedAtomicActor {

    // All the constructors are wrappers of the super class constructors.

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public CodeGenActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CodeGenActor(Workspace workspace) {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CodeGenActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add code from the code argument to the "default" stream.
     *  @param code The code to be added.
     */
    public void addCode(String code) {
        addCode(code, "default");
    }

    /** Add code from the code argument to the "default" stream.
     *  @param code StringBuffer containing the code to be added.
     */
    public void addCode(StringBuffer code) {
        addCode(code.toString(), "default");
    }

    /** Add code from the code argument to stream.
     *  @param code The code to be added.
     *  @param streamName The name of the stream.     
     */
    public void addCode(String code, String streamName) {
        StringBuffer buffer = (StringBuffer)_codeBlocks.get(streamName);
        if (buffer == null) {
            buffer = new StringBuffer();
            _codeBlocks.put(streamName, buffer);
        }
        buffer.append(processCode(code));
    }
    // NOTE: There is a three argument version
    // of the addCode() method, but I don't really understand
    // what the third argument means...

    public String getCode(String streamName) {
        StringBuffer buffer = (StringBuffer)_codeBlocks.get(streamName);
        if (buffer == null) {
            return "";
        }
        return buffer.toString();
    }

    public String processCode(String code) {
        // FIXME: This should transform the Pt Classic style
        // macro references like $ref(input).
        // For now, just leave the code unchanged.
        return code;
    }

    private HashMap _codeBlocks = new HashMap();
}
