/* A code generation helper class for domains.sdf.lib.DownSample
 @Copyright (c) 2007 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.lib;

import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;


/**
 A code generation helper class for ptolemy.domains.sdf.lib.DownSample.

 @author Man-Kit Leung, Dai Bui
 @version $Id: DownSample.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class DownSample extends ProgramCodeGeneratorAdapter {

    /**
     * Construct a DownSample helper.
     * @param actor The associated actor.
     */
    public DownSample(ptolemy.domains.sdf.lib.DownSample actor) {
        super(actor);
    }
}
