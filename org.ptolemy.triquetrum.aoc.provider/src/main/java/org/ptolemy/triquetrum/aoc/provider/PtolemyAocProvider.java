/* AOC class provider for Ptolemy in Triquetrum.

Copyright (c) 2017 The Regents of the University of California; iSencia Belgium NV.
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
package org.ptolemy.triquetrum.aoc.provider;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.FrameworkUtil;
import org.ptolemy.classloading.ActorOrientedClassProvider;
import org.ptolemy.commons.VersionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.RemoveGraphicalClasses;

/**
 * The Ptolemy AOC provider for Triquetrum, providing all Ptolemy AOCs that should be available in the
 * palette and/or for model execution.
 * <p>
 * Cfr <code>org.ptolemy.triquetrum.editor.palette</code> for palette contents, aligned to expectations
 * in Triquetrum. For the 2017 release, this only needs to provide the SineWave actor.
 * </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.1
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public class PtolemyAocProvider implements ActorOrientedClassProvider {
  private final static Logger LOGGER = LoggerFactory.getLogger(PtolemyAocProvider.class);
  
  /**
   * Checks if the given className is in the set of supported AOC class names and then returns the corresponding AOC.
   *  
   * The current implementation ignores the versionSpec.
   */
  @Override
  public CompositeEntity getActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    LOGGER.trace("getActorOrientedClass() - entry : aoc {} version {}", className, versionSpec);
    CompositeEntity result = null;
    try {
      if (aocSet.contains(className)) {
        result = parsedAOCs.get(className);
        if(result == null) {
          Workspace workspace = new Workspace("PtolemyAocProvider");
          MoMLParser parser = new MoMLParser(workspace, versionSpec, getClass().getClassLoader());
          MoMLParser.addMoMLFilters(parserFilters, workspace);
          URL aocPath = FrameworkUtil.getBundle(PtolemyAocProvider.class).getResource("/" + className.replace('.', '/') + ".xml");
          result = (CompositeEntity) parser.parse(null, aocPath);
          MoMLParser.setMoMLFilters(null, workspace);
          parsedAOCs.put(className, result);
        };
        return result;
      } else {
        throw new ClassNotFoundException(className);
      }
    } catch (Exception e) {
      LOGGER.error("Internal error ", e);
      throw new ClassNotFoundException(className);
    } finally {
      LOGGER.trace("getActorOrientedClass() - exit : aoc {} version {} : {}", className, versionSpec, result != null ? "found " + result : "not found");
    }
  }

  // private stuff

  /**
   * A list of filters that must be applied to filter out Moml contents that are not compatible with Triquetrum and/or contents of the current
   * set of Ptolemy II OSGi bundles.
   */
  private static final List<MoMLFilter> parserFilters = new ArrayList<>();

  /**
   * The set of AOC class names that can be provided by this AOC provider.
   */
  private static final Set<String> aocSet = new HashSet<>();
  
  /**
   * Parsed AOCs, mapped to their AOC class name.
   */
  private static final Map<String, CompositeEntity> parsedAOCs = new ConcurrentHashMap<>();

  static {
    parserFilters.add(new RemoveGraphicalClasses());
    aocSet.add("ptolemy.actor.lib.Sinewave");
  }
}
