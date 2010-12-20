
// Faces.java
// Andrew Davison, February 2007, ad@fivedots.coe.psu.ac.th

/* Faces stores the information for each face of a model.

   A face is represented by three arrays of indicies for 
   the vertices, normals, and tex coords used in that face.

   facesVertIdxs, facesTexIdxs, and facesNormIdxs are ArrayLists of
   those arrays; one entry for each face.

   renderFace() is supplied with a face index, looks up the
   associated vertices, normals, and tex coords indicies arrays,
   and uses those arrays to access the actual vertices, normals, 
   and tex coords data for rendering the face.

   CHANGES (Feb 2007)
     - changed renderFace() to flip tex coords if necessary

*/

package ptolemy.domains.jogl.objLoader;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;
import com.sun.opengl.util.*;
import java.text.DecimalFormat;



public class Faces
{
  private static final float DUMMY_Z_TC = -5.0f;

  /* indicies for vertices, tex coords, and normals used
     by each face */
  private ArrayList<int[]> facesVertIdxs;
  private ArrayList<int[]> facesTexIdxs;
  private ArrayList<int[]> facesNormIdxs;

  // references to the model's vertices, normals, and tex coords
  private ArrayList<Tuple3> verts;
  private ArrayList<Tuple3> normals;
  private ArrayList<Tuple3> texCoords;

  // for reporting
  private DecimalFormat df = new DecimalFormat("0.##");  // 2 dp


  public Faces(ArrayList<Tuple3> vs, ArrayList<Tuple3> ns, 
                                         ArrayList<Tuple3> ts)
  {
    verts = vs;
    normals = ns;
    texCoords = ts;

    facesVertIdxs = new ArrayList<int[]>();
    facesTexIdxs = new ArrayList<int[]>();
    facesNormIdxs = new ArrayList<int[]>();
  }  // end of Faces()



 public boolean addFace(String line)
 /* get this face's indicies from line "f v/vt/vn ..."
    with vt or vn index values perhaps being absent. */
 {
    try {
      line = line.substring(2);   // skip the "f "
      StringTokenizer st = new StringTokenizer(line, " ");
      int numTokens = st.countTokens();   // number of v/vt/vn tokens
      // create arrays to hold the v, vt, vn indicies
      int v[] = new int[numTokens]; 
      int vt[] = new int[numTokens];
      int vn[] = new int[numTokens];

      for (int i = 0; i < numTokens; i++) {
        String faceToken = addFaceVals(st.nextToken());  // get a v/vt/vn token
        // System.out.println(faceToken);

        StringTokenizer st2 = new StringTokenizer(faceToken, "/");
        int numSeps = st2.countTokens();  // how many '/'s are there in the token

        v[i] = Integer.parseInt(st2.nextToken());
        vt[i] = (numSeps > 1) ? Integer.parseInt(st2.nextToken()) : 0;
        vn[i] = (numSeps > 2) ? Integer.parseInt(st2.nextToken()) : 0;
            // add 0's if the vt or vn index values are missing;
            // 0 is a good choice since real indicies start at 1
      }
      // store the indicies for this face
      facesVertIdxs.add(v);
      facesTexIdxs.add(vt);
      facesNormIdxs.add(vn);
    }
    catch (NumberFormatException e) {
      System.out.println("Incorrect face index");
      System.out.println(e.getMessage());
      return false;
    }
    return true;
  }  // end of addFace()


  private String addFaceVals(String faceStr)
  /* A face token (v/vt/vn) may be missing vt or vn
     index values; add 0's in those cases.
  */
  {
    char chars[] = faceStr.toCharArray();
    StringBuffer sb = new StringBuffer();
    char prevCh = 'x';   // dummy value

    for (int k = 0; k < chars.length; k++) {
      if (chars[k] == '/' && prevCh == '/')   // if no char between /'s
        sb.append('0');   // add a '0'
      prevCh = chars[k];
      sb.append(prevCh);
    }
    return sb.toString();
  }  // end of addFaceVals()



  public void renderFace(int i, boolean flipTexCoords, GL gl)
  /* Render the ith face by getting the vertex, normal, and tex
     coord indicies for face i. Use those indicies to access the
     actual vertex, normal, and tex coord data, and render the face.

     Each face uses 3 array of indicies; one for the vertex
     indicies, one for the normal indicies, and one for the tex
     coord indicies.

     If the model doesn't use normals or tex coords then the indicies
     arrays will contain 0's.

     If the tex coords need flipping then the t-values are changed.
  */
  {
    if (i >= facesVertIdxs.size())   // i out of bounds?
      return;

    int[] vertIdxs = (int[]) (facesVertIdxs.get(i));
         // get the vertex indicies for face i

    int polytype;
    if (vertIdxs.length == 3)
      polytype = gl.GL_TRIANGLES;
    else if (vertIdxs.length == 4)
      polytype = gl.GL_QUADS;
    else
      polytype = gl.GL_POLYGON;


    gl.glBegin(polytype);

    // get the normal and tex coords indicies for face i
    int[] normIdxs = (int[]) (facesNormIdxs.get(i));
    int[] texIdxs = (int[]) (facesTexIdxs.get(i));

    /* render the normals, tex coords, and vertices for face i
       by accessing them using their indicies */
    Tuple3 vert, norm, texCoord;
    float yTC;
    for (int f = 0; f < vertIdxs.length; f++) {
      if (normIdxs[f] != 0) {  // if there are normals, render them
        norm = (Tuple3) normals.get(normIdxs[f] - 1);
        gl.glNormal3f(norm.getX(), norm.getY(), norm.getZ());
      }
                
      if (texIdxs[f] != 0) {   // if there are tex coords, render them
        texCoord = (Tuple3) texCoords.get(texIdxs[f] - 1);
        yTC = texCoord.getY();
        if (flipTexCoords)    // flip the y-value (the texture's t-value)
          yTC = 1.0f - yTC;

        if (texCoord.getZ() == DUMMY_Z_TC)  // using 2D tex coords
          gl.glTexCoord2f(texCoord.getX(), yTC);
        else // 3D tex coords
          gl.glTexCoord3f(texCoord.getX(), yTC, texCoord.getZ());
/*
          System.out.print("Tex index: " + (texIdxs[f]) + ": ");
          System.out.println("Tex coord: " + df.format(texCoord.getX()) + ", " +
                                 df.format( yTC ) + ", " +
                                 df.format( texCoord.getZ() ));
*/
      }

      vert = (Tuple3) verts.get(vertIdxs[f] - 1);  // render the vertices
      gl.glVertex3f(vert.getX(), vert.getY(), vert.getZ());
/*
      System.out.print("Vert index: " + (vertIdxs[f]) + ": ");
      System.out.println("Coord: " + df.format(vert.getX()) + ", " +
                                 df.format( vert.getY() ) + ", " +
                                 df.format( vert.getZ() ));
*/
    }
            
    gl.glEnd();
  } // end of renderFace()



  public int getNumFaces()
  {  return facesVertIdxs.size();  }

}  // end of Faces class
