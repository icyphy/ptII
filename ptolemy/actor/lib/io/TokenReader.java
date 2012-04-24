/* An actor that reads a string from a file or URL, parses it assuming it is defining a record, and outputs the record.

 Copyright (c) 2003-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.io;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TokenReader

/**
 An actor that reads a string from a file or URL, parses it assuming it is defining a record, and outputs the record.
 
 FIXME: More here. Particularly, document output type handling.

 @author Edward A. Lee
 @author Marten Lohstroh
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class TokenReader extends FileReader {
    
    /** Construct an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TokenReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.GENERAL);
        outputType = new Parameter(this, "outputType");
        
        errorHandlingStrategy = new StringParameter(this, "errorHandlingStrategy");
        errorHandlingStrategy.addChoice("Throw Exception");
        errorHandlingStrategy.addChoice("Do Nothing");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The strategy to use if the file or URL cannot be read, the data read from the file
     *  or URL cannot be parsed, or the parsed token cannot be converted into a token of the type
     *  given by <i>outputType</i>, if such a type is given.
     *  This is a string that has the following
     *  possible values: "Do Nothing" or "Throw Exception", where
     *  "Throw Exception" is the default.
     */
    public StringParameter errorHandlingStrategy;

    /** If this parameter has a value, then the value specifies the
     *  type of the output port. When the actor reads from the file
     *  or URL, it expects the data read to be a string that can be
     *  parsed into a token that is convertible to (or identical to)
     *  this output type. If it is not, then the action taken is
     *  specified by the <i>errorHandlingStrategy</i> parameter.
     *  If this parameter has no value (the default), then the
     *  output type will be set to match whatever is first read from the
     *  file or URL and will be updated on each subsequent firing if
     *  the data read from the file or URL cannot be converted to the
     *  type determined by the first read. 
     */
    public Parameter outputType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME.
     *  @exception IllegalActionException FIXME.
     */
    public void fire() throws IllegalActionException {
        try {
            super.fire();
        } catch (IllegalActionException exception) {
            String errorHandlingStrategyValue = errorHandlingStrategy.stringValue();
            if (errorHandlingStrategyValue.equals("Throw Exception")) {
                throw exception;
            }
        }
    }
    
    /** FIXME
     * 
     */
    public void preinitialize() throws IllegalActionException {
        Token outputTypeValue = outputType.getToken();
        if (outputTypeValue != null) {
            // An output type has been specified.
            // Force the output to this type.
        	// TODO: use setTypeAtMost() instead
            output.setTypeEquals(outputTypeValue.getType());
        } else {
            // Declare constraints that the output type must
            // be greater than or equal to the types of all the
            // destination ports.
            // 
            // FIXME: The base class sets the output type
            // to String. That may need to change. It seems that just
            // removing the string declaration in the base class doesn't
            // work because then default constraints result in the output
            // being greater than or equal to the trigger input, which is
            // boolean.
            
        	CPO lattice = TypeLattice.lattice();
        	List<Type> portTypeList = new LinkedList<Type>();
            List<TypedIOPort> destinations = output.sinkPortList();
            
            for (TypedIOPort destination : destinations) {
            	portTypeList.add(destination.getType());
            }
            output.setTypeEquals((Type) lattice.greatestLowerBound(portTypeList.toArray()));
            //output.setTypeAtMost((Type) lattice.greatestLowerBound(portTypeList.toArray()));
            
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** FIXME: Send the specified string to the output.
     *  @param fileContents The contents of the file or URL that was read.
     *  @throws IllegalActionException If sending the data fails.
     */
    protected void _handleFileData(String fileContents)
            throws IllegalActionException {
        
        if (_parser == null) {
            _parser = new PtParser();
        }
        ASTPtRootNode parseTree = _parser.generateParseTree(fileContents);

        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }

        Token result = _parseTreeEvaluator.evaluateParseTree(parseTree);

        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " + fileContents);
        }

        Token outputTypeValue = outputType.getToken();
        // TODO: shouldn't output port type be set regardless of where the definition comes from? Also: do this in preinitialize()
        
        Token convertedToken = output.getType().convert(result);
        //output.broadcast(convertedToken);
        output.broadcast(result);
        if (outputTypeValue != null) {
            // An output type has been specified. Try to convert
            // the parsed token to that type.
            //Token convertedToken = outputTypeValue.getType().convert(result);
            //output.broadcast(convertedToken);
        } else {
        	
            // An output data type has not been specified.
            // First, try to convert to the type that the output resolved to. 
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    /** The parser to use. */
    private PtParser _parser = null;

    /** The parse tree evaluator to use. */
    private ParseTreeEvaluator _parseTreeEvaluator = null;
}
