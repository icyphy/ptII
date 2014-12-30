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

package com.jgoodies.forms.factories;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * An interface that defines the factory methods as used by the
 * {@link com.jgoodies.forms.builder.PanelBuilder} and its subclasses.<p>
 *
 * The String arguments passed to the methods <code>#createLabel(String)</code>,
 * <code>#createTitle(String)</code>, and
 * <code>#createSeparator(String, int)</code> can contain an optional
 * mnemonic marker. The mnemonic and mnemonic index are indicated
 * by a single ampersand (<tt>&amp;</tt>). For example
 * <tt>&quot;&amp;Save&quot;</tt>, or <tt>&quot;Save&nbsp;&amp;as&quot;</tt>.
 * To use the ampersand itself duplicate it, for example
 * <tt>&quot;Look&amp;&amp;Feel&quot;</tt>.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 * @see    DefaultComponentFactory
 * @see    com.jgoodies.forms.builder.PanelBuilder
 */

public interface ComponentFactory {

    /**
     * Creates and returns a label with an optional mnemonic.
     *
     * <pre>
     * createLabel("Name");       // No mnemonic
     * createLabel("N&amp;ame");      // Mnemonic is 'a'
     * createLabel("Save &amp;as");   // Mnemonic is the second 'a'
     * createLabel("Look&amp;&amp;Feel"); // No mnemonic, text is Look&amp;Feel
     * </pre>
     *
     * @param textWithMnemonic  the label's text -
     *     may contain an ampersand (<tt>&amp;</tt>) to mark a mnemonic
     * @return an label with optional mnemonic
     */
    public JLabel createLabel(String textWithMnemonic);

    /**
     * Creates and returns a title label that uses the foreground color
     * and font of a <code>TitledBorder</code>.<p>
     *
     * <pre>
     * createTitle("Name");       // No mnemonic
     * createTitle("N&amp;ame");      // Mnemonic is 'a'
     * createTitle("Save &amp;as");   // Mnemonic is the second 'a'
     * createTitle("Look&amp;&amp;Feel"); // No mnemonic, text is Look&amp;Feel
     * </pre>
     *
     * @param textWithMnemonic  the label's text -
     *     may contain an ampersand (<tt>&amp;</tt>) to mark a mnemonic
     * @return an emphasized title label
     */
    public JLabel createTitle(String textWithMnemonic);

    /**
     * Creates and returns a labeled separator. Useful to separate
     * paragraphs in a panel, which is often a better choice than a
     * <code>TitledBorder</code>.<p>
     *
     * <pre>
     * final int LEFT = SwingConstants.LEFT;
     * createSeparator("Name",       LEFT); // No mnemonic
     * createSeparator("N&amp;ame",      LEFT); // Mnemonic is 'a'
     * createSeparator("Save &amp;as",   LEFT); // Mnemonic is the second 'a'
     * createSeparator("Look&amp;&amp;Feel", LEFT); // No mnemonic, text is Look&amp;Feel
     * </pre>
     *
     * @param textWithMnemonic  the label's text -
     *     may contain an ampersand (<tt>&amp;</tt>) to mark a mnemonic
     * @param alignment text alignment, one of <code>SwingConstants.LEFT</code>,
     *     <code>SwingConstants.CENTER</code>, <code>SwingConstants.RIGHT</code>
     * @return a title label with separator on the side
     */
    public JComponent createSeparator(String textWithMnemonic, int alignment);

}
