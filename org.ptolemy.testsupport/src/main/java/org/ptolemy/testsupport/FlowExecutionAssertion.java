/* This is a simple "builder" class to specify and assert flow execution expectations.
 Copyright (c) 2014 The Regents of the University of California.
 
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;

/**
 * This is a simple "builder" class to specify and assert flow execution expectations :
 * <ul>
 * <li>Number of messages that have been received by input ports</li>
 * <li>Number of messages that have been sent by output ports</li>
 * <li>Number of fire/process iterations done by actors</li>
 * </ul>
 * <p>
 * The ports and actors are specified by their full name, optionally including the root model name.<br/>
 * E.g. the input port of the <code>Console</code> actor can be named <code>"Console.input"</code> or, when used within a known flow called
 * <code>"HelloWorld"</code>, it can be named as <code>".HelloWorld.Console.input"</code>, which is the result of the method <code>Port.getFullName()</code>.
 * </p>
 * If a port or an actor is inside a submodel in a hierarchic model/flow, its hierarchic path must always be specified. E.g. for a <code>Console</code> actor
 * inside a submodel <code>"SayHello"</code> of the model <code>"HelloWorld"</code>, the actor can be referred to as <code>"SayHello.Console"</code> or
 * <code>".HelloWorld.SayHello.Console"</code>, but not simply as <code>"Console"</code>.
 * <p>
 * Same goes for the actors.
 * </p>
 * The initial "." is typical for a full name of a Passerelle/Ptolemy model element, as obtained from that method call.
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 10.1
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (?)
 */
public class FlowExecutionAssertion {
  /**
   * The default constructor creates an instance without initial test outcome expectations. 
   * 
   * The expectations should all be defined by repeatedly invoking the
   * methods <code>expectMsg...</code> and <code>expectActor...</code>
   */
  public FlowExecutionAssertion() {
  }

  /**
   * This constructor creates an instance with some initial test outcome expectations, specified in <code>Maps</code>.
   * <p>
   * Further expectations can still be defined by repeatedly invoking the methods <code>expectMsg...</code> and <code>expectActor...</code>
   * </p>
   * 
   * @param portMsgReceiptCounts
   * @param portMsgSentCounts
   * @param actorIterationCounts
   */
  public FlowExecutionAssertion(Map<String, Long> portMsgReceiptCounts, Map<String, Long> portMsgSentCounts, Map<String, Long> actorIterationCounts) {
    this._portMsgReceiptCounts = portMsgReceiptCounts;
    this._portMsgSentCounts = portMsgSentCounts;
    this._actorIterationCounts = actorIterationCounts;
  }

