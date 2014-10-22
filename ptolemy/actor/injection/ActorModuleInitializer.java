/*
 This class initialized the PtolemyInjector with Java SE specific interface to
 implementation mappings that are within Ptolemy package.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.actor.injection;

import java.util.ArrayList;
import java.util.ResourceBundle;

///////////////////////////////////////////////////////////////////
//// ActorModuleInitializer
/**
 * Initializer of the PtolemyInjector with Java SE specific actor interface to
 * implementation mappings.  This class reads the ptolemy.actor.ActorModule.properties
 * file to initialize the PtolemyModule.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ActorModuleInitializer {

    /**
     * Create instance that initializes the PtolemyInjector.
     */
    public ActorModuleInitializer() {
        initializeInjector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Get Modules used by the initializer.
     * @return get Modules used by the initializer.
     */
    public static ArrayList<PtolemyModule> getModules() {
        return _PTOLEMY_MODULES;
    }

    /**
     * Initialize the PtolemyInjector using module definitions from
     * the ptolemy.actor.ActorModule.properties file.
     */
    public synchronized static void initializeInjector() {
        if (!_isInitialized) {
            _initializer.initialize();
            _isInitialized = true;
        }
    }

    /** Set the initializer.
     *  @param initializer The initializer.
     */
    public static void setInitializer(Initializer initializer) {
        if (initializer == null) {
            throw new NullPointerException("Initializer must be non-null");
        }
        _initializer = initializer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * Flag indicating whether the injector is initialized.
     */
    private static volatile boolean _isInitialized;

    /**
     * Modules used by the initializer
     */
    private static final ArrayList<PtolemyModule> _PTOLEMY_MODULES = new ArrayList<PtolemyModule>();
    static {
        _PTOLEMY_MODULES.add(new PtolemyModule(ResourceBundle
                .getBundle("ptolemy.actor.ActorModule")));
    }

    /**
     * The default initializer used by the PtolemyInjector if one is not provided to
     * it.  The default initializer would initialize Java SE specific classes.
     */
    protected static Initializer _defaultInitializer = new Initializer() {
        @Override
        public void initialize() {
            PtolemyInjector.createInjector(_PTOLEMY_MODULES);
        }
    };

    private static Initializer _initializer = _defaultInitializer;

    /**
     * Initializer is responsible for initializing the PtolemyInjector with
     * modules specific to the platform it was developed for.
     *
     */
    public interface Initializer {
        /**
         * Initialize the PtolemyInjector with modules specific to its platform.
         */
        public void initialize();
    }
}
