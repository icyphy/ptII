/*
 Initializer of the PtolemyModule with Java SE specific interface to
 implementation mappings.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.util;

import java.util.ResourceBundle;

import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;

///////////////////////////////////////////////////////////////////
//// PtolemyModuleJavaSEInitializer
/**
 * Initializer of the PtolemyModule with Java SE specific interface to
 * implementation mappings.  The module uses ptolemy.actor.JavaSEActorModule.properties
 * file to initialize the PtolemyModule.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PtolemyModuleJavaSEInitializer {

    /**
     * Create instance that initializes the PtolemyInjector.
     */
    public PtolemyModuleJavaSEInitializer() {
        initializeInjector();
    }

    /**
     * Initialize the PtolemyInjector using module definitions from
     * ptolemy.actor.JavaSEActorModule.properties file.
     */
    public static void initializeInjector() {
        if (!_isInitialized) {
            PtolemyInjector.createInjector(new PtolemyModule(ResourceBundle
                    .getBundle("ptolemy.actor.JavaSEActorModule")));
            _isInitialized = true;
        }
    }

    /**
     * Flag indicating whether the injector is initialized.
     */
    private static volatile boolean _isInitialized;
}
