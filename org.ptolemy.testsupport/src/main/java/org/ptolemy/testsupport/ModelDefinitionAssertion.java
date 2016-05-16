/* A builder class to specify expected elements of a defined model.
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
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A builder class to specify expected elements of a defined model :
 * <ul>
 * <li>directors, actors, parameters and other NamedObj instances : define expected presence by name or by instance</li>
 * <li>parameters : define expected value</li>
 * <li>relations : define expected presence between ports</li>
 * </ul>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class ModelDefinitionAssertion {

  /**
   * Clear all test result expectations.
   * <p>
   * New expectations can be defined again by repeatedly invoking the methods <code>expectActor...</code> etc.
   * </p>
   *
   * @return this ModelDefinitionAssertion instance to allow fluent method chaining
   */
  public ModelDefinitionAssertion clear() {
    _expectedActorNames.clear();
    _expectedParameterNames.clear();
    _expectedLinks.clear();
    return this;
  }

  /**
   * Assert all configured expectations on the given model. The assertions are done using JUnit's
   * <code>Assert.assert...()</code> methods, so any discovered deviation will result in a JUnit test failure.
   * <p>
   * If all expectations are ok, further tests can be chained through the returned reference to this
   * <code>ModelDefinitionAssertion</code> instance.
   * </p>
   *
   * @param model
   *          the model for which expected contents must be asserted.
   * @return this ModelDefinitionAssertion instance to allow fluent method chaining
   */
  public ModelDefinitionAssertion assertModel(CompositeActor model) {
    _assertActorNames(model, _expectedActorNames);
    _assertParameterNames(model, _expectedParameterNames);
    _assertLinks(model, _expectedLinks);
    return this;
  }

  /**
   *
   * @param actorName
   *          the NamedObj.getFullName() of the actor
   * @return this ModelDefinitionAssertion instance to allow fluent method chaining
   */
  public ModelDefinitionAssertion expectActor(String actorName) {
    _expectedActorNames.add(actorName);
    return this;
  }

  /**
   *
   * @param parameterName
   *          the NamedObj.getFullName() of the parameter
   * @return this ModelDefinitionAssertion instance to allow fluent method chaining
   */
  public ModelDefinitionAssertion expectParameter(String parameterName) {
    _expectedParameterNames.add(parameterName);
    return this;
  }

  /**
   *
   * @param from
   *          the NamedObj.getFullName() of the relation or output port that must be connected to the <b>to</b> port or relation
   * @param to
   *          the NamedObj.getFullName() of the relation or input port that must be connected to the <b>from</b> port or relation
   * @return this ModelDefinitionAssertion instance to allow fluent method chaining
   */
  public ModelDefinitionAssertion expectLink(String from, String to) {
    _expectedLinks.add(new Link(from, to));
    return this;
  }

  // protected methods

  /**
   * Asserts whether all expected actors are present in the given model,
   * based on the given actor names.
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.
   * </p>
   * @param model
   * @param expectedActorNames
   */
  protected void _assertActorNames(CompositeActor model, Collection<String> expectedActorNames) {
    for (String name : expectedActorNames) {
      Object actor = model.getEntity(TestUtilities.getFullNameButWithoutModelName(model, name));
      Assert.assertNotNull("No actor " + name + " found in model " + model.getFullName(), actor);
      Assert.assertTrue(name + " is not an Actor in model " + model.getFullName(), (actor instanceof Actor));
    }
  }

  /**
   * Asserts whether all expected parameters are present in the given model,
   * based on the given parameter names.
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.
   * </p>
   * @param model
   * @param expectedParameterNames
   */
  protected void _assertParameterNames(CompositeActor model, Collection<String> expectedParameterNames) {
    for (String name : expectedParameterNames) {
      Object parameter = model.getAttribute(TestUtilities.getFullNameButWithoutModelName(model, name));
      Assert.assertNotNull("No parameter " + name + " found in model " + model.getFullName(), parameter);
      Assert.assertTrue(name + " is not an Attribute in model " + model.getFullName(), (parameter instanceof Attribute));
    }
  }

  /**
   * Asserts whether all expected links are present in the given model,
   * based on the given {@link Link}s, which are simple pairs of from- & to- port or relation names.
   * <p>
   * The implementation is based on JUnit's {@link Assert} methods.<br/>
   * </p>
   * @param model
   * @param expectedLinks
   */
  protected void _assertLinks(CompositeActor model, Collection<Link> expectedLinks) {
    for (Link link : expectedLinks) {
      String fromName = TestUtilities.getFullNameButWithoutModelName(model, link.from);
      TypedIOPort fromPort = (TypedIOPort) model.getPort(fromName);
      TypedIORelation fromRelation = (TypedIORelation) model.getRelation(fromName);

      String toName = TestUtilities.getFullNameButWithoutModelName(model, link.to);
      TypedIOPort toPort = (TypedIOPort) model.getPort(toName);
      TypedIORelation toRelation = (TypedIORelation) model.getRelation(toName);

      Assert.assertFalse("No port or relation " + link.from + " found in model " + model.getFullName(), fromPort == null && fromRelation == null);
      Assert.assertFalse("No port or relation " + link.to + " found in model " + model.getFullName(), toPort == null && toRelation == null);

      if (fromPort != null) {
        if (toPort != null) {
          _assertPortToPortLink(model, link, fromPort, toPort);
        } else {
          _assertPortToRelationLink(model, link, fromPort, toRelation);
        }
      } else {
        if (toPort != null) {
          _assertRelationToPortLink(model, link, fromRelation, toPort);
        } else {
          _assertRelationToRelationLink(model, link, fromRelation, toRelation);
        }
      }
    }
  }

  // private things

  private void _assertPortToPortLink(CompositeActor model, Link link, TypedIOPort fromPort, TypedIOPort toPort) {
    Assert.assertTrue(link.from + " not connected to " + link.to + " in model " + model.getFullName(), fromPort.sinkPortList().contains(toPort));
    boolean linkedViaReceiver = false;
    try {
      Receiver[][] remoteReceivers = fromPort.getRemoteReceivers();
      for (Receiver[] receivers : remoteReceivers) {
        for (Receiver receiver : receivers) {
          linkedViaReceiver = toPort.equals(receiver.getContainer());
          if (linkedViaReceiver) {
            break;
          }
        }
        if (linkedViaReceiver) {
          break;
        }
      }
      Assert.assertTrue(link.from + " not connected via a Receiver to " + link.to + " in model " + model.getFullName(), linkedViaReceiver);
    } catch (IllegalActionException e) {
      Assert.fail("Error obtaining remote receivers " + e.getMessage());
    }
  }

  private void _assertPortToRelationLink(CompositeActor model, Link link, TypedIOPort fromPort, TypedIORelation toRelation) {
    Assert.assertTrue(link.from + " not connected to " + link.to + " in model " + model.getFullName(), toRelation.linkedSourcePortList().contains(fromPort));
  }

  private void _assertRelationToPortLink(CompositeActor model, Link link, TypedIORelation fromRelation, TypedIOPort toPort) {
    Assert.assertTrue(link.from + " not connected to " + link.to + " in model " + model.getFullName(), fromRelation.linkedDestinationPortList().contains(toPort));
  }

  private void _assertRelationToRelationLink(CompositeActor model, Link link, TypedIORelation fromRelation, TypedIORelation toRelation) {
    Assert.assertTrue(link.from + " not connected to " + link.to + " in model " + model.getFullName(), toRelation.linkedObjectsList().contains(fromRelation));
  }

  /**
   * simple container for storing named from-to links
   */
  private static class Link {
    String from;
    String to;

    public Link(String from, String to) {
      this.from = from;
      this.to = to;
    }
  }

  private Collection<String> _expectedActorNames = new ArrayList<String>();
  private Collection<String> _expectedParameterNames = new ArrayList<String>();
  private Collection<Link> _expectedLinks = new ArrayList<Link>();
}
