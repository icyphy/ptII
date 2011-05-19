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
package org.mlc.swing.layout;

import java.util.List;

/**
 * This represents a property in the ComponentBuilder. This is kind of a hack
 * and this really should be done simply using introspection and property
 * editors. It is simple though and works for now...
 *
 * @author Michael Connor mlconnor&#064;yahoo.com
@version $Id$
@since Ptolemy II 8.0
 */
public class BeanProperty {
    String name;

    Class type;

    List<Object> possibleValues;

    /** Creates a new instance of BeanProperty */
    public BeanProperty(String name, Class type, List<Object> possibleValues) {
        this.name = name;
        this.type = type;
        this.possibleValues = possibleValues;
    }

    public BeanProperty(String name, Class type) {
        this(name, type, null);
    }

    /**
     * Registers the value of the name property
     *
     * @param name
     *          The value of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of the name property
     *
     * @return The value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Registers the value of the type property
     *
     * @param type
     *          The value of the property
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * Returns the value of the type property
     *
     * @return The value
     */
    public Class getType() {
        return this.type;
    }

}
