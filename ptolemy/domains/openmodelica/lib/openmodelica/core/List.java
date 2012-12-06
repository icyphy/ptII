/* 
 * This file is part of Modelica Development Tooling.
 *
 * Copyright (c) 2005, Linköpings universitet, Department of
 * Computer and Information Science, PELAB
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 * http://www.opensource.org/licenses/bsd-license.php)
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of LinkÃ¶pings universitet nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.domains.openmodelica.lib.openmodelica.core;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A modelica list representation. Modelica lists can be nested. A modelica
 * list can contain both an elements and other lists. Just think LISP. (()(())) 
 * 
 * This is basically a wrapper around a standard linked list.
 */
public class List extends ListElement implements Iterable<ListElement> {
    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    java.util.List<ListElement> theList;

    public List() {
        theList = new LinkedList<ListElement>();
    }

    private List(java.util.List<ListElement> contents) {
        theList = contents;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void addAll(List proposals) {
        theList.addAll(proposals.theList);
    }

    public void append(ListElement element) {
        theList.add(element);
    }

    public void clear() {
        theList.clear();
    }

    public ListElement elementAt(int index) {
        return theList.get(index);
    }

    public Iterator<ListElement> iterator() {
        return theList.iterator();
    }

    public int size() {
        return theList.size();
    }

    public List subList(int fromIndex, int toIndex) {
        return new List(theList.subList(fromIndex, toIndex));
    }
}
