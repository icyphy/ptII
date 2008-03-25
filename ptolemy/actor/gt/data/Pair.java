/* Generic pair data structure.

@Copyright (c) 1997-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt.data;

public class Pair<E1, E2> extends Tuple<Object> {

    public Pair(E1 first, E2 second) {
        super(first, second);
    }

    @SuppressWarnings("unchecked")
    public E1 getFirst() {
        return (E1) get(0);
    }

    @SuppressWarnings("unchecked")
    public E2 getSecond() {
        return (E2) get(1);
    }

    public void set(E1 first, E2 second) {
        set(0, first);
        set(1, second);
    }

    public void setFirst(E1 first) {
        set(0, first);
    }

    public void setSecond(E2 second) {
        set(1, second);
    }
}
