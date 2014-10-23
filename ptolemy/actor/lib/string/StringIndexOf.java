/* Finds index of a string contained in a given text

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

import java.util.Locale;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// StringIndexOf

/**
 Output the index of a <i>searchFor</i> string contained in a given
 <i>inText</i>.  The search begins at the index given by
 <i>startIndex</i> and ends at either the end or the beginning of the
 string, depending on whether <i>searchForwards</i> is true or false,
 respectively. If the string is not found, then the output is -1.  If
 the string is found, then the output is the index of the start of the
 string, where index 0 refers to the first character in the string.

 @author Rakesh Reddy, Philip Baldwin, Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (pjb2e)
 @Pt.AcceptedRating Green (eal)
 */
public class StringIndexOf extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringIndexOf(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        searchFor = new PortParameter(this, "searchFor");
        searchFor.setStringMode(true);
        searchFor.setExpression("");
        new SingletonParameter(searchFor.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        inText = new PortParameter(this, "inText");
        inText.setStringMode(true);
        inText.setExpression("");
        new SingletonParameter(inText.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        ignoreCase = new Parameter(this, "ignoreCase");
        ignoreCase.setTypeEquals(BaseType.BOOLEAN);
        ignoreCase.setToken(new BooleanToken(false));

        startIndex = new PortParameter(this, "startIndex");
        startIndex.setTypeEquals(BaseType.INT);
        startIndex.setExpression("0");
        new SingletonParameter(startIndex.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        // Create new parameters and ports, then set default values and/or
        // types of parameters and ports.
        searchForwards = new Parameter(this, "searchForwards");
        searchForwards.setTypeEquals(BaseType.BOOLEAN);
        searchForwards.setExpression("true");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter to state whether to ignore case. This is a
     *  boolean that defaults to false.
     */
    public Parameter ignoreCase;

    /** Port and parameter specifying the string that will be searched.
     *  This has type string and defaults to the empty string.
     */
    public PortParameter inText;

    /** Output producing the index of the <i>searchFor</i> string, if
     *  it is found, and -1 otherwise. This has type int.
     */
    public TypedIOPort output;

    /** Port and parameter specifying a string to find in
     *  the <i>inText</i> string.  This has type string and
     *  defaults to an empty string.
     */
    public PortParameter searchFor;

    /** Boolean parameter indicating the direction in which to search.
     *  This is true to find the first occurrence and false to find the last.
     *  The default value is true.
     */
    public Parameter searchForwards;

    /** Port and parameter that determines where to start the search.
     *  This has type int and defaults to 0.
     */
    public PortParameter startIndex;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the specified search and output either -1 (the string is not
     *  found) or the index of the string that is found.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        searchFor.update();
        inText.update();
        startIndex.update();

        String searchForString = ((StringToken) searchFor.getToken())
                .stringValue();
        String inTextString = ((StringToken) inText.getToken()).stringValue();
        int startIndexValue = ((IntToken) startIndex.getToken()).intValue();
        boolean forwards = ((BooleanToken) searchForwards.getToken())
                .booleanValue();

        if (((BooleanToken) ignoreCase.getToken()).booleanValue()) {
            searchForString = searchForString.toLowerCase(Locale.getDefault());
            inTextString = inTextString.toLowerCase(Locale.getDefault());
        }

        int returnValue;

        if (forwards) {
            returnValue = inTextString
                    .indexOf(searchForString, startIndexValue);
        } else {
            returnValue = inTextString.lastIndexOf(searchForString,
                    startIndexValue);
        }

        output.send(0, new IntToken(returnValue));
    }
}
