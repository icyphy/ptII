/*
 The Injector class is responsible for loading implementation based on a 
 interface.
 
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

package ptolemy.actor.injection;

import java.util.HashMap;
import java.util.Map;

///////////////////////////////////////////////////////////////////
//// Injector
/**
 * The Injector class is responsible for loading implementation based on a 
 * interface.  The mappings from the interface to implementation must be loaded prior to that.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class Injector {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get implementation for the provided interface based on the mappings
     * loaded into the injector.
     * @param type The interface type to load.
     * @return The implementation of the interface.
     */
    public <T> T getInstance(Class<T> type) {
        Class<T> implementation = (Class<T>) _interfaceToImplementationMap
                .get(type);
        if (implementation != null) {
            try {
                return implementation.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalStateException("Problem instantiating type "
                        + implementation, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Problem instantiating type "
                        + implementation, e);
            }
        }
        throw new IllegalStateException("Implementation for the interface "
                + type + " was not found");
    }

    /**
     * Load the interface to implementation mappings into the injector.
     * @param interfaceToImplementationMap The interface to implementation mapping.
     */
    public void loadMappings(
            Map<Class<?>, Class<?>> interfaceToImplementationMap) {
        _interfaceToImplementationMap.putAll(interfaceToImplementationMap);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The interface to the implementation mapping.
     */
    private HashMap<Class<?>, Class<?>> _interfaceToImplementationMap = new HashMap<Class<?>, Class<?>>();
}
