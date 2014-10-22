/* An actor that reads expressions from a text file and outputs them as tokens.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.io;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ExpressionReader

/**
 This actor reads a file or URL, one line at a time, evaluates each
 line as an expression, and outputs the token resulting from the
 evaluation. The first line in the file determines the data type
 of the output. All other lines must contain expressions that
 evaluate to the same type or a subtype, or a run-time type error will occur.
 The file or URL is specified using any form acceptable
 to FileParameter.  If an end of file is reached, then prefire() and
 postfire() will both return false.

 @see ExpressionWriter
 @see ptolemy.data.expr.FileParameter
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.2
 @deprecated Use LineReader and ExpressionToToken.
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuj)
 */
@Deprecated
public class ExpressionReader extends LineReader {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExpressionReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _expressionEvaluator = new Variable(this, "_expressionEvaluator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ExpressionReader newObject = (ExpressionReader) super.clone(workspace);

        newObject._expressionEvaluator = (Variable) newObject
                .getAttribute("_expressionEvaluator");
        return newObject;
    }

    /** Output the data read in the preinitialize() or postfire() if
     *  there is any.
     *  @exception IllegalActionException If there's no director, or
     *   if the expression read from the file cannot be parsed.
     */
    @Override
    public void fire() throws IllegalActionException {
        // NOTE: Since we don't call super.fire(), we have to do what
        // is done in the Source base class.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
        // Initialize method in superclass closes the file.
        // We need to reopen it and reread it.
        if (_firstFiring) {
            _openAndReadFirstTwoLines();
            _firstFiring = false;
        }

        if (_currentLine != null) {
            _expressionEvaluator.setExpression(_currentLine);
            output.broadcast(_expressionEvaluator.getToken());
        }
        if (_nextLine == null) {
            endOfFile.broadcast(BooleanToken.TRUE);
        } else {
            endOfFile.broadcast(BooleanToken.FALSE);
        }
    }

    /** Open the file or URL and read the first line, and use the
     *  first line to set the type of the output.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the first line cannot be read.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Read the first two lines so that we can use the first
        // line to set the type of the output. The file will be
        // closed and re-opened in initialize().
        _openAndReadFirstTwoLines();

        // Set the output type.
        _expressionEvaluator.setExpression(_currentLine);
        output.setTypeEquals(_expressionEvaluator.getType());
    }

    /** Override the base class to clear memory of any
     *  possibly erroneous expression.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // This is necessary because if there is an
        // invalid expression here, then validate() will
        // fail when the model is next run.
        // Thanks to Adriana Ricchiuti for diagnosing this.
        _expressionEvaluator.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Variable used to evaluate expressions. */
    private Variable _expressionEvaluator;
}
