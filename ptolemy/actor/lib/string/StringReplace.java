/* Replace an instance of a string with another input string according
 to a regular expression.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// StringReplace

/**
 On each firing, look for instances of the pattern specified by <i>pattern</i>
 in <i>stringToEdit</i> and replace them with the string given by
 <i>replacement</i>.  If <i>replaceAll</i> is true, then replace
 all instances that match <i>pattern</i>.  Otherwise, replace only
 the first instance that matches.  If there is no match, then the
 output is the string provided by <i>stringToEdit</i>, unchanged.
 The <i>pattern</i> is given by a regular expression.
 For a reference on regular expression syntax see:
 <a href="http://java.sun.com/docs/books/tutorial/extra/regex/index.html">
 http://java.sun.com/docs/books/tutorial/extra/regex/index.html</a>

 @author Antonio Yordan-Nones, Neil E. Turner, Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (djstone)
 @Pt.AcceptedRating Green (net)
 */
public class StringReplace extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringReplace(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create new parameters and ports.
        // Set default values of the parameters and type constraints.
        pattern = new PortParameter(this, "pattern");
        pattern.setStringMode(true);
        pattern.setExpression("");
        (new SingletonParameter(pattern.getPort(), "_showName"))
                .setToken(BooleanToken.TRUE);

        replaceAll = new Parameter(this, "replaceAll");
        replaceAll.setExpression("true");
        replaceAll.setTypeEquals(BaseType.BOOLEAN);

        replacement = new PortParameter(this, "replacement");
        replacement.setStringMode(true);
        replacement.setExpression("");
        (new SingletonParameter(replacement.getPort(), "_showName"))
                .setToken(BooleanToken.TRUE);

        stringToEdit = new PortParameter(this, "stringToEdit");
        stringToEdit.setStringMode(true);
        stringToEdit.setExpression("");
        (new SingletonParameter(stringToEdit.getPort(), "_showName"))
                .setToken(BooleanToken.TRUE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The string to edit by replacing substrings that match the
     *  specified pattern with the specified replacement. This is
     *  a string that defaults to the empty string.
     */
    public PortParameter stringToEdit;

    /** The output port on which the edited string is produced.
     *  This has type string.
     */
    public TypedIOPort output;

    /** The pattern used to pattern match and replace the stringToEdit
     *  string. It is an empty string by default.
     */
    public PortParameter pattern;

    /** When the boolean value is true, replace all instances that match the
     *  pattern, and when false, replace the first instance.
     */
    public Parameter replaceAll;

    /** The replacement string that replaces any matched instance of the
     *  pattern. It is an empty string by default.
     */
    public PortParameter replacement;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to compile a regular expression when
     *  it is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>pattern</i> and the regular expression fails to
     *   compile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pattern) {
            try {
                String patternValue = ((StringToken) pattern.getToken())
                        .stringValue();
                _pattern = Pattern.compile(patternValue);
            } catch (PatternSyntaxException ex) {
                String patternValue = ((StringToken) pattern.getToken())
                        .stringValue();
                throw new IllegalActionException(this, ex,
                        "Failed to compile regular expression \""
                                + patternValue + "\"");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Perform pattern matching and substring replacement, and output
     *  the modified string. If no match is found, output the
     *  unmodified stringToEdit string.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        replacement.update();
        stringToEdit.update();
        pattern.update();

        String replacementValue = ((StringToken) replacement.getToken())
                .stringValue();
        String stringToEditValue = ((StringToken) stringToEdit.getToken())
                .stringValue();
        boolean replaceAllTokens = ((BooleanToken) replaceAll.getToken())
                .booleanValue();

        Matcher match = _pattern.matcher(stringToEditValue);
        String outputString;

        if (replaceAllTokens) {
            outputString = match.replaceAll(replacementValue);
        } else {
            outputString = match.replaceFirst(replacementValue);
        }

        output.send(0, new StringToken(outputString));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The compiled regular expression.
    private Pattern _pattern;
}
