/* Interface for code generator helper classes.

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
package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DirectorCodeGenerator

/** FIXME: class comments needed.
 *
 *  @author Man-Kit Leung
 *  @version $Id: DirectorCodeGenerator.java 51851 2008-12-11 22:30:05Z mankit $
 *  @since Ptolemy II 7.2
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class Director implements ExecutableCodeGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public PartialResult newReceiver() {
        
        
        return null;
        
    }

    public PartialResult transferInputs(PartialResult port) {
        return null;
        
    }

    public PartialResult transferOutputs(PartialResult port) {
        return null;
        
    }

    public PartialResult fire() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult iterate(PartialResult countExpression)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult postfire() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult prefire() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult preintialize() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult stop() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public NamedObj getComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    public PartialResult intialize() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCodeGenerator(CodeGenerator codeGenerator) {
        // TODO Auto-generated method stub
        
    }

    public PartialResult wrapup() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }    

}
