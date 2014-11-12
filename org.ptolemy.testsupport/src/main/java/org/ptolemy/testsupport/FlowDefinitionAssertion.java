/* A builder class to specify expected elements of a Flow definition.
 Copyright (c) 2014 The Regents of the University of California; iSencia Belgium NV.
 
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
package org.ptolemy.testsupport;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A builder class to specify expected elements of a Flow definition :
 * <ul>
 * <li>directors, actors, parameters and other NamedObj instances : define expected presence by name or by instance</li>
 * <li>parameters : define expected value</li>
 * <li>relations : define expected presence between ports</li>
 * </ul>
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 10.1
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class FlowDefinitionAssertion {

  /**
   * Clear all test result expectations.
   * <p>
   * New expectations can be defined again by repeatedly invoking the methods <code>expectActor...</code> etc.
   * </p>
   * 
   * @return this FlowDefinitionAssertion instance to allow fluent method chaining
   */
  public FlowDefinitionAssertion clear() {
    _expectedActorNames.clear();
    _expectedParameterNames.clear();
    _expectedRelations.clear();
    return this;
  }

  /**
   * Assert all configured expectations on the given flow. The assertions are done using JUnit's
   * <code>Assert.assert...()</code> methods, so any discovered deviation will result in a JUnit test failure.
   * <p>
   * If all expectations are ok, further tests can be chained through the returned reference to this
   * <code>FlowDefinitionAssertion</code> instance.
   * </p>
   * 
   * @param flow
   *          the flow that has been executed and for which test result expectations must be asserted.
   * @return this FlowDefinitionAssertion instance to allow fluent method chaining
   */
  public FlowDefinitionAssertion assertFlow(CompositeActor flow) {
    _assertActorNames(flow, _expectedActorNames);
    _assertParameterNames(flow, _expectedParameterNames);
    _assertRelations(flow, _expectedRelations);
    return this;
  }

  /**
   * 
   * @param actorName
   *          the NamedObj.getFullName() of the actor
   * @return this FlowDefinitionAssertion instance to allow fluent method chaining
   */
  public FlowDefinitionAssertion expectActor(String actorName) {
    _expectedActorNames.add(actorName);
    return this;
  }

  /**
   * 
   * @param parameterName
   *          the NamedObj.getFullName() of the parameter
   * @return this FlowDefinitionAssertion instance to allow fluent method chaining
   */
  public FlowDefinitionAssertion expectParameter(String parameterName) {
    _expectedParameterNames.add(parameterName);
    return this;
  }

  /**
   * 
   * @param from
   *          the NamedObj.getFullName() of the output port that must be connected to the <b>to</b> port
   * @param to
   *          the NamedObj.getFullName() of the input port that must be connected to the <b>from</b> port
   * @return this FlowDefinitionAssertion instance to allow fluent method chaining
   */
  public FlowDefinitionAssertion expectRelation(String from, String to) {
    _expectedRelations.add(new Relation(from, to));
    return this;
  }

  // protected methods
  
  /**
   * Asserts whether all expected actors are present in the given flow, 
   * based on the given actor names.
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.
   * </p> 
   * @param flow
   * @param expectedActorNames
   */
  protected void _assertActorNames(CompositeActor flow, Collection<String> expectedActorNames) {
    for (String name : expectedActorNames) {
      Object actor = flow.getEntity(TestUtilities.getFullNameButWithoutModelName(flow, name));
      Assert.assertNotNull("No actor " + name + " found in flow " + flow.getFullName(), actor);
      Assert.assertTrue(name + " is not an Actor in flow " + flow.getFullName(), (actor instanceof Actor));
    }
  }

  /**
   * Asserts whether all expected parameters are present in the given flow, 
   * based on the given parameter names.
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.
   * </p> 
   * @param flow
   * @param expectedParameterNames
   */
  protected void _assertParameterNames(CompositeActor flow, Collection<String> expectedParameterNames) {
    for (String name : expectedParameterNames) {
      Object parameter = flow.getAttribute(TestUtilities.getFullNameButWithoutModelName(flow, name));
      Assert.assertNotNull("No parameter " + name + " found in flow " + flow.getFullName(), parameter);
      Assert.assertTrue(name + " is not an Attribute in flow " + flow.getFullName(), (parameter instanceof Attribute));
    }
  }

  /**
   * Asserts whether all expected relations are present in the given flow, 
   * based on the given {@link Relation}s, which are simple pairs of from- & to- port names.
   * <p>
   * The method checks for the presence of the from&to ports for a given Relation,
   * whether the ports are connected (i.e. the to-port is in the <code>sinkPortList()</code> of the from-port)
   * and that a matching <code>Receiver</code> is present in the from-port's <code>getRemoteReceivers()</code>.
   * </p> 
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.<br/>
   * </p> 
   * @param flow
   * @param expectedRelations
   */
  protected void _assertRelations(CompositeActor flow, Collection<Relation> expectedRelations) {
    for (Relation relation : expectedRelations) {
      TypedIOPort outputPort = (TypedIOPort) flow.getPort(TestUtilities.getFullNameButWithoutModelName(flow, relation.from));
      TypedIOPort inputPort = (TypedIOPort) flow.getPort(TestUtilities.getFullNameButWithoutModelName(flow, relation.to));
      Assert.assertNotNull("No port " + relation.from + " found in flow " + flow.getFullName(), outputPort);
      Assert.assertNotNull("No port " + relation.to + " found in flow " + flow.getFullName(), inputPort);
      Assert.assertTrue(relation.from + " not connected to " + relation.to + " in flow " + flow.getFullName(), outputPort.sinkPortList().contains(inputPort));
      boolean linkedViaReceiver = false;
      try {
        Receiver[][] remoteReceivers = outputPort.getRemoteReceivers();
        for (Receiver[] receivers : remoteReceivers) {
          for (Receiver receiver : receivers) {
            linkedViaReceiver = inputPort.equals(receiver.getContainer());
            if (linkedViaReceiver) {
              break;
            }
          }
          if (linkedViaReceiver) {
            break;
          }
        }
        Assert.assertTrue(relation.from + " not connected via a Receiver to " + relation.to + " in flow " + flow.getFullName(), linkedViaReceiver);
      } catch (IllegalActionException e) {
        Assert.fail("Error obtaining remote receivers " + e.getMessage());
      }
    }
  }

  // private things

  /**
   * simple container for storing named from-to relations
   */
  private static class Relation {
    String from;
    String to;

    public Relation(String from, String to) {
      this.from = from;
      this.to = to;
    }
  }

  private Collection<String> _expectedActorNames = new ArrayList<String>();
  private Collection<String> _expectedParameterNames = new ArrayList<String>();
  private Collection<Relation> _expectedRelations = new ArrayList<Relation>();
}
