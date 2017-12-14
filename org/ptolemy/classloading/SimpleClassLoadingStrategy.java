/* Simple implementation based on Class.forName.

Copyright (c) 2015-2016 The Regents of the University of California; iSencia Belgium NV.
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
 *
 * <p>REMARK: It does not support loading actor-oriented classes! </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public class SimpleClassLoadingStrategy implements ClassLoadingStrategy {

    /**
     * Construct a strategy that uses the default class loader,
     * i.e. the one with which this own class was loaded.
     */
    public SimpleClassLoadingStrategy() {
        _classLoader = getClass().getClassLoader();
    }

    /**
     * Construct a strategy that uses the given class loader.
     *
     * @param classLoader The class loader
     */
    public SimpleClassLoadingStrategy(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    /**
     * Load a Java class.
     * @param className The namee of the class.
     * @param versionSpec The version
     * @return the Class for the given name.
     * @exception ClassNotFoundException If the class is not found.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Class loadJavaClass(String className,
            VersionSpecification versionSpec) throws ClassNotFoundException {
        return Class.forName(className, true, _classLoader);
    }

    /**
     *  Load an actor-oriented class, which is typically a .moml file.
     * @param className The namee of the class.
     * @param versionSpec The version
     * @return the Class for the given name.
     * @exception ClassNotFoundException Always thrown in this base class.
     */
    @Override
    public CompositeEntity loadActorOrientedClass(String className,
            VersionSpecification versionSpec) throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ClassLoader _classLoader;
}
