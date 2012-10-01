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
        new HashMap<Integer, Character>(){
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
        new HashMap<Integer, Integer>(){
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
