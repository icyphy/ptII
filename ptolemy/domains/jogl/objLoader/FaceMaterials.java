/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2011 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
// FaceMaterials.java
// Andrew Davison, November 2006, ad@fivedots.coe.psu.ac.th

/* FaceMaterials stores the face indicies where a material
   is first used. At render time, this information is utilized
   to change the rendering material when a given face needs
   to be drawn.
*/

package ptolemy.domains.jogl.objLoader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class FaceMaterials {
    private HashMap<Integer, String> faceMats;
    // the face index (integer) where a material is first used

    // for reporting
    private HashMap<String, Integer> matCount;

    // how many times a material (string) is used

    public FaceMaterials() {
        faceMats = new HashMap<Integer, String>();
        matCount = new HashMap<String, Integer>();
    } // end of FaceMaterials()

    public void addUse(int faceIdx, String matName) {
        // store the face index and the material it uses
        if (faceMats.containsKey(faceIdx)) {
            System.out.println("Face index " + faceIdx
                    + " changed to use material " + matName);
        }
        faceMats.put(faceIdx, matName);

        // store how many times matName has been used by faces
        if (matCount.containsKey(matName)) {
            int i = matCount.get(matName) + 1;
            matCount.put(matName, i);
        } else {
            matCount.put(matName, 1);
        }
    } // end of addUse()

    public String findMaterial(int faceIdx) {
        return faceMats.get(faceIdx);
    }

    public void showUsedMaterials()
    /* List all the materials used by faces, and the number of
       faces that have used them. */
    {
        System.out.println("No. of materials used: " + matCount.size());

        // build an iterator of material names
        Set<String> keys = matCount.keySet();
        Iterator<String> iter = keys.iterator();

        // cycle through the hashmap showing the count for each material
        String matName;
        int count;
        while (iter.hasNext()) {
            matName = iter.next();
            count = matCount.get(matName);

            System.out.print(matName + ": " + count);
            System.out.println();
        }
    } // end of showUsedMaterials()

} // end of FaceMaterials class
