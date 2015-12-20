/* A contract for dynamic providers of actor-oriented classes.

Copyright (c) 2015 The Regents of the University of California.
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

package org.ptolemy.classloading;

import org.ptolemy.commons.VersionSpecification;

import ptolemy.kernel.CompositeEntity;

/**
 * A contract for dynamic providers of actor-oriented classes, i.e. reusable assemblies of model elements.
 * <p>
 * These providers could obtain such assemblies from moml class definitions in files, databases or other storage systems,
 * or defined in any relevant manner.
 * </p>
 * <p>
 * Implementations should typically be injected in a ClassLoadingStrategy.
 * Currently the OSGiClassLoadingStrategy is the main injection target.
 * </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public interface ActorOrientedClassProvider {

  /**
   * Returns the <code>CompositeEntity</code> providing the requested actor-oriented class definition, if this provider has it.
   * <p>
   * An actor-oriented class can have only a "simple" name, i.e. no dots, as the underlying <code>NamedObj</code> does not allow dot-separated names.
   * However, provider implementations may allow <code>className</code> values with dots.
   * Remark that once loaded in the Ptolemy runtime, all actor-oriented classes are managed by their "simple" name only!
   * So, when using dot-separated "nested" names, the final part must still be unique within the complete runtime!
   * </p>
   * <p>
   * So using hierarchical class names may make sense to support organizing the classes in hierarchical structures for storage and lookup,
   * instead of being limited to simple "linear" lists. But they do not introduce a runtime "name space" like packages do for Java classes.
   * </p>
   * If this provider doesn't have this class available, it should throw a <code>ClassNotFoundException</code>.
   * (Optionally, it could also just return null, for those dvp-ers who don't like exceptions. ;-) )
   *
   * @param className Mandatory, not-null.
   * @param versionSpec optional constraint on desired version for the class that must be provided. If null, no version constraint is imposed.
   * @return the actor-oriented class matching the given className
   * @exception ClassNotFoundException if this provider can not provide the requested class for the requested version (if specified)
   */
  CompositeEntity getActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
}
