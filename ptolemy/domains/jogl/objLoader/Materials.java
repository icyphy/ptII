// Materials.java
// Andrew Davison, February 2007, ad@fivedots.coe.psu.ac.th

/* This class does two main tasks:
      * it loads the material details from the MTL file, storing
        them as Material objects in the materials ArrayList.

      * it sets up a specified material's colours or textures
        to be used when rendering -- see renderWithMaterial()

   CHANGES (Feb 2007)
     - a flipTexCoords global
     - renderWithMaterial() sets and returns flipTexCoords
*/

package ptolemy.domains.jogl.objLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;

public class Materials {
    private static final String MODEL_DIR = "models/";

    private ArrayList<Material> materials;
    // stores the Material objects built from the MTL file data

    // for storing the material currently being used for rendering
    private String renderMatName = null;

    private boolean usingTexture = false;
    private boolean flipTexCoords = false;

    // whether tex coords should be flipped around the y-axis

    public Materials(String mtlFnm) {
        materials = new ArrayList<Material>();

        String mfnm = MODEL_DIR + mtlFnm;
        try {
            System.out.println("Loading material from " + mfnm);
            BufferedReader br = new BufferedReader(new FileReader(mfnm));
            readMaterials(br);
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    } // end of Materials()

    private void readMaterials(BufferedReader br)
    /* Parse the MTL file line-by-line, building Material
       objects which are collected in the materials ArrayList. */
    {
        try {
            String line;
            Material currMaterial = null; // current material

            while (((line = br.readLine()) != null)) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                if (line.startsWith("newmtl ")) { // new material
                    if (currMaterial != null) {
                        materials.add(currMaterial);
                    }

                    // start collecting info for new material
                    currMaterial = new Material(line.substring(7));
                } else if (line.startsWith("map_Kd ")) { // texture filename
                    String fileName = MODEL_DIR + line.substring(7);
                    currMaterial.loadTexture(fileName);
                } else if (line.startsWith("Ka ")) {
                    currMaterial.setKa(readTuple3(line));
                } else if (line.startsWith("Kd ")) {
                    currMaterial.setKd(readTuple3(line));
                } else if (line.startsWith("Ks ")) {
                    currMaterial.setKs(readTuple3(line));
                } else if (line.startsWith("Ns ")) { // shininess
                    float val = Float.valueOf(line.substring(3)).floatValue();
                    currMaterial.setNs(val);
                } else if (line.charAt(0) == 'd') { // alpha
                    float val = Float.valueOf(line.substring(2)).floatValue();
                    currMaterial.setD(val);
                } else if (line.startsWith("illum ")) { // illumination model
                    // not implemented
                } else if (line.charAt(0) == '#') {
                    continue;
                } else {
                    System.out.println("Ignoring MTL line: " + line);
                }

            }
            materials.add(currMaterial);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    } // end of readMaterials()

    private Tuple3 readTuple3(String line)
    /* The line starts with an MTL word such as Ka, Kd, Ks, and
       the three floats (x, y, z) separated by spaces
    */
    {
        StringTokenizer tokens = new StringTokenizer(line, " ");
        tokens.nextToken(); // skip MTL word

        try {
            float x = Float.parseFloat(tokens.nextToken());
            float y = Float.parseFloat(tokens.nextToken());
            float z = Float.parseFloat(tokens.nextToken());

            return new Tuple3(x, y, z);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        return null; // means an error occurred
    } // end of readTuple3()

    public void showMaterials()
    // list all the Material objects
    {
        System.out.println("No. of materials: " + materials.size());
        Material m;
        for (int i = 0; i < materials.size(); i++) {
            m = materials.get(i);
            m.showMaterial();
            // System.out.println();
        }
    } // end of showMaterials()

    // ----------------- using a material at render time -----------------

    public boolean renderWithMaterial(String faceMat, GL gl)
    /* Render using the texture or colours associated with the
       material, faceMat. But only change things if faceMat is
       different from the current rendering material, whose name
       is stored in renderMatName.

       Return true/false if the texture coords need flipping,
       and store the current value in a global
    */
    {
        if (!faceMat.equals(renderMatName)) { // is faceMat is a new material?
            renderMatName = faceMat;
            switchOffTex(gl); // switch off any previous texturing

            // set up new rendering material
            Texture tex = getTexture(renderMatName);
            if (tex != null) { // use the material's texture
                // System.out.println("Using texture with " + renderMatName);
                switchOnTex(tex, gl);

                flipTexCoords = tex.getMustFlipVertically();
                // if (flipTexCoords)
                //  System.out.println("Must flip tcs for " + renderMatName);
            } else {
                setMaterialColors(renderMatName, gl);
            }
        }
        return flipTexCoords;
    } // end of renderWithMaterial()

    public void switchOffTex(GL gl)
    // switch texturing off and put the lights on;
    // also called from ObjModel.drawToList()
    {
        if (usingTexture) {
            gl.glDisable(GL.GL_TEXTURE_2D);
            usingTexture = false;
            gl.glEnable(GL.GL_LIGHTING);
        }
    } // end of resetMaterials()

    private void switchOnTex(Texture tex, GL gl)
    // switch the lights off, and texturing on
    {
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_TEXTURE_2D);
        usingTexture = true;
        tex.bind();
    } // end of resetMaterials()

    private Texture getTexture(String matName)
    // return the texture associated with the material name
    {
        Material m;
        for (int i = 0; i < materials.size(); i++) {
            m = materials.get(i);
            if (m.hasName(matName)) {
                return m.getTexture();
            }
        }
        return null;
    } // end of getTexture()

    private void setMaterialColors(String matName, GL gl)
    // start rendering using the colours specifies by the named material
    {
        Material m;
        for (int i = 0; i < materials.size(); i++) {
            m = materials.get(i);
            if (m.hasName(matName)) {
                m.setMaterialColors(gl);
            }
        }
    } // end of setMaterialColors()

} // end of Materials class
