/* An adapter class for ptolemy.domains.de.lib.Register

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

//////////////////////////////////////////////////////////////////////////
//// Register

/**
 A adapter class for ptolemy.domains.de.lib.Register.

 @author Jeff C. Jensen
@version $Id$
@since Ptolemy II 8.0
 */
public class Register
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.de.lib.Register {
    /**
     *  Construct a Register adapter.
     *  @param actor The given ptolemy.actor.lib.TimeGap actor.
     */
    public Register(ptolemy.domains.de.lib.Register actor) {
        super(actor);
    }

    /** Return the name of the port that is the time source.
     *  @return The string "trigger".
     */
    public String getTimeSourcePortName() {
        return "trigger";
    }
}