  /**
   * Clear all test result expectations.
   * <p>
   * New expectations can be defined again by repeatedly invoking the methods <code>expectMsg...</code> and <code>expectActor...</code>
   * </p>
   * 
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion clear() {
    _portMsgReceiptCounts.clear();
    _portMsgSentCounts.clear();
    _actorIterationCounts.clear();
    return this;
  }

  /**
   * Assert all configured expectations on the given flow. The assertions are done using JUnit's <code>Assert.assert...()</code> methods, so any discovered
   * deviation will result in a JUnit test failure.
   * <p>
   * If all expectations are ok, further tests can be chained through the returned reference to this <code>FlowStatisticsAssertion</code> instance.
   * </p>
   * 
   * @param flow
   *          the flow that has been executed and for which test result expectations must be asserted.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion assertFlow(CompositeActor flow) {
    assertPortReceiptStatistics(flow, _portMsgReceiptCounts);
    assertPortSentStatistics(flow, _portMsgSentCounts);
    assertActorIterationStatistics(flow, _actorIterationCounts);
    return this;
  }

  /**
   * Add an expected count for messages received on the default input port of the given actor.
   * <p>
   * When the actor doesn't have an input port named "input", an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param expectedCount the expected count for messages received on the default input port of the given actor.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgReceiptCount(ComponentEntity<?> actor, Long expectedCount) {
    return expectMsgReceiptCount(actor, "input", expectedCount);
  }

  /**
   * Add an expected count for messages sent on the default output port of the given actor.
   * <p>
   * When the actor doesn't have an output port named "output", an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param expectedCount the expected count for messages sent on the default output port of the given actor.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgSentCount(ComponentEntity<?> actor, Long expectedCount) {
    return expectMsgSentCount(actor, "output", expectedCount);
  }

  /**
   * Add an expected count for messages received on the given port.
   * <p>
   * When the port is not an input port, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount the expected count for messages received on the given port.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgReceiptCount(IOPort port, Long expectedCount) {
    if (port == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      if (!port.isInput()) {
        throw new IllegalArgumentException("Port " + port.getFullName() + " is not an input port.");
      } else {
        String portName = port.getFullName().split("\\.", 3)[2];
        return expectMsgReceiptCount(portName, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages sent on the given port.
   * <p>
   * When the port is not an output port, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount the expected count for messages sent on the given port.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgSentCount(IOPort port, Long expectedCount) {
    if (port == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      if (!port.isOutput()) {
        throw new IllegalArgumentException("Port " + port.getFullName() + " is not an output port.");
      } else {
        String portName = port.getFullName().split("\\.", 3)[2];
        return expectMsgSentCount(portName, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages received on the given named input port of the given actor.
   * <p>
   * When the actor doesn't have a port with the given name, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param portName
   *          should be a simple name of the port of the given actor. I.e. not a full hierarchic name in the sense as described in the class doc, but e.g.
   *          plainly <code>"input"</code>.
   * @param expectedCount the expected count for messages received on the given named input port of the given actor.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgReceiptCount(ComponentEntity<?> actor, String portName, Long expectedCount) {
    if (actor == null || portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      IOPort p = (IOPort) actor.getPort(portName);
      if (p == null) {
        throw new IllegalArgumentException("Port " + portName + " does not exist for actor " + actor.getFullName());
      } else if (!p.isInput()) {
        throw new IllegalArgumentException("Port " + portName + " is not an input port.");
      } else {
        return expectMsgReceiptCount(p, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages sent on the given named output port of the given actor.
   * <p>
   * When the actor doesn't have a port with the given name, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param portName
   *          should be a simple name of the port of the given actor. I.e. not a full hierarchic name in the sense as described in the class doc, but e.g.
   *          plainly <code>"output"</code>.
   * @param expectedCount the expected count for messages sent on the given named output port of the given actor.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgSentCount(ComponentEntity<?> actor, String portName, Long expectedCount) {
    if (actor == null || portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      IOPort p = (IOPort) actor.getPort(portName);
      if (p == null) {
        throw new IllegalArgumentException("Port " + portName + " does not exist for actor " + actor.getFullName());
      } else if (!p.isOutput()) {
        throw new IllegalArgumentException("Port " + portName + " is not an output port.");
      } else {
        return expectMsgSentCount(p, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages received on the port with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param portName
   *          check the class doc for port naming options
   * @param expectedCount the expected count for messages received on the port with given name.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgReceiptCount(String portName, Long expectedCount) {
    if (portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      _portMsgReceiptCounts.put(portName, expectedCount);
      return this;
    }
  }

  /**
   * Add an expected count for messages sent on the port with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param portName
   *          check the class doc for port naming options
   * @param expectedCount the expected count for messages sent on the port with given name.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectMsgSentCount(String portName, Long expectedCount) {
    if (portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      _portMsgSentCounts.put(portName, expectedCount);
      return this;
    }
  }

  /**
   * Add an expected count for fire/process iterations for the given actor.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount the expected count for fire/process iterations for the given actor.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectActorIterationCount(ComponentEntity<?> actor, Long expectedCount) {
    if (actor == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      String actorName = actor.getFullName().split("\\.", 3)[2];
      return expectActorIterationCount(actorName, expectedCount);
    }
  }

  /**
   * Add an expected count for fire/process iterations for the actor with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actorName
   *          check the class doc for actor naming options
   * @param expectedCount the expected count for fire/process iterations for the actor with given name.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowExecutionAssertion expectActorIterationCount(String actorName, Long expectedCount) {
    if (actorName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      _actorIterationCounts.put(actorName, expectedCount);
      return this;
    }
  }

  /**
   * Assert all specified expectations for message receipts on input ports, applied on the given flow instance.
   * 
   * @param flow
   * @param receivedCounts a map with full portNames and their expected counts of received messages
   */
  protected void assertPortReceiptStatistics(CompositeActor flow, Map<String, Long> receivedCounts) {
    if (receivedCounts != null) {
      for (Entry<String, Long> rcvCountEntry : receivedCounts.entrySet()) {
        String portName = rcvCountEntry.getKey();
        long expCount = rcvCountEntry.getValue();
        if (portName.startsWith("." + flow.getName())) {
          // chop flow name
          portName = portName.split("\\.", 3)[2];
        }
        IOPort p = (IOPort) flow.getPort(portName);
        assertNotNull("No port " + portName + " found in flow " + flow.getName(), p);
        assertTrue("Port " + portName + " is not an input port.", p.isInput());
        assertEquals("Wrong received count for port " + portName, expCount, TestUtils.getStatistics(p).getNrReceivedMessages());
      }
    }
  }

