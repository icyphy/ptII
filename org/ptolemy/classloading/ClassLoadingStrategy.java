/* Strategy to be able to switch class loading mechanisms, depending on the needs for a runtime environment.

Copyright (c) 2015 The Regents of the University of California; iSencia Belgium NV.
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
 * Strategy to be able to switch class loading mechanisms, depending on the needs for a runtime environment
 *  (especially for actors and other model entities),
 * <p>
 * In a "plain" Java SE runtime, a default implementation would use simple <code>Class.forName()</code> 
 * (for Java classes) or local file-lookup (for actor-oriented classes) or similar.<br/>
 * In an OSGi-based runtime, more advanced options can be implemented to allow dynamic actor class updates, version management etc.
 * </p>
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public interface ClassLoadingStrategy {
  
  /**
   * 
   * @param className
   * @param versionSpec
   * @return the Class for the given name
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("rawtypes")
  Class loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
  
  /**
   * 
   * @param className
   * @param versionSpec
   * @return
   * @throws ClassNotFoundException
   */
  CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
}
