/* Test driver for ptalon.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2006-2014 The Regents of the University of California.
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
 */
package ptolemy.actor.ptalon;

import java.util.LinkedList;

/** Test driver for ptalon.
 * @author Adam Cataldo
 * @version $Id$
 * @since Ptolemy II 6.1
 */
public class Tester {

    public static void main(String args[]) {
        String in = "ptalonActor:a(x := <1/>, y := <2/>)(z := ptalonActor:b(y := <2/>, z := <2/>))";
        String[] out = _parseActorExpression(in);
        for (String element : out) {
            System.out.println(element);
        }
    }

    public static String[] _parseActorExpression(String expression) {
        expression = expression.replaceAll("\\)(\\p{Blank})*\\(", ",");
        String[] actorSeparated = expression.split("\\(", 2);
        String actor = actorSeparated[0];
        String remains = actorSeparated[1];
        remains = remains.trim().substring(0, remains.length() - 1);
        LinkedList<Integer> markers = new LinkedList<Integer>();
        int parenthesis = 0;
        for (int i = 0; i < remains.length() - 1; i++) {
            if (remains.charAt(i) == '(') {
                parenthesis++;
            } else if (remains.charAt(i) == ')') {
                parenthesis--;
            } else if (remains.charAt(i) == ',' && parenthesis == 0) {
                markers.add(i);
            }
        }
        String[] assignments = new String[markers.size() + 1];
        int lastMarker = -1;
        int index = 0;
        for (int thisMarker : markers) {
            assignments[index] = remains.substring(lastMarker + 1, thisMarker);
            index++;
            lastMarker = thisMarker;
        }
        assignments[index] = remains
                .substring(lastMarker + 1, remains.length());
        String[] output = new String[2 * assignments.length + 1];
        output[0] = actor;
        for (int i = 0; i < assignments.length; i++) {
            String[] equation = assignments[i].split(":=", 2);
            output[2 * i + 1] = equation[0].trim();
            output[2 * i + 2] = equation[1].trim();
        }
        return output;
    }

}
