/* RTMaude Code generator helper for Transformer

 Copyright (c) 2011-2011 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor.lib;

import ptolemy.codegen.rtmaude.actor.TypedAtomicActor;

//////////////////////////////////////////////////////////////////////////
////TypedAtomicActor

/**
 * Generate RTMaude code for a Transformer.
 *
 * @see ptolemy.actor.lib.Transformer
 * @author Christopher Brooks
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (cxh)
 * @Pt.AcceptedRating red (cxh)
 */
public class Transformer extends TypedAtomicActor {
    /** Construct the code generator helper associated
     *  with the given Transformer.
     *  @param component The associated TypedAtomicActor.
     */
    public Transformer(ptolemy.actor.lib.Transformer component) {
        super(component);
    }
}
