/* Code generator helper for typed composite actor.

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
package ptolemy.codegen.vhdl.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for typed composite actor.

 @author Gang Zhou, Contributors: Teale Fristoe
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends VHDLCodeGeneratorHelper {

    /** Construct the VHDL code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /** Generate the fire code of the associated composite actor. This method
     *  first generates code for transferring any data from the input
     *  ports of this composite to the ports connected on the inside
     *  by calling the generateTransferInputsCode() method of the
     *  local director helper. It then invokes the generateFireCode()
     *  method of its local director helper.  After the
     *  generateFireCode() method of the director helper returns,
     *  generate code for transferring any output data created by
     *  calling the local director helper's
     *  generateTransferOutputsCode() method.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated
     *  with an actor throws it while generating fire code for the
     *  actor, or the director helper throws it while generating code
     *  for transferring data.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer result = new StringBuffer();

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actor);

            if (helper.doGenerate()) {
                result.append(helper.generateFireCode());
            }
        }
        return processCode(result.toString());
    }

    /** Get the header files needed by the code generated from this helper 
     *  class. It returns the result of calling getHeaderFiles() method of 
     *  the helpers of all contained actors.
     * 
     *  @return A set of strings that are header files. 
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating header files for the actor.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {

            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actors
                    .next());

            if (helper.doGenerate()) {
                files.addAll(helper.getHeaderFiles());
            }
        }
        return files;
    }

    /** Return the include directories specified in the "includeDirectories"
     *  blocks of the templates of the actors contained in this CompositeActor.
     *  @return A Set of the include directories.
     *  @exception IllegalActionException If thrown when gathering include
     *   directories.
     */
    public Set getIncludeDirectories() throws IllegalActionException {
        Set includeDirectories = new HashSet();

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actor);

            if (helper.doGenerate()) {
                includeDirectories.addAll(helper.getIncludeDirectories());
            }
        }

        return includeDirectories;
    }

    /** Return the libraries specified in the "libraries" blocks of the 
     *  templates of the actors contained in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new HashSet();

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actor);

            if (helper.doGenerate()) {
                libraries.addAll(helper.getLibraries());
            }
        }

        return libraries;
    }

    /** Generate a set of shared code fragments of the associated
     *  composite actor.  It returns the result of calling
     *  getSharedCode() method of the helpers of all contained actors.
     *  
     *  @return a set of shared code fragments.
     *  @exception IllegalActionException If the helper associated with
     *  an actor throws it while generating shared code for the actor.
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            VHDLCodeGeneratorHelper helper = _getHelper((NamedObj) actor);

            if (helper.doGenerate()) {
                sharedCode.addAll(helper.getSharedCode());
            }
        }

        return sharedCode;
    }

    /**
     * Throw an exception.
     * @exception IllegalActionException Thrown if a composite is asked
     *  if it is synthesizable. 
     */
    public boolean isSynthesizable() throws IllegalActionException {
        throw new IllegalActionException(this,
                "TypedCompositeActor should not be "
                        + "asked if it is synthesizeable.");
    }
}
