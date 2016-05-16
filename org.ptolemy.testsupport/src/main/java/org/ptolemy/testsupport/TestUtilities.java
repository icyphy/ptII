/* A collection of utility methods to support unit testing.

Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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

import java.util.List;

import org.ptolemy.testsupport.statistics.ActorStatistics;
import org.ptolemy.testsupport.statistics.NamedStatistics;
import org.ptolemy.testsupport.statistics.PortStatistics;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * This class contains a set of utility methods for using execution statistics
 * that can be asserted in unit tests.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class TestUtilities {

  /**
   * Gets the full name but without leading model name.
   *
   * @param model the model
   * @param fullName the full name of a model element
   * @return the full name but without model name
   */
  public static String getFullNameButWithoutModelName(final CompositeActor model, String fullName) {
    // we need to obtain the full name of the NamedObj,
    // but without the model name in it
    if (model == null || model.getName() == null) {
      return fullName;
    }
    final int i = fullName.indexOf(model.getName());
    if (i > 0) {
      // there's always an extra '.' in front of the model name...
      // and a trailing '.' just behind it...
      fullName = fullName.substring(i + model.getName().length() + 1);
    }
    return fullName;
  }

  /**
   * Get the execution statistics for the given actor.
   *
   * @param actor the actor
   * @return the statistics for the actor
   */
  public static ActorStatistics getStatistics(ComponentEntity<?> actor) {
    List<StatisticsAttribute> attrs = actor.attributeList(StatisticsAttribute.class);
    if (attrs.isEmpty()) {
      return null;
    } else {
      return (ActorStatistics) attrs.get(0).getStatistics();
    }
  }

  /**
   * Get the execution statistics for the given Port.
   *
   * @param p the port
   * @return the statistics for the port
   */
  public static PortStatistics getStatistics(Port p) {
    List<StatisticsAttribute> attrs = p.attributeList(StatisticsAttribute.class);
    if (attrs.isEmpty()) {
      return null;
    } else {
      return (PortStatistics) attrs.get(0).getStatistics();
    }
  }

  /**
   * Enriches the model elements with actor and port statistics containers, to allow post-execution test assertions.
   * This adds attributes in-place in the given model, and returns it as well to allow a more fluent usage in testing scripts.
   *
   * @param model the model
   * @return the (enriched) model
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public static CompositeEntity enableStatistics(CompositeEntity model) throws IllegalActionException, NameDuplicationException {
    for (Object entity : model.entityList()) {
      if (entity instanceof ComponentEntity) {
        _enableStatistics((ComponentEntity<?>) entity);
      }
    }
    return model;
  }

  // private elements

  /**
   * Enable gathering execution statistics on the given model entity and its sub-components.
   * <p>
   * This is implemented by registering an {@link Attribute} that binds a {@link NamedStatistics} instance
   * to each relevant contained model element.
   * </p>
   *
   * @param entity the entity for which execution statistics must be enabled
   * @throws NameDuplicationException i.c.o. a failure caused by duplicate attribute names on some model element
   * @throws IllegalActionException i.c.o. some other error while enabling statistics gathering
   */
  private static void _enableStatistics(ComponentEntity<?> entity) throws IllegalActionException, NameDuplicationException {
    if (entity instanceof AtomicActor<?>) {
      ActorStatistics actorStats = new ActorStatistics(entity);
      ((AtomicActor<?>)entity).addActorFiringListener(actorStats);
      new StatisticsAttribute(entity, actorStats);
      for (Object p : entity.portList()) {
        if (p instanceof IOPort) {
          IOPort ioP = (IOPort) p;
          PortStatistics portStats = new PortStatistics(ioP);
          ioP.addIOPortEventListener(portStats);
          new StatisticsAttribute(ioP, portStats);
        }
      }
    }
    if (entity instanceof CompositeEntity) {
      enableStatistics((CompositeEntity) entity);
    }
  }

  // TODO check if it is worth the overhead to introduce a separate Attribute impl for the statistics-2-element binding
  // or if it's not sufficient/easier to just make the ActorStatistics and PortStatistics attributes themselves
  /**
   * Instances of this class provide a binding between a statistics object and the associated model element.
   */
  private static class StatisticsAttribute extends Attribute {

    /**
     * Instantiates a new statistics attribute.
     * See {@link Attribute#Attribute(NamedObj, String)} for details about generated exceptions
     *
     * @param container the model element for which execution statistics must be maintained
     * @param stats the statistics instance
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public StatisticsAttribute(NamedObj container, NamedStatistics stats) throws IllegalActionException, NameDuplicationException {
      super(container, "stats");
      this._statistics = stats;
    }

    /**
     * Get the statistics instance associated with the container of this Attribute.
     *
     * @return the statistics
     */
    public NamedStatistics getStatistics() {
      return _statistics;
    }

    // private properties
    private NamedStatistics _statistics;
  }
}
