/* An actor that reads a string from a file or URL, parses it assuming it is defining a record, and outputs the record.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TokenReader

/**
 An actor that reads a string expression from a file or URL upon receiving a
 signal on its <i>trigger</i> input port, and outputs a token that is the
 result of evaluating the read string. The file or URL is specified by the
 <i>FileOrURL</i> parameter or set using the <i>FileOrURL</i> port. If the
 file or URL cannot be read, the expression cannot be parsed successfully,
 or the resulting token does not match the type constraint of the output port,
 the value of the <i>errorHandlingStrategy</i> parameter determines the
 behavior of this actor.
 TODO: describe automatic port constraint setting
 FIXME: More here. Particularly, document output type handling.

 @author Edward A. Lee, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 10.0
 @deprecated Use LineReader followed by ExpressionToToken.
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
@Deprecated
public class TokenReader extends FileReader {

    /** Construct an actor with a name and a container.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown.
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
        outputType = new Parameter(this, "outputType");
        // must reset the output type to unknown (base class sets it to string)
        output.setTypeEquals(BaseType.UNKNOWN);

        errorHandlingStrategy = new StringParameter(this,
                "errorHandlingStrategy");
        errorHandlingStrategy.addChoice("Throw Exception");
        errorHandlingStrategy.addChoice("Do Nothing");

        // Show the firingCountLimit parameter last.
        firingCountLimit.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The error handled strategy.  The strategy to use if:
     *  <ul>
     *  <li> the file or URL cannot be read,</li>
     *  <li> the data read from the file or the URL cannot be parsed,</li>
     *  <li> the parsed token cannot be converted into a token of the type
     *  given by <i>outputType</i>, if such a type is given, or</li>
     *  <li> the parsed token cannot
     *  be converted to a token of the resolved type of the output, if no <i>outputType</i>
     *  is given.</li>
     *  </ul>
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

    /** Not implemented entirely yet. FIXME
     *  @exception IllegalActionException
     */
    @Override
    public void fire() throws IllegalActionException {
        try {
            super.fire();
        } catch (IllegalActionException exception) {
            String errorHandlingStrategyValue = errorHandlingStrategy
                    .stringValue();
            if (errorHandlingStrategyValue.equals("Throw Exception")) {
                throw exception;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do not establish the usual default type constraints.
     *  @return null
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /** FIXME: Send the specified string to the output.
     *  @param fileContents The contents of the file or URL that was read.
     *  @exception IllegalActionException If sending the data fails.
     */
    @Override
    protected void _handleFileData(String fileContents)
            throws IllegalActionException {

        if (_parser == null) {
            _parser = new PtParser();
        }
        ASTPtRootNode parseTree = _parser.generateParseTree(fileContents);

        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }

        // FIXME: Evaluating a parse tree that comes from an untrusted
        // source creates a security problem.
        Token result = _parseTreeEvaluator.evaluateParseTree(parseTree);

        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " + fileContents);
        }

        if (!output.getType().isCompatible(result.getType())) {
            // Handle the error according to the error policy.
            // FIXME: Fall through to the broadcast, which will throw
            // an exception.
        }
        output.broadcast(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The parser to use. */
    private PtParser _parser = null;

    /** The parse tree evaluator to use. */
    private ParseTreeEvaluator _parseTreeEvaluator = null;

}
