/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.actor.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * <p>Titre : ShortTitleTabbedPaneUI</p>
 * <p>Description : A customized UI for the TabbedPane use to navigate</p>
Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc & Benoit Masson
 * 01 sept. 2003
 */
public class ShortTitleTabbedPaneUI extends BasicTabbedPaneUI {

        private static final int TAB_MINIMUM_SIZE = 20;

        /* (non-Javadoc)
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#paintText(java.awt.Graphics, int, java.awt.Font, java.awt.FontMetrics, int, java.lang.String, java.awt.Rectangle, boolean)
         */
        protected void paintText(
                Graphics g,
                int tabPlacement,
                Font font,
                FontMetrics metrics,
                int tabIndex,
                String title,
                Rectangle textRect,
                boolean isSelected) {

                if (title.length() > TAB_MINIMUM_SIZE) {
                        title =
                                "..."
                                        + title.substring(
                                                title.length() - TAB_MINIMUM_SIZE + 3,
                                                title.length());
                        textRect.x += 4;
                }

                super.paintText(
                        g,
                        tabPlacement,
                        font,
                        metrics,
                        tabIndex,
                        title,
                        textRect,
                        isSelected);
        }

        /* (non-Javadoc)
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#calculateTabWidth(int, int, java.awt.FontMetrics)
         */
        protected int calculateTabWidth(
                int tabPlacement,
                int tabIndex,
                FontMetrics metrics) {

                int taille = 0;
                String title = tabPane.getTitleAt(tabIndex);
                if (title.length() > TAB_MINIMUM_SIZE) {
                        taille =
                                SwingUtilities.computeStringWidth(
                                        metrics,
                                        (title
                                                .substring(
                                                        title.length() - TAB_MINIMUM_SIZE,
                                                        title.length())))
                                        + 3;
                } else {
                        taille = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                }

                return taille;
        }

}
