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
    
    /** Get letter corresponding to interrupt handler number.
     * @param number Interrupt handler number.
     * @return Corresponding letter.
     */
    public static Character _getLetter(int number) {
        switch (number) {
            case 156:
                return INTERRUPT_HANDLER_LETTER_156; 
            case 157:
                return INTERRUPT_HANDLER_LETTER_157; 
            case 158:
                return INTERRUPT_HANDLER_LETTER_158; 
            case 159:
                return INTERRUPT_HANDLER_LETTER_159; 
            case 162:
                return INTERRUPT_HANDLER_LETTER_162; 
            default:
                return null;
        } 
    }
    
    public static final Map<Integer, Character> _interruptHandlerLetters = 
        new HashMap<Integer, Character>(){
            {
                put(156, RenesasUtilities.INTERRUPT_HANDLER_LETTER_156);
                put(157, RenesasUtilities.INTERRUPT_HANDLER_LETTER_157);
                put(158, RenesasUtilities.INTERRUPT_HANDLER_LETTER_158);
                put(159, RenesasUtilities.INTERRUPT_HANDLER_LETTER_159);
                put(162, RenesasUtilities.INTERRUPT_HANDLER_LETTER_162); 
            }
        }; 
    
}