  /**
   * Assert all specified expectations for message sent by output ports, applied on the given flow instance.
   * 
   * @param flow
   * @param sentCounts a map with full portNames and their expected counts of sent messages
   */
  protected void assertPortSentStatistics(CompositeActor flow, Map<String, Long> sentCounts) {
    if (sentCounts != null) {
      for (Entry<String, Long> rcvCountEntry : sentCounts.entrySet()) {
        String portName = rcvCountEntry.getKey();
        long expCount = rcvCountEntry.getValue();
        if (portName.startsWith("." + flow.getName())) {
          // chop flow name
          portName = portName.split("\\.", 3)[2];
        }
        IOPort p = (IOPort) flow.getPort(portName);
        assertNotNull("No port " + portName + " found in flow " + flow.getName(), p);
        assertTrue("Port " + portName + " is not an output port.", p.isOutput());
        assertEquals("Wrong sent count for port " + portName, expCount, TestUtils.getStatistics(p).getNrSentMessages());
      }
    }
  }

  /**
   * Assert all specified expectations for actor iteration counts, applied on the given flow instance.
   * 
   * @param flow
   * @param iterationCounts a map with full actorNames and their expected iteration counts
   */
  protected void assertActorIterationStatistics(CompositeActor flow, Map<String, Long> iterationCounts) {
    if (iterationCounts != null) {
      for (Entry<String, Long> itrCountEntry : iterationCounts.entrySet()) {
        String actorName = itrCountEntry.getKey();
        long expCount = itrCountEntry.getValue();
        if (actorName.startsWith("." + flow.getName())) {
          // chop flow name
          actorName = actorName.split("\\.", 3)[2];
        }
        ComponentEntity<?> a = flow.getEntity(actorName);
        assertNotNull("No actor " + actorName + " found in flow " + flow.getName(), a);
        if (expCount != 0) {
          assertNotNull("Actor statistics not enabled for actor " + actorName, TestUtils.getStatistics(a));
          assertEquals("Wrong iteration count for actor " + actorName, expCount, TestUtils.getStatistics(a).getNrCycles());
        } else {
          // in some test cases, actors may not even have been initialized if we look for 0 iterations
          if (TestUtils.getStatistics(a) != null) {
            assertEquals("Wrong iteration count for actor " + actorName, expCount, TestUtils.getStatistics(a).getNrCycles());
          }
        }
      }
    }
  }
  
  // private properties
  

  /**
   * Maintains the expected counts of received messages for input ports. Ports are specified by their name. Please check the class doc for info about naming
   * options.
   */
  private Map<String, Long> _portMsgReceiptCounts = new HashMap<String, Long>();

  /**
   * Maintains the expected counts of sent messages for output ports. Ports are specified by their name. Please check the class doc for info about naming
   * options.
   */
  private Map<String, Long> _portMsgSentCounts = new HashMap<String, Long>();

  /**
   * Maintains the expected iteration counts for actors. Actors are specified by their name. Please check the class doc for info about naming options.
   */
  private Map<String, Long> _actorIterationCounts = new HashMap<String, Long>();
}
