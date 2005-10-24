/*
  A simple program to test nested exceptions.

  Copyright (c) 2001-2005 The University of Maryland.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the above
  copyright notice and the following two paragraphs appear in all copies
  of this software.

  IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
  FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
  ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
  THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.

  THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
  PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
  MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY

*/

/// NestedExceptions

/*** A simple program to test nested exceptions.
     The code tests nested exceptions.
     The program should catch the resulting exception.
     The output of the program would be:
     Caught First Exception.

     @author Paul C. Yang, Ankush Varma
     @version $Id$
     @since Ptolemy II 4.0
     @Pt.ProposedRating Red (ankush)
     @Pt.AcceptedRating Red (ankush)
*/
public class NestedExceptions {
    /** Outputs the messages and caught exceptions.
     *  @param args Application arguments.
     */
    public static void main(String[] args) {
        // Outer Exception.
        try {
            // Inner Exception.
            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.println("Caught First Exception.");
            }

            throw new Exception();
        } catch (Exception e) {
            System.out.println("Caught Second Exception.");
        }
    }
}
