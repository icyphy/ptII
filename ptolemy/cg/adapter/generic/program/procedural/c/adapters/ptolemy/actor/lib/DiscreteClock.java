/*
 @Copyright (c) 2005-2011 The Regents of the University of California.
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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A adapter class for ptolemy.actor.lib.DiscreteClock.
 *
 * @author Jia Zou, William Lucas
 */
public class DiscreteClock extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the DiscreteClock adapter.
     * @param actor the associated actor
     */
    public DiscreteClock(ptolemy.actor.lib.DiscreteClock actor) {
        super(actor);
    }

    //    public String generateFireCode() throws IllegalActionException {
    //        CodeStream codeStream = _templateParser.getCodeStream();
    //        codeStream.clear();
    //        LinkedList args = new LinkedList();
    //        Parameter delay = ((ptolemy.actor.lib.TimeDelay) getComponent()).delay;
    //        double value = ((DoubleToken) delay.getToken()).doubleValue();
    //
    //        int intPart = (int) value;
    //        int fracPart = (int) ((value - intPart) * 1000000000.0);
    //        args.add(Integer.toString(intPart));
    //        args.add(Integer.toString(fracPart));
    //
    //        codeStream.appendCodeBlock("fireBlock", args);
    //        return processCode(codeStream.toString());
    //    }
    
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        Parameter stopTime = ((ptolemy.actor.lib.DiscreteClock) getComponent()).stopTime;
        double doubleStopTime = ((DoubleToken) stopTime.getToken()).doubleValue();
        ptolemy.actor.lib.DiscreteClock actor = (ptolemy.actor.lib.DiscreteClock) getComponent();
        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) actor.getContainer();
        ptolemy.actor.Director director = container.getDirector();
        double modelStopTime = director.getModelStopTime().getDoubleValue();
        if (doubleStopTime > modelStopTime)
        	doubleStopTime = modelStopTime;
        Parameter period = ((ptolemy.actor.lib.DiscreteClock) getComponent()).period;
        double doublePeriod = ((DoubleToken) period.getToken()).doubleValue();
        
        args.add(Double.toString(doubleStopTime));
        args.add(Double.toString(doublePeriod));
        
        Parameter offsetPar = ((ptolemy.actor.lib.DiscreteClock) getComponent()).offsets;
        Token offsetToken = offsetPar.getToken();
        Token[] offsets;
        double[] offsetsDouble = null;
        int size = 0;
        
        if (offsetToken instanceof ArrayToken) {
        	offsets = ((ArrayToken) offsetToken).arrayValue();
            size = offsets.length;
            args.add(Integer.toString(size));
            int i = 0;
            if (size > 0) {
            	if (offsets[0] instanceof DoubleToken) {
            		offsetsDouble = new double[size];
            	}
            	else {
                    throw new IllegalActionException("Token type at DiscreteClock "
                            + "not supported yet.");
                }
            }
            for (Token t : offsets) {
            	if (t instanceof DoubleToken) {
            		offsetsDouble[i++] = ((DoubleToken)t).doubleValue();
            	}
            	else {
                    throw new IllegalActionException("Token type at DiscreteClock "
                            + "not supported yet.");
                }
            }
        } else {
            throw new IllegalActionException("Token type at DiscreteClock "
                    + "not supported yet.");
        }
        
        String offsetsString = "";
        int i = 0;
        for (double offset : offsetsDouble) 
        	offsetsString += "$actorSymbol(offsets)["+ i++ +"] = " + Double.toString(offset) + "; ";
                
        args.add(offsetsString);
        
        Parameter valuesPar = ((ptolemy.actor.lib.DiscreteClock) getComponent()).values;
        Token valuesToken = valuesPar.getToken();
        Token[] values;
        double[] valuesDouble = null;
        int[] valuesInt = null;
        boolean[] valuesBool = null;
        size = 0;
        
        if (valuesToken instanceof ArrayToken) {
        	values = ((ArrayToken) valuesToken).arrayValue();
            size = values.length;
            args.add(Integer.toString(size));
            i = 0;
            if (size > 0) {
            	if (values[0] instanceof DoubleToken) {
            	    valuesDouble = new double[size];
            	}
            	else if (values[0] instanceof IntToken) {
            	    valuesInt = new int[size];
            	}
            	else if (values[0] instanceof BooleanToken) {
                    valuesBool = new boolean[size];
            	}
            	else {
                    throw new IllegalActionException("Token type at DiscreteClock "
                            + "not supported yet.");
                }
            }
            for (Token t : values) {
            	if (t instanceof DoubleToken) {
            	    valuesDouble[i++] = ((DoubleToken)t).doubleValue();
            	}
            	else if (t instanceof IntToken) {
            	    valuesInt[i++] = ((IntToken)t).intValue();
            	}
            	else if (t instanceof BooleanToken) {
            	    valuesBool[i++] = ((BooleanToken)t).booleanValue();
            	}
            	else {
                    throw new IllegalActionException("Token type at DiscreteClock "
                            + "not supported yet.");
                }
            }
        } else {
            throw new IllegalActionException("Token type at DiscreteClock "
                    + "not supported yet.");
        }
        
        String valuesString = "";
        i = 0;
        if (valuesDouble != null)
            for (double value : valuesDouble) 
                valuesString += "$actorSymbol(values)["+ i++ +"] = " + Double.toString(value) + "; ";
        else if (valuesInt != null)
            for (int value : valuesInt) 
                valuesString += "$actorSymbol(values)["+ i++ +"] = " + Integer.toString(value) + "; ";
        else if (valuesBool != null)
            for (boolean value : valuesBool) 
                valuesString += "$actorSymbol(values)["+ i++ +"] = " + Boolean.toString(value) + "; ";
            
        args.add(valuesString);
        
        codeStream.appendCodeBlock("initBlock", args);
        
        if (((ptolemy.actor.lib.DiscreteClock) getComponent()).start.isOutsideConnected()) {
            codeStream.appendCodeBlock("startConnectedInit");
        }
        return processCode(codeStream.toString());
    }
    
    /**
     * Generate the fire code of a single event.
     * @return The generated code.
     * @exception IllegalActionException 
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
    	CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        
        ptolemy.actor.lib.DiscreteClock clock = (ptolemy.actor.lib.DiscreteClock) getComponent();
        
        if (clock.start.numberOfSources() > 0)
            codeStream.appendCodeBlock("startConnected");
        
        if (clock.stop.numberOfSources() > 0)
            codeStream.appendCodeBlock("stopConnected");
        
        if (((PortParameter)clock.period).getPort().isOutsideConnected())
            codeStream.appendCodeBlock("periodConnected");
        
        if (clock.trigger.numberOfSources() > 0) {
            // Have to consume all trigger inputs.
            for (int i = 0; i < clock.trigger.getWidth(); i++) {
                args.clear();
                args.add(Integer.toString(i));
                codeStream.appendCodeBlock("triggerConnected", args);
            }
        }
        
        args.clear();
        codeStream.appendCodeBlock("fireTestBlock");
        
        return processCode(codeStream.toString());
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
        
        Parameter valuePar = ((ptolemy.actor.lib.DiscreteClock) getComponent()).values;
        Token valueToken = valuePar.getToken();
        Token[] values;
        int size = 0;
        
        if (valueToken instanceof ArrayToken) {
            values = ((ArrayToken) valueToken).arrayValue();
            size = values.length;
        }
        args.add(Integer.toString(size));
        args.add(Boolean.toString(((ptolemy.actor.lib.DiscreteClock) getComponent()).trigger.numberOfSources() > 0));
            
        codeStream.appendCodeBlock("postfireBlock", args);
        return processCode(codeStream.toString());
    }
}
