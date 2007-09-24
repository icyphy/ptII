/* A helper class for ptolemy.actor.lib.MultiplyDivide

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 A helper class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class MultiplyDivide extends CCodeGeneratorHelper {
    /**
     * Constructor method for the MultiplyDivide helper.
     * @param actor the associated actor
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generate code that loops through each
     * input multiport and combine (multiply or divide) them.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        Type type = actor.output.getType();
        boolean divideOnly = actor.multiply.getWidth() == 0;

        ArrayList args = new ArrayList();

        String blockType = isPrimitive(type) ? "" : "Token";
        
        boolean upgradeMultiply = isPrimitive(actor.multiply.getType()) && !isPrimitive(type); 
        boolean upgradeDivide = isPrimitive(actor.divide.getType()) && !isPrimitive(type); 

        if (!divideOnly) {
            if (upgradeMultiply) {
                args.add(codeGenType(actor.multiply.getType()));
            }                
            _codeStream.appendCodeBlock("SetNumeratorBlock", args);
            
        } else {
            _codeStream.appendCodeBlock(blockType + "SetNumeratorOneBlock");
        }

        args.clear();
        if (actor.divide.getWidth() > 0) {
            if (upgradeDivide) {
                args.add(codeGenType(actor.divide.getType()));
            }                
            _codeStream.appendCodeBlock("SetDenominatorBlock", args);
        }

        args.clear();
        args.add(Integer.valueOf(0));
        for (int i = 1; i < actor.multiply.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            if (upgradeMultiply) {
                args.add(codeGenType(actor.multiply.getType()));
            }                
            _codeStream.appendCodeBlock(blockType + "MultiplyBlock", args);
        }

        for (int i = 1; i < actor.divide.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            if (upgradeDivide) {
                args.add(codeGenType(actor.divide.getType()));
            }                
            _codeStream.appendCodeBlock(blockType + "DivideBlock", args);
        }

        if (actor.divide.getWidth() == 0) {
            _codeStream.appendCodeBlock("NumeratorOutputBlock");
        } else {
            _codeStream.appendCodeBlock(blockType + "OutputBlock");
        }

        return processCode(_codeStream.toString());
    }
}
