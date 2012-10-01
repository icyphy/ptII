/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2012 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions to set up mapping tables for Renesas platform.
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (derler)
 * @Pt.AcceptedRating Red (derler)
 */
public class RenesasUtilities {

    public static char INTERRUPT_HANDLER_LETTER_156 = 'A';
    public static char INTERRUPT_HANDLER_LETTER_157 = 'B';
    public static char INTERRUPT_HANDLER_LETTER_158 = 'C';
    public static char INTERRUPT_HANDLER_LETTER_159 = 'D';
    public static char INTERRUPT_HANDLER_LETTER_162 = 'E';
    public static char INTERRUPT_HANDLER_LETTER_180 = 'A';
    public static char INTERRUPT_HANDLER_LETTER_181 = 'B';
    public static char INTERRUPT_HANDLER_LETTER_182 = 'C';
    public static char INTERRUPT_HANDLER_LETTER_183 = 'D';



    public static final Map<Integer, Character> interruptHandlerLetters =
        new HashMap<Integer, Character>() {
            {
                put(156, RenesasUtilities.INTERRUPT_HANDLER_LETTER_156);
                put(157, RenesasUtilities.INTERRUPT_HANDLER_LETTER_157);
                put(158, RenesasUtilities.INTERRUPT_HANDLER_LETTER_158);
                put(159, RenesasUtilities.INTERRUPT_HANDLER_LETTER_159);
                put(162, RenesasUtilities.INTERRUPT_HANDLER_LETTER_162);
                put(180, RenesasUtilities.INTERRUPT_HANDLER_LETTER_180);
                put(181, RenesasUtilities.INTERRUPT_HANDLER_LETTER_181);
                put(182, RenesasUtilities.INTERRUPT_HANDLER_LETTER_182);
                put(183, RenesasUtilities.INTERRUPT_HANDLER_LETTER_183);
            }
        };

    public static final Map<Integer, Integer> timerNumbers =
        new HashMap<Integer, Integer>() {
            {
                put(156, 0);
                put(157, 0);
                put(158, 0);
                put(159, 0);
                put(162, 0);
                put(180, 0);
                put(181, 0);
                put(182, 0);
                put(183, 0);
            }
        };


}
