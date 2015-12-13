/* Simple implementation based on Class.forName.

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
 * As the name says... a simple strategy implementation providing a bridge 
 * between the <code>ClassLoadingStrategy</code> approach and 
 * the usage of a plain <code>ClassLoader</code>, for loading Java classes.
 * <br/>
 * REMARK : It does not support loading actor-oriented classes! 
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public class SimpleClassLoadingStrategy implements ClassLoadingStrategy {
  
  /**
   * Constructor that uses the default class loader, 
   * i.e. the one with which this own class was loaded.
   */
  public SimpleClassLoadingStrategy() {
    _classLoader = getClass().getClassLoader();
  }

  /**
   * Enforces the usage of the given class loader.
   * 
   * @param classLoader
   */
  public SimpleClassLoadingStrategy(ClassLoader classLoader) {
    this._classLoader = classLoader;
  }

  @SuppressWarnings("rawtypes")
  public Class loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    return Class.forName(className, true, _classLoader);
  }

  public CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    throw new ClassNotFoundException();
  }
  
  // private stuff

  private ClassLoader _classLoader;
}
