/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
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
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
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

/*
 * UserPrefs.java
 *
 * Created on March 23, 2005, 7:54 PM by Kevin Routley.
 */

package org.mlc.swing.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.prefs.Preferences;

/**
 * This is a singleton container for handling user preferences. These include
 * window size and positions, debug frame visibility, etc.
 * @author Kevin Routley
@version $Id$
@since Ptolemy II 8.0
 */
public class UserPrefs {

    private Preferences prefStore = null;

    static UserPrefs usersPrefs = new UserPrefs();

    /**
     * Access the singleton instance.
     */
    public static UserPrefs getPrefs() {
        return usersPrefs;
    }

    /**
     * Creates a new instance of UserPrefs - private to prevent external
     * instantiation
     */
    private UserPrefs() {
        prefStore = Preferences
                .userNodeForPackage(org.mlc.swing.layout.LayoutConstraintsManager.class);
    }

    boolean showPreviewBorder() {
        return prefStore.get("preview_border", "1").equals("1");
    }

    /**
     * Should we show the debug panel?
     */
    public boolean showDebugPanel() {
        return prefStore.get("debug_panel", "0").equals("1");
    }

    void saveWinLoc(String winname, Point screenloc, Dimension size) {
        prefStore.putInt(winname + "_WinX", screenloc.x < 0 ? 0 : screenloc.x);
        prefStore.putInt(winname + "_WinY", screenloc.y < 0 ? 0 : screenloc.y);
        prefStore.putInt(winname + "_WinH", size.height);
        prefStore.putInt(winname + "_WinW", size.width);
    }

    Rectangle getWinLoc(String winname) {
        Rectangle r = new Rectangle(prefStore.getInt(winname + "_WinX", 0),
                prefStore.getInt(winname + "_WinY", 0), prefStore.getInt(
                        winname + "_WinW", 800), prefStore.getInt(winname
                                + "_WinH", 600));
        return r;
    }

    /**
     * Save the current state of the panel - debug on/off.
     */
    public void saveDebugState(boolean b) {
        prefStore.put("debug_panel", b ? "1" : "0");
    }

    /** Fetch a window's size and position data and apply it. The window name
     * must be the same as supplied when calling @see saveWinLoc.
     */
    public void useSavedBounds(String winname, Window window) {
        if (window == null) {
            return;
        }
        Rectangle r = UserPrefs.getPrefs().getWinLoc(winname);
        window.setLocation(r.x, r.y);
        window.setSize(r.width, r.height);
    }

    /** Save a window's size and position data under the provided name.
     * (The data must be fetched using the same name).
     */
    public void saveWinLoc(String winname, Window window) {
        if (window == null) {
            return;
        }
        saveWinLoc(winname, window.getLocationOnScreen(), window.getSize());
    }
}
