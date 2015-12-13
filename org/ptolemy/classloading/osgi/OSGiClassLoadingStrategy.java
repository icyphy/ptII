/* This is the preferred <code>ClassLoadingStrategy</code> implementation in a full-blown OSGi-based runtime.

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

package org.ptolemy.classloading.osgi;

import java.util.HashSet;
import java.util.Set;

import org.ptolemy.classloading.ActorOrientedClassProvider;
import org.ptolemy.classloading.ClassLoadingStrategy;
import org.ptolemy.classloading.ModelElementClassProvider;
import org.ptolemy.commons.VersionSpecification;

import ptolemy.kernel.CompositeEntity;

/**
 * This is the preferred <code>ClassLoadingStrategy</code> implementation in a full-blown OSGi-based runtime.
 * It supports dynamic actor class updates through OSGi's great dynamism based on micro-services.
 * <p>
 * This dynamism is obtained by delegating the class loading to the registered implementations of 
 * <code>ModelElementClassProvider</code> and <code>ActorOrientedClassProvider</code>.
 * </p>
 * 
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public class OSGiClassLoadingStrategy implements ClassLoadingStrategy {

  public Class<?> loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    Class<?> result = null;
    
    for(ModelElementClassProvider classProvider : _modelElementClassProviders) {
      try {
        result=classProvider.getClass(className, versionSpec);
        if(result!=null) {
          break;
        }
      } catch (ClassNotFoundException e) {
        // just means the provider doesn't know about this one
      }
    }
    if(result!=null) {
      return result;
    } else {
      throw new ClassNotFoundException(className);
    }
  }

  public CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    CompositeEntity result = null;
    
    for(ActorOrientedClassProvider classProvider : _actorOrientedClassProviders) {
      try {
        result=classProvider.getActorOrientedClass(className, versionSpec);
        if(result!=null) {
          break;
        }
      } catch (ClassNotFoundException e) {
        // just means the provider doesn't know about this one
      }
    }
    if(result!=null) {
      return result;
    } else {
      throw new ClassNotFoundException(className);
    }
  }

  // provider registration mgmt stuff
  
  /**
   * Adds the given provider to the set of registered ModelElementClassProviders.
   * 
   * @param classProvider should be not-null
   * @return true if the entry was added successfully
   * @throws IllegalArgumentException when the given provider is null
   */
  public boolean addModelElementClassProvider(ModelElementClassProvider classProvider) {
    if(classProvider==null) {
      throw new IllegalArgumentException("classProvider can not be null");
    }
    return _modelElementClassProviders.add(classProvider);
  }
  
  /**
   * Removes the given provider from the set of registered ModelElementClassProviders.
   * 
   * @param classProvider should be not-null
   * @return true if the set of registered providers contained the given instance and it was removed successfully
   * @throws IllegalArgumentException when the given provider is null
   */
  public boolean removeModelElementClassProvider(ModelElementClassProvider classProvider) {
    if(classProvider==null) {
      throw new IllegalArgumentException("classProvider can not be null");
    }
    return _modelElementClassProviders.remove(classProvider);
  }
  
  /**
   * Clears the set of registered ModelElementClassProviders.
   * Does not touch the registered ActorOrientedClassProviders.
   */
  public void clearModelElementClassProviders() {
    _modelElementClassProviders.clear();
  }
  
  /**
   * Adds the given provider to the set of registered ActorOrientedClassProviders.
   * 
   * @param classProvider should be not-null
   * @return true if the entry was added successfully
   * @throws IllegalArgumentException when the given provider is null
   */
  public boolean addActorOrientedClassProvider(ActorOrientedClassProvider classProvider) {
    if(classProvider==null) {
      throw new IllegalArgumentException("classProvider can not be null");
    }
    return _actorOrientedClassProviders.add(classProvider);
  }

  /**
   * Removes the given provider from the set of registered ActorOrientedClassProviders.
   * 
   * @param classProvider should be not-null
   * @return true if the set of registered providers contained the given instance and it was removed successfully
   * @throws IllegalArgumentException when the given provider is null
   */
  public boolean removeActorOrientedClassProvider(ActorOrientedClassProvider classProvider) {
    return _actorOrientedClassProviders.remove(classProvider);
  }

  /**
   * Clears the set of registered ActorOrientedClassProviders.
   * Does not touch the registered ModelElementClassProviders.
   */
  public void clearActorOrientedClassProviders() {
    _actorOrientedClassProviders.clear();
  }

  // private stuff
  /** All registered providers for "plain" model elements like actors, directors, ...*/
  private Set<ModelElementClassProvider> _modelElementClassProviders = new HashSet<ModelElementClassProvider>();
  /** All registered providers for actor-oriented classes in a model */
  private Set<ActorOrientedClassProvider> _actorOrientedClassProviders = new HashSet<ActorOrientedClassProvider>();
}
