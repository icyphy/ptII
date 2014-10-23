/*
 * Copyright (c) 2002-2007 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.forms.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Consists only of static utility methods.
 *
 * This class may be merged with the FormLayoutUtils extra - or not. *
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class Utilities {

    // Instance *************************************************************

    private Utilities() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    /**
     * Lazily checks and answers whether the Aqua look&amp;feel is active.
     *
     * @return true if the current look&amp;feel is Aqua
     */
    public static boolean isLafAqua() {
        if (cachedIsLafAqua == null) {
            cachedIsLafAqua = Boolean.valueOf(computeIsLafAqua());
            ensureLookAndFeelChangeHandlerRegistered();
        }
        return cachedIsLafAqua.booleanValue();
    }

    // Caching and Lazily Computing the Laf State *****************************

    /**
     * Holds the cached result of the Aqua l&amp;f check.
     * Is invalidated by the <code>LookAndFeelChangeHandler</code>
     * if the look&amp;feel changes.
     */
    private static Boolean cachedIsLafAqua;

    /**
     * Describes whether the <code>LookAndFeelChangeHandler</code>
     * has been registered with the <code>UIManager</code> or not.
     * It is registered lazily when the first cached l&amp;f state is computed.
     */
    private static boolean lafChangeHandlerRegistered = false;

    private static synchronized void ensureLookAndFeelChangeHandlerRegistered() {
        if (!lafChangeHandlerRegistered) {
            UIManager.addPropertyChangeListener(new LookAndFeelChangeHandler());
            lafChangeHandlerRegistered = true;
        }
    }

    /**
     * Computes and answers whether the Aqua look&amp;feel is active.
     *
     * @return true if the current look&amp;feel is Aqua
     */
    private static boolean computeIsLafAqua() {
        LookAndFeel laf = UIManager.getLookAndFeel();
        return laf.getName().startsWith("Mac OS X Aqua");
    }

    /**
     * Listens to changes of the Look and Feel and invalidates the cache.
     */
    private static final class LookAndFeelChangeHandler implements
    PropertyChangeListener {

        /**
         * Invalidates the cached laf states, if the UIManager has fired
         * any property change event. Since we need to handle look&amp;feel
         * changes only, we check the event's property name to be
         * "lookAndFeel" or <code>null</code>. The check for null is necessary
         * to handle the special event where property name, old and new value
         * are all <code>null</code> to indicate that multiple properties
         * have changed.
         *
         * @param evt  describes the property change
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName == null || propertyName.equals("lookAndFeel")) {
                cachedIsLafAqua = null;
            }
        }
    }

}
