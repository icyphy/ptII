/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.codegen.kernel.userMacro;

import java.util.List;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

public class cdr {

    public static String handleMacro(List<String> arguments)
            throws IllegalActionException {
        String list = arguments.get(0).trim();
        int commaIndex = CodeGeneratorHelper._indexOf(",", list, 1);
        if (commaIndex >= 0) {
            String result = '(' + list.substring(commaIndex + 1, list
                    .lastIndexOf(')')) + ')';
            return result;
        } else {
            return "()";
        }
    }

    public static boolean checkArguments(List<String> arguments) {
        if (arguments.size() != 1) {
            return false;
        }

        String list = arguments.get(0).trim();
        if (!list.startsWith("(") || !list.endsWith(")")) {
            return false;
        }

        return true;
    }
}
