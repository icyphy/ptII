/* An actor that reads expressions from a text file and outputs them as tokens.

 @Copyright (c) 2002-2005 The Regents of the University of California.
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

import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
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
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuj)
 */
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

    /** Output the data read in the preinitialize() or postfire() if
     *  there is any.
     *  @exception IllegalActionException If there's no director, or
     *   if the expression read from the file cannot be parsed.
     */
    public void fire() throws IllegalActionException {
        // NOTE: Since we don't call super.fire(), we have to do what
        // is done in the Source base class.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        if (_currentLine != null) {
            _expressionEvaluator.setExpression(_currentLine);
            output.broadcast(_expressionEvaluator.getToken());
        }
    }

    /** Open the file or URL and read the first line, and use the
     *  first line to set the type of the output.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the first line cannot be read.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Set the output type.
        _expressionEvaluator.setExpression(_currentLine);
        output.setTypeEquals(_expressionEvaluator.getType());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Variable used to evaluate expressions. */
    private Variable _expressionEvaluator;
}
