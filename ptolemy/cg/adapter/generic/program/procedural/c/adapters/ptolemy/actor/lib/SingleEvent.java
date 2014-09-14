/* An adapter class for ptolemy.actor.lib.SingleEvent

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.LinkedList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SingleEvent

/**
 A adapter class for ptolemy.actor.lib.SingleEvent.

 @author Jia Zou, Willian Lucas
 @version $Id$
 @since Ptolemy II 10.0
 */
public class SingleEvent extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct a SingleEvent adapter.
     *  @param actor The given ptolemy.actor.lib.SingleEvent actor.
     */
    public SingleEvent(ptolemy.actor.lib.SingleEvent actor) {
        super(actor);
    }

    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        Parameter time = ((ptolemy.actor.lib.SingleEvent) getComponent()).time;
        double doubleTime = ((DoubleToken) time.getToken()).doubleValue();
        // The value should not be used in the initialize block, only on the fire block
        /*Parameter valuePar = ((ptolemy.actor.lib.SingleEvent) getComponent()).value;
        double value;
        Token valueToken = valuePar.getToken();
        if (valueToken instanceof BooleanToken) {
            if (((BooleanToken) valueToken).booleanValue() == true) {
                value = 1;
            } else {
                value = 0;
            }
        } else if (valueToken instanceof IntToken) {
            value = ((IntToken) valueToken).intValue();
        } else if (valueToken instanceof DoubleToken) {
            value = ((DoubleToken) valueToken).doubleValue();
        } else {
            throw new IllegalActionException("Token type at single "
                    + "event not supported yet.");
        }*/
        int intPart = (int) doubleTime;
        int fracPart = (int) ((doubleTime - intPart) * 1000000000.0);
        args.add(Integer.toString(intPart));
        args.add(Integer.toString(fracPart));
        //args.add(Double.toString(value));

        codeStream.appendCodeBlock("initBlock", args);
        return processCode(codeStream.toString());
    }

    /**
     * Generate the fire code of a single event.
     * @return The generated code.
     * @exception IllegalActionException If thrown by the super class
     * or while getting a token.
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        // FIXME : not sure if this call is really necessary
        super._generateFireCode();
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();

        Parameter valuePar = ((ptolemy.actor.lib.SingleEvent) getComponent()).value;
        double value;
        Token valueToken = valuePar.getToken();
        if (valueToken instanceof BooleanToken) {
            if (((BooleanToken) valueToken).booleanValue() == true) {
                value = 1;
            } else {
                value = 0;
            }
        } else if (valueToken instanceof IntToken) {
            value = ((IntToken) valueToken).intValue();
        } else if (valueToken instanceof DoubleToken) {
            value = ((DoubleToken) valueToken).doubleValue();
        } else {
            throw new IllegalActionException("Token type at single "
                    + "event not supported yet.");
        }

        args.add(Double.toString(value));

        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }

}
