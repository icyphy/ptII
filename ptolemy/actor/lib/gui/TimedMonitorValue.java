/* Monitor input values and include the model time of the director.

 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import ptolemy.actor.Director;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TimedMonitorValue

/**
 Monitor inputs by setting the <i>value</i> parameter equal
 to each arriving token.  This actor can be used with
 an icon that displays the value of a parameter to get
 on-screen display of values in a diagram. The value is
 updated only in postfire.

 <p>Note that the icon for this actor is defined in
 <code>ptolemy/actor/lib/timedsinks.xml</code>, which looks something
 like
 <pre>
&lt;entity name="TimedMonitorValue" class="ptolemy.actor.lib.TimedMonitorValue"&gt;
&lt;doc&gt;Monitor and display values&lt;/doc&gt;
   &lt;property name="displayWidth" class="ptolemy.data.expr.Parameter" value="20"/&gt;
   &lt;property name="_icon" class="ptolemy.vergil.icon.UpdatedValueIcon"&gt;
      &lt;property name="attributeName" value="value"/&gt;
      &lt;property name="displayWidth" value="displayWidth"/&gt;
   &lt;/property&gt;
&lt;/entity&gt;
 </pre>
 @author Christopher Brooks
 @version $Id: MonitorValue.java 70398 2014-10-22 23:44:32Z cxh $
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TimedMonitorValue extends MonitorValue {
    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedMonitorValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a token from the named input channel.
     *  This is a protected method to allow subclasses to override
     *  how inputs are observed.
     *  @param i The channel
     *  @return A token from the input channel or null if there is
     *   nothing to display.
     *  @exception IllegalActionException If reading the input fails.
     */
    protected Token _getInputToken(int i) throws IllegalActionException {
        if (input.hasToken(i)) {
            Token token = input.get(i);
            Director director = getDirector();
            if (director != null) {
                return new StringToken(director.getModelTime() + ": " + token);
            } else {
                return token;
            }
        }
        return null;
    }
}
