/* Pattern match a string to a regular expression.

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
package ptolemy.actor.lib.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// StringMatches

/**
 Pattern match a string to a regular expression and output a true if it
 matches and a false if it does not. For a reference on regular
 expression syntax see:
 <a href="http://download.oracle.com/javase/tutorial/essential/regex/#in_browser">
 http://download.oracle.com/javase/tutorial/essential/regex/</a>.

 @author Antonio Yordan-Nones, Colin Cochran, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (djstone)
 @Pt.AcceptedRating Green (eal)
 */
public class StringMatches extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, this constructs
     *  the <i>pattern</i> and <i>matchString</i> PortParameters,
     *  and the <i>output</i> port.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringMatches(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create one matchString portParameter, one matchString port,
        // and one output port.
        pattern = new PortParameter(this, "pattern");
        pattern.setStringMode(true);
        pattern.setExpression("");
        new SingletonParameter(pattern.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        matchString = new PortParameter(this, "matchString");
        matchString.setStringMode(true);
        matchString.setExpression("");
        new SingletonParameter(matchString.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The string to be pattern matched to the regular expression.
     */
    public PortParameter matchString;

    /** Output is true if the pattern exists in the string, false otherwise.
     *  Its type is boolean.
     */
    public TypedIOPort output;

    /** The regular expression to be pattern matched with the
     *  matchString string.
     *  Its default parameter is an empty string that matches no strings.
     */
    public PortParameter pattern;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to compile the regular expression if
     *  it has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the pattern cannot be compiled
     *  into a regular expression.
     */
    @Override
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

    /** Clone the actor into the specified workspace. This calls the
     *  base class and initializes private variables.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StringMatches newObject = (StringMatches) super.clone(workspace);
        String patternValue = null;
        try {
            patternValue = ((StringToken) newObject.pattern.getToken())
                    .stringValue();
            newObject._pattern = Pattern.compile(patternValue);
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to compile regular expression \"" + patternValue
                    + "\"");
        }
        return newObject;
    }

    /** Pattern match a regular expression against a supplied
     *  matchString and output a true if they match and a false
     *  otherwise.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        pattern.update();
        matchString.update();

        String matchStringValue = ((StringToken) matchString.getToken())
                .stringValue();
        Matcher match = _pattern.matcher(matchStringValue);
        output.send(0, new BooleanToken(match.find()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The pattern for the regular expression.
    private Pattern _pattern;
}
