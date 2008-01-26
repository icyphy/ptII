/* A subscriber that aggregates messages from multiple publishers.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.actor.Director;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SubscriptionAggregator

/**
 Aggregate data produced by multiple publishers.
 This is a generalization of the Subscriber (the base class)
 where the channel name is interpreted as a regular expression.
 Data produced by all publishers that publish on a channel name
 that matches the regular expression are aggregated using the
 operation given by the <i>operation</i> parameter.

 <p>Note that the {@link ptolemy.actor.lib.Subscriber#channel <i>channel</i>}
 parameter of the superclass is now a regular expression in this class.
 Thus, this class is usually slower than the superclass.  One thing
 to watch out for is using <code>.</code> instead of <code>\.</code>
 and <code>*</code> instead of <code>.+</code>.
 For example, <code>channel.foo.*</code> might be faster as
 <code>channel\.foo.+</code>.


 @author Edward A. Lee, Raymond A. Cardillo, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SubscriptionAggregator extends Subscriber {

    /** Construct a subscriber with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public SubscriptionAggregator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setMultiport(false);

        operation = new StringParameter(this, "operation");
        operation.addChoice("add");
        operation.addChoice("multiply");
        operation.setExpression("add");
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The operation used to aggregate the data produced by
     *  matching publishers. The choices are "add" and "multiply".
     *  Note that "multiply" is a poor choice if the data type
     *  has a non-commutative multiplication operation (e.g.
     *  matrix types) because the result will be nondeterministic.
     *  This is a string that defaults to "add".
     */
    public StringParameter operation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to record the operation choice.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == operation) {
            String newValue = operation.stringValue();
            if (newValue.equals("add")) {
                _addOperation = true;
            } else {
                _addOperation = false;
            }
        } else if (attribute == channel) {
            _channelPattern = Pattern.compile(channel.stringValue());
            super.attributeChanged(attribute);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Determine whether a channel name matches this subscriber.
     *  This class returns true if the specified string matches
     *  the value of the <i>channel</i> parameter interpreted
     *  as a regular expression.
     *  @param channelName A channel name.
     *  @return True if this subscriber subscribes to the specified channel.
     */
    protected boolean channelMatches(String channelName) {
        // It turns out that Java regex is computational intensive.
        // For an analysis, see http://swtch.com/~rsc/regexp/regexp1.html
        if (_channel != null && channelName != null) {
            if (_channelMatches.contains(channelName)) {
                // If we've already been asked about this channel and returned
                // true, then return true again.
                return true;
            }
            if (_channelDoesNotMatch.contains(channelName)) {
                // If we've already been asked about this channel and returned
                // false, then return false again.
                return false;
            }
            if (_channelPattern == null) {
                // We call channelMatches() many times, so cache the compiled
                // pattern.
                _channelPattern = Pattern.compile(_channel);
            }
            Matcher matcher = _channelPattern.matcher(channelName);
            if (matcher.matches()) {
                _channelMatches.add(channelName);
                return true;
            }
            // FIXME: this might end up consuming lots of space in large
            // graphs.  We could store the hash of the channelName
            // and if the hash is present, then do the comparison.
            _channelDoesNotMatch.add(channelName);
        }
        return false;
    }

    /** Read at most one input token from each input
     *  channel, add all the tokens, and send the result
     *  to the output.
     *  @exception IllegalActionException If there is no director, or
     *   if there is no input connection.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this,
                    "SubscriptionAggregator has no matching Publisher.");
        }
        Token result = null;
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                if (result == null) {
                    result = token;
                } else {
                    if (_addOperation) {
                        result = result.add(token);
                    } else {
                        result = result.multiply(token);
                    }
                }
            }
        }
        output.send(0, result);
    }

    //     public void preinitialize() throws IllegalActionException {
    //         super.preinitialize();
    //         String pattern = "([^\\\\])\\.";
    //         String replacement=  "$1\\\\.";
    //         String backslashed = channel.stringValue().replaceAll(pattern, replacement);
    //         System.out.println("SubscriptionAgg: old: "
    //                 + channel.stringValue()
    //                 + " " + backslashed);

    //         pattern = "\\*";
    //         replacement = ".+";
    //         String backslashed2 = backslashed.replaceAll(pattern, replacement);

    //         System.out.println("SubscriptionAgg: old: "
    //                 + channel.stringValue()
    //                 + " tmp: " + backslashed
    //                 + " new: " + backslashed2);

    //         channel.setExpression(backslashed2);
    //     }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Update the connection to the publishers, if there are any.
     *  @exception IllegalActionException If creating the link
     *   triggers an exception.
     */
    protected void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }
        // Unlink to all previous relations, if any.
        Iterator relations = _relations.iterator();
        while (relations.hasNext()) {
            Relation relation = (Relation) relations.next();
            input.unlink(relation);
        }
        _relations.clear();

        // Link to all matching publishers.
        Iterator publishers = _findPublishers().iterator();
        while (publishers.hasNext()) {
            Publisher publisher = (Publisher) publishers.next();
            if (publisher._relation == null) {
                if (!publisher._updatedLinks) {
                    // If we call Subscriber.preinitialize()
                    // before we call Publisher.preinitialize(),
                    // then the publisher will not have created
                    // its relation.
                    publisher._updateLinks();
                }
            }
            ComponentRelation relation = publisher._relation;
            if (!input.isLinked(relation)) {
                // The Publisher._updateLinks() may have already linked us.
                input.liberalLink(relation);
            }
            _relations.add(relation);
        }
        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
        _updatedLinks = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find matching publishers, if there are any.
     *  @return A list of publishers.
     */
    private List _findPublishers() {
        List result = new LinkedList();
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Publisher) {
                    if (channelMatches(((Publisher) actor)._channel)) {
                        result.add(actor);
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that the operation is "add" rather than "multiply". */
    private boolean _addOperation = true;

    /** Set of channel names that have already not been matched. */
    private Set _channelDoesNotMatch = new HashSet();

    /** Set of channel names that have already matched. */
    private Set _channelMatches = new HashSet();

    /** Regex Pattern for _channelName. */
    private Pattern _channelPattern;

    /** The list of relations used to link to subscribers. */
    private List _relations = new LinkedList();
}
