/*
 @Copyright (c) 2005-2014 The Regents of the University of California.
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
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A adapter class for ptolemy.actor.lib.PoissonClock.
 *
 * @author Jia Zou, William Lucas
@version $Id$
@since Ptolemy II 10.0
 */
public class PoissonClock extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the PoissonClock adapter.
     * @param actor the associated actor
     */
    public PoissonClock(ptolemy.actor.lib.PoissonClock actor) {
        super(actor);
    }

    /** Generate the initialize code.
     *  @return The initialize code.
     *  @exception IllegalActionException If thrown while generating
     *  the initialization code, while appending the code block or
     *  while converting the codeStream to a string.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        Parameter stopTime = ((ptolemy.actor.lib.PoissonClock) getComponent()).stopTime;
        double doubleStopTime = ((DoubleToken) stopTime.getToken())
                .doubleValue();
        ptolemy.actor.lib.PoissonClock actor = (ptolemy.actor.lib.PoissonClock) getComponent();
        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) actor
                .getContainer();
        ptolemy.actor.Director director = container.getDirector();
        double modelStopTime = director.getModelStopTime().getDoubleValue();
        if (doubleStopTime > modelStopTime) {
            doubleStopTime = modelStopTime;
        }
        Parameter meanTime = ((ptolemy.actor.lib.PoissonClock) getComponent()).meanTime;
        double doubleMeanTime = ((DoubleToken) meanTime.getToken())
                .doubleValue();
        Parameter fireAtStart = ((ptolemy.actor.lib.PoissonClock) getComponent()).fireAtStart;
        boolean boolFireAtStart = ((BooleanToken) fireAtStart.getToken())
                .booleanValue();

        args.add(Double.toString(doubleStopTime));
        args.add(Double.toString(doubleMeanTime));
        args.add(Boolean.toString(boolFireAtStart));

        Parameter valuesPar = ((ptolemy.actor.lib.PoissonClock) getComponent()).values;
        Token valuesToken = valuesPar.getToken();
        Token[] values;
        double[] valuesDouble = null;
        int[] valuesInt = null;
        int size = 0;

        if (valuesToken instanceof ArrayToken) {
            values = ((ArrayToken) valuesToken).arrayValue();
            size = values.length;
            args.add(Integer.toString(size));
            int i = 0;
            if (size > 0) {
                if (values[0] instanceof DoubleToken) {
                    valuesDouble = new double[size];
                } else if (values[0] instanceof IntToken) {
                    valuesInt = new int[size];
                } else {
                    throw new IllegalActionException(
                            "Token type at PoissonClock "
                                    + "not supported yet.");
                }
            }
            for (Token t : values) {
                if (t instanceof DoubleToken) {
                    valuesDouble[i++] = ((DoubleToken) t).doubleValue();
                } else if (t instanceof IntToken) {
                    valuesInt[i++] = ((IntToken) t).intValue();
                } else {
                    throw new IllegalActionException(
                            "Token type at PoissonClock "
                                    + "not supported yet.");
                }
            }
        } else {
            throw new IllegalActionException("Token type at PoissonClock "
                    + "not supported yet.");
        }

        StringBuffer valuesString = new StringBuffer();
        int i = 0;
        if (valuesDouble != null) {
            for (double value : valuesDouble) {
                valuesString.append("$actorSymbol(values)[" + i++ + "] = "
                        + Double.toString(value) + "; ");
            }
        } else if (valuesInt != null) {
            for (int value : valuesInt) {
                valuesString.append("$actorSymbol(values)[" + i++ + "] = "
                        + Integer.toString(value) + "; ");
            }
        }

        args.add(valuesString.toString());

        long longPrivateSeed = 0;
        Parameter privateSeed = ((ptolemy.actor.lib.PoissonClock) getComponent()).privateSeed;
        if (privateSeed.getToken() instanceof LongToken) {
            longPrivateSeed = ((LongToken) privateSeed.getToken()).longValue();
        }
        args.add(Long.toString(longPrivateSeed));

        long longSeed = 0;
        Parameter seed = ((ptolemy.actor.lib.PoissonClock) getComponent()).seed;
        if (seed.getToken() instanceof LongToken) {
            longSeed = ((LongToken) seed.getToken()).longValue()
                    + ((ptolemy.actor.lib.PoissonClock) getComponent())
                    .getFullName().hashCode();
        }
        args.add(Long.toString(longSeed));

        codeStream.appendCodeBlock("initBlock", args);
        return processCode(codeStream.toString());
    }

    /**
     * Generate the fire code of a Poisson Clock.
     * @return The generated code.
     * @exception IllegalActionException If thrown while appending the code block or
     * while converting the codeStream to a string.
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        String parentFireCode = super._generateFireCode();
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();

        codeStream.appendCodeBlock("fireBlockInit");
        for (int i = 0; i < ((ptolemy.actor.lib.PoissonClock) getComponent()).trigger
                .getWidth(); i++) {
            args.clear();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("fireBlockTrigger", args);
        }

        codeStream.appendCodeBlock("fireBlockEnd");
        return parentFireCode + processCode(codeStream.toString());
    }

    /**
     * Generate the postfire code. We do not call the super
     * method, because we have arguments to add here
     *
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();

        Parameter valuePar = ((ptolemy.actor.lib.PoissonClock) getComponent()).values;
        Token valueToken = valuePar.getToken();
        Token[] values;
        int size = 0;

        if (valueToken instanceof ArrayToken) {
            values = ((ArrayToken) valueToken).arrayValue();
            size = values.length;
        }
        args.add(Integer.toString(size));

        codeStream.appendCodeBlock("postfireBlock", args);
        return processCode(codeStream.toString());
    }
}
