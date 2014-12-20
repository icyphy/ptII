/*
 The Injector class is responsible for loading implementation based on a
 interface.

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

import java.util.HashMap;
import java.util.Map;

///////////////////////////////////////////////////////////////////
//// Injector
/**
 * The Injector class is responsible for loading implementation based on a
 * interface.  The mappings from the interface to implementation must be loaded prior to that.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class Injector {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get implementation for the provided interface based on the mappings
     * loaded into the injector.
     * @param T The implementation.
     * @param type The interface type to load.
     * @return The implementation of the interface.
     */
    public <T> T getInstance(Class<T> type) {
        Class<T> implementation = (Class<T>) _resolvedInterfaceToImplementationMap
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
        } else {
            String implementationName = _interfaceToImplementationMap.get(type
                    .getName());
            if (implementationName != null) {
                try {
                    Class<?> implementationClass = getClass().getClassLoader()
                            .loadClass(implementationName);
                    _resolvedInterfaceToImplementationMap.put(type,
                            implementationClass);
                    return (T) implementationClass.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Problem loading type "
                            + implementationName, e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(
                            "Problem instantiating type " + implementationName,
                            e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(
                            "Problem instantiating type " + implementationName,
                            e);
                }
            }
        }
        throw new IllegalStateException(
                "Implementation for the interface "
                        + type
                        + " was not found. "
                        + "Perhaps\n"
                        + type.getName()
                        + "="
                        + type.getName()
                        + "\nneeds to be added to the implementations class mappings file ptolemy/actor/*.properties such as ptolemy/actor/ActorModule.properties or some other injector file.");

    }

    /**
     * Load the interface to implementation mappings into the injector.
     * @param interfaceToImplementationMap The interface to implementation mapping.
     */
    public void loadMappings(Map<String, String> interfaceToImplementationMap) {
        _interfaceToImplementationMap.putAll(interfaceToImplementationMap);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The resolved interface to the implementation class mappings.
     */
    private HashMap<Class<?>, Class<?>> _resolvedInterfaceToImplementationMap = new HashMap<Class<?>, Class<?>>();

    /**
     * The resolved interface to the implementation class mappings.
     */
    private HashMap<String, String> _interfaceToImplementationMap = new HashMap<String, String>();
}
