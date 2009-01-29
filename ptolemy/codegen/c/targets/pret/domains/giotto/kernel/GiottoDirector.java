/* Code generator helper class associated with the GiottoDirector class.

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
package ptolemy.codegen.c.targets.pret.domains.giotto.kernel;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////
////GiottoDirector

/**
 Code generator helper associated with the GiottoDirector class. This class
 is also associated with a code generator.

 @author Ben Lickly
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class GiottoDirector extends ptolemy.codegen.c.domains.giotto.kernel.GiottoDirector {

    /** Construct the code generator helper associated with the given
     *  GiottoDirector.
     *  @param giottoDirector The associated
     *  ptolemy.domains.giotto.kernel.GiottoDirector
     */
    public GiottoDirector(ptolemy.domains.giotto.kernel.GiottoDirector giottoDirector) {
        super(giottoDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Attribute iterations = _director.getAttribute("iterations");
        if (iterations == null) {
            code.append(_eol + "while (true) {" + _eol);
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();
            if (iterationCount <= 0) {
                code.append(_eol + "while (true) {" + _eol);
            } else {
                // Declare iteration outside of the loop to avoid
                // mode" with gcc-3.3.3
                code.append(_eol + "int iteration;" + _eol);
                code.append("for (iteration = 0; iteration < "
                        + iterationCount + "; iteration ++) {" + _eol);
            }
        }


        
        code.append(generateFireCode());
     
        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        ActorCodeGenerator modelHelper = (ActorCodeGenerator) _getHelper(_director
                .getContainer());
        modelHelper.generateModeTransitionCode(code);

        /*if (callPostfire) {
            code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                    + "break;" + _eol + _INDENT2 + "}" + _eol);
        }
         */
       
        code.append(generatePostfireCode());
        
        Attribute period = _director.getAttribute("period");
        if (period != null) {
            Double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
           
            
            code.append("}" + _eol);
        }

        return code.toString();
    }
    
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        double period = _getPeriod();
        for (Actor actor : (List<Actor>) 
                ((TypedCompositeActor) _director.getContainer()).deepEntityList()) {
            ActorCodeGenerator helper = (ActorCodeGenerator) _getHelper(actor);
            
            // FIXME: generate deadline instruction w/ period
            
            // Note: Currently, the deadline instruction takes the number 
            // of cycle as argument. In the future, this should be changed
            // to use time unit, which is platform-independent. 
            // So for now, we will assume the clock speed to be 250 Mhz. 
            // Thus, in order to get a delay of t seconds (= period/frequency) 
            // for each actor, we will need to wait for 
            // period/frequency * 250,000,000 cycles/second.
            
            int cycles = (int)(250000000 * period / _getFrequency(actor));
            code.append("DEAD(" + cycles  + ");" + _eol);

            helper.generateFireCode();
            helper.generatePostfireCode();
        }
        return code.toString();
    }

    private int _getFrequency(Actor actor) throws IllegalActionException {
        Attribute frequency = ((Entity)actor).getAttribute("frequency");
        if (frequency == null) {
            return 1;
        } else {
            return ((IntToken) ((Variable) frequency).getToken()).intValue();
        }
    }

    private double _getPeriod() throws IllegalActionException {
        Attribute period = _director.getAttribute("period");
        double periodValue;
        if (period != null) {
            // FIXME: If this is not the top level director, this value will be
            // incorrect!
            periodValue = ((DoubleToken) ((Variable) period).getToken())
            .doubleValue();
        } else {
            // throw exception.
        }
        return periodValue;
    }

 
}
