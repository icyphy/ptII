/* Simple Java Test Program for Nashorn

Copyright (c) 2021 The Regents of the University of California.
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

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;


/** Simple class used by configure to test whether Nashorn,
    the JavaScript engine is present.
    If this file will not compile because the import statement fails,
    then try using a version of the Oracle JDK between 1.8 and 14
    or install OpenJDK.
    @author Christopher Brooks
    @since Ptolemy II 11.1
    @Pt.ProposedRating Green (cxh)
    @Pt.AcceptedRating Red
*/
public class NashornTest {
    public static void main(String[] args) {
        System.out.print(System.getProperty("java.version"));
    }
}
