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

package com.jgoodies.forms.debug;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Provides static methods that help you understand and fix layout problems
 * when using the {@link FormLayout}. Dumps information about the layout grid,
 * layout groups and cell constraints to the console.<p>
 *
 * Implicit values are mapped to concrete. For example, implicit alignments
 * in column and row specifications will be visible. And cell constraint
 * alignments that use or override the column and row defaults are visible too.
 * <pre>
 * ColumnSpec("p")   -&gt; ColumnSpec("fill:pref:0");
 * ColumnSpec("p:1") -&gt; ColumnSpec("fill:pref:1");
 *
 * RowSpec("p")      -&gt; RowSpec("center:pref:0");
 * RowSpec("p:1")    -&gt; RowSpec("center:pref:1");
 * </pre>
 *
 * @author        Karsten Lentzsch
 * @version $Revision$
 *
 * @see FormDebugPanel
 */
public final class FormDebugUtils {

    private FormDebugUtils() {
        // Overrides default constructor; prevents instantiation.
    }

    // Console Dump *********************************************************

    /**
     * Dumps all layout state to the console: column and row specifications,
     * column and row groups, grid bounds and cell constraints.
     *
     * @param container  the layout container
     */
    public static void dumpAll(Container container) {
        if (!(container.getLayout() instanceof FormLayout)) {
            System.out.println("The container's layout is not a FormLayout.");
            return;
        }
        FormLayout layout = (FormLayout) container.getLayout();
        dumpColumnSpecs(layout);
        dumpRowSpecs(layout);
        System.out.println();
        dumpColumnGroups(layout);
        dumpRowGroups(layout);
        System.out.println();
        dumpConstraints(container);
        dumpGridBounds(container);
    }

    /**
     * Dumps the layout's column specifications to the console.
     *
     * @param layout   the <code>FormLayout</code> to inspect
     */
    public static void dumpColumnSpecs(FormLayout layout) {
        System.out.print("COLUMN SPECS:");
        for (int col = 1; col <= layout.getColumnCount(); col++) {
            ColumnSpec colSpec = layout.getColumnSpec(col);
            System.out.print(colSpec.toShortString());
            if (col < layout.getColumnCount()) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }

    /**
     * Dumps the layout's row specifications to the console.
     *
     * @param layout   the <code>FormLayout</code> to inspect
     */
    public static void dumpRowSpecs(FormLayout layout) {
        System.out.print("ROW SPECS:   ");
        for (int row = 1; row <= layout.getRowCount(); row++) {
            RowSpec rowSpec = layout.getRowSpec(row);
            System.out.print(rowSpec.toShortString());
            if (row < layout.getRowCount()) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }

    /**
     * Dumps the layout's column groups to the console.
     *
     * @param layout   the <code>FormLayout</code> to inspect
     */
    public static void dumpColumnGroups(FormLayout layout) {
        dumpGroups("COLUMN GROUPS: ", layout.getColumnGroups());
    }

    /**
     * Dumps the layout's row groups to the console.
     *
     * @param layout   the <code>FormLayout</code> to inspect
     */
    public static void dumpRowGroups(FormLayout layout) {
        dumpGroups("ROW GROUPS:    ", layout.getRowGroups());
    }

    /**
     * Dumps the container's grid info to the console if and only
     * if the container's layout is a <code>FormLayout</code>.
     *
     * @param container   the container to inspect
     * @exception IllegalArgumentException   if the layout is not FormLayout
     */
    public static void dumpGridBounds(Container container) {
        System.out.println("GRID BOUNDS");
        dumpGridBounds(getLayoutInfo(container));
    }

    /**
     * Dumps the grid layout info to the console.
     *
     * @param layoutInfo   provides the column and row origins
     */
    public static void dumpGridBounds(FormLayout.LayoutInfo layoutInfo) {
        System.out.print("COLUMN ORIGINS: ");
        for (int columnOrigin : layoutInfo.columnOrigins) {
            System.out.print(columnOrigin + " ");
        }
        System.out.println();

        System.out.print("ROW ORIGINS:    ");
        for (int rowOrigin : layoutInfo.rowOrigins) {
            System.out.print(rowOrigin + " ");
        }
        System.out.println();
    }

    /**
     * Dumps the component constraints to the console.
     *
     * @param container   the layout container to inspect
     */
    public static void dumpConstraints(Container container) {
        System.out.println("COMPONENT CONSTRAINTS");
        if (!(container.getLayout() instanceof FormLayout)) {
            System.out.println("The container's layout is not a FormLayout.");
            return;
        }
        FormLayout layout = (FormLayout) container.getLayout();
        int childCount = container.getComponentCount();
        for (int i = 0; i < childCount; i++) {
            Component child = container.getComponent(i);
            CellConstraints cc = layout.getConstraints(child);
            String ccString = cc == null ? "no constraints" : cc
                    .toShortString(layout);
            System.out.print(ccString);
            System.out.print("; ");
            String childType = child.getClass().getName();
            System.out.print(childType);
            if (child instanceof JLabel) {
                JLabel label = (JLabel) child;
                System.out.print("      \"" + label.getText() + "\"");
            }
            if (child.getName() != null) {
                System.out.print("; name=");
                System.out.print(child.getName());
            }
            System.out.println();
        }
        System.out.println();
    }

    // Helper Code **********************************************************

    /**
     * Dumps the given groups to the console.
     *
     * @param title       a string title for the dump
     * @param allGroups   a two-dimensional array with all groups
     */
    private static void dumpGroups(String title, int[][] allGroups) {
        System.out.print(title + " {");
        for (int group = 0; group < allGroups.length; group++) {
            int[] groupIndices = allGroups[group];
            System.out.print(" {");
            for (int i = 0; i < groupIndices.length; i++) {
                System.out.print(groupIndices[i]);
                if (i < groupIndices.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("} ");
            if (group < allGroups.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("}");
    }

    /**
     * Computes and returns the layout's grid origins.
     *
     * @param container   the layout container to inspect
     * @return an object that comprises the cell origins and extents
     * @exception IllegalArgumentException   if the layout is not FormLayout
     */
    public static FormLayout.LayoutInfo getLayoutInfo(Container container) {
        if (!(container.getLayout() instanceof FormLayout)) {
            throw new IllegalArgumentException(
                    "The container must use an instance of FormLayout.");
        }
        FormLayout layout = (FormLayout) container.getLayout();
        return layout.getLayoutInfo(container);
    }

}
