/* Replace an instance of a string with another input string according
 to simple matching (no regex)

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringSimpleReplace

/**
 On each firing, look for instances of the pattern specified by <i>pattern</i>
 in <i>stringToEdit</i> and replace them with the string given by
 <i>replacement</i>.  If <i>replaceAll</i> is true, then replace
 all instances that match <i>pattern</i>.  Otherwise, replace only
 the first instance that matches.  If there is no match, then the
 output is the string provided by <i>stringToEdit</i>, unchanged.
 The <i>pattern</i> is <b>not</b> a regular expression, to use
 a regular expression, see {@link ptolemy.actor.lib.string.StringReplace}.
 <p>
 The <i>replacement</i> string, as usual with string-valued parameters
 in Ptolemy II, can include references to parameter values in scope.
 E.g., if the enclosing composite actor has a parameter named "x"
 with value 1, say, then the replacement string a${x}b will become
 "a1b".</p>
 <p>Note that if the <i>pattern</i> is the two character string
 <code>\r</code>, then that pattern is handled specially and
 collapsed to the single character '\r'.  This is for use in removing
 \r's from test output.

 @author Christopher Brooks
 @deprecated This class is primarily used in models that will be code generated so that regular expressions are not needed in the output.  In general, use StringReplace with the regularExpression parameter set to false.
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating ret (cxh)
 @Pt.AcceptedRating red (cxh)
 */
@Deprecated
public class StringSimpleReplace extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringSimpleReplace(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create new parameters and ports.
        // Set default values of the parameters and type constraints.
        pattern = new PortParameter(this, "pattern");
        pattern.setStringMode(true);
        pattern.setExpression("");
        new SingletonParameter(pattern.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        replacement = new PortParameter(this, "replacement");
        replacement.setStringMode(true);
        replacement.setExpression("");
        new SingletonParameter(replacement.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        stringToEdit = new PortParameter(this, "stringToEdit");
        stringToEdit.setStringMode(true);
        stringToEdit.setExpression("");
        new SingletonParameter(stringToEdit.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pattern) {
            // We don't call super here because we don't
            // want to compile the pattern
            _patternValue = ((StringToken) pattern.getToken()).stringValue();
            if (_patternValue.equals("\\r")) {
                _patternValue = "\r";
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
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        replacement.update();
        stringToEdit.update();
        pattern.update();

        String replacementValue = ((StringToken) replacement.getToken())
                .stringValue();
        String stringToEditValue = ((StringToken) stringToEdit.getToken())
                .stringValue();

        String outputString;

        outputString = stringToEditValue.replace(_patternValue,
                replacementValue);

        output.send(0, new StringToken(outputString));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The replacement string.
    private String _patternValue;
}
