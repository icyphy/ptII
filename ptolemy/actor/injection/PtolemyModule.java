/*
 PtolemyModule loads interface to implementation mappings from the provided
 ResourceBundle and configures Guice AbstractModule to use those mappings.

 Copyright (c) 2011-2012 The Regents of the University of California.
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

///////////////////////////////////////////////////////////////////
//// PtolemyModule
/**
 * PtolemyModule loads interface to implementation mappings from the provided
 * ResourceBundle.
 *
 * The PtolemyModule is used for creating a PtolemyInjector that is responsible for
 * dependency injection.  The rationale for this class is to promote portability of
 * the Ptolemy by providing different interface to implementation mappings for different
 * platforms such as Android and Java SE.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PtolemyModule {

    /**
     * Create a new instance of the PtolemyModule based on the provided moduleBundle.
     * @param moduleBundle The moduleBundle contains mappings from platform independent
     * interfaces to platform dependent implementations.  The bundle must have key value mappings
     * from the fully specified interface name to the fully specified class name.
     */
    public PtolemyModule(ResourceBundle moduleBundle) {
        // Key is the interface class name.
        // Value is the interface implementation class name.
        // We have to use ResourceBundle.getKeys() method because Android does not support .keySet() method.
        Enumeration<String> keys = moduleBundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = moduleBundle.getString(key);
            getBindings().put(key, value);
        }
    }

    /**
     * Return the bindings from interfaces to their implementations.
     * @return the interface to implementations mappings.
     */
    public HashMap<String, String> getBindings() {
        return _interfaceToImplementationMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The mapping from interface to implementation.
     */
    private final HashMap<String, String> _interfaceToImplementationMap = new HashMap<String, String>();
}
