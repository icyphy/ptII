/* A helper class for ptolemy.actor.lib.comm.HammingDecoder

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.comm;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;

//////////////////////////////////////////////////////////////////////////
//// HammingDecoder

/**
 A helper class for ptolemy.actor.lib.comm.HammingDecoder.

 @author Gang Zhou
 @version $Id$
 @see ptolemy.actor.lib.comm.HammingDecoder
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class HammingDecoder extends CCodeGeneratorHelper {
    /** Constructor method for the HammingDecoder helper.
     *  @param actor the associated actor
     */
    public HammingDecoder(ptolemy.actor.lib.comm.HammingDecoder actor) {
        super(actor);
    }
}
