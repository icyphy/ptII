
// OBJModel.java
// Andrew Davison, February 2007, ad@fivedots.coe.psu.ac.th

/* Load the OBJ model from MODEL_DIR, centering and scaling it.
   The scale comes from the sz argument in the constructor, and
   is implemented by changing the vertices of the loaded model.
   
   The model can have vertices, normals and tex coordinates, and
   refer to materials in a MTL file.

   The OpenGL commands for rendering the model are stored in 
   a display list (modelDispList), which is drawn by calls to
   draw().

   Information about the model is printed to stdout.

   Based on techniques used in the OBJ loading code in the
   JautOGL multiplayer racing game by Evangelos Pournaras 
   (http://today.java.net/pub/a/today/2006/10/10/
               development-of-3d-multiplayer-racing-game.html 
   and https://jautogl.dev.java.net/), and the 
   Asteroids tutorial by Kevin Glass 
   (http://www.cokeandcode.com/asteroidstutorial)

   CHANGES (Feb 2007)
     - a global flipTexCoords boolean
     - drawToList() sets and uses flipTexCoords
*/

package ptolemy.domains.jogl.objLoader;

import java.io.*;
import java.util.*;

import javax.media.opengl.*;


public class OBJModel
{
  private static final String MODEL_DIR = "/Users/yasemindemir/Documents/workspacePTII/ptII/ptolemy/domains/jogl/objLoader/models/";
  private static final float DUMMY_Z_TC = -5.0f;

  // collection of vertices, normals and texture coords for the model
  private ArrayList<Tuple3> verts;
  private ArrayList<Tuple3> normals;
  private ArrayList<Tuple3> texCoords;
  private boolean hasTCs3D = true;  
        // whether the model uses 3D or 2D tex coords
  private boolean flipTexCoords = false;   
    // whether tex coords should be flipped around the y-axis

  private Faces faces;              // model faces
  private FaceMaterials faceMats;   // materials used by faces
  private Materials materials;      // materials defined in MTL file
  private ModelDimensions modelDims;  // model dimensions

  private String modelNm;    // without path or ".OBJ" extension
  private float maxSize;     // for scaling the model

  private int modelDispList;  // the model's display list


  public OBJModel(String nm, GL gl)
  {  this(nm, 1.0f, gl, false);  }


  public OBJModel(String nm, float sz, GL gl, boolean showDetails)
  {
    modelNm = nm;
    maxSize = sz;
    initModelData(modelNm);

    loadModel(modelNm);
    centerScale();
    drawToList(gl);

    if (showDetails)
      reportOnModel();
  } // end of OBJModel()


  private void initModelData(String modelNm)
  {
    verts = new ArrayList<Tuple3>();
    normals = new ArrayList<Tuple3>();
    texCoords = new ArrayList<Tuple3>();

    faces = new Faces(verts, normals, texCoords);
    faceMats = new FaceMaterials();
    modelDims = new ModelDimensions();
  }  // end of initModelData()


  private void loadModel(String modelNm)
  {
    String fnm = MODEL_DIR + modelNm + ".obj";
    try {
      System.out.println("Loading model from " + fnm + " ...");
      BufferedReader br = new BufferedReader( new FileReader(fnm) );
      readModel(br);
      br.close();
    }
    catch(IOException e)
    {  System.out.println(e.getMessage());  
       System.exit(1);
    }
  }  // end of loadModel()


  private void readModel(BufferedReader br)
  // parse the OBJ file line-by-line
  {
    boolean isLoaded = true;   // hope things will go okay

    int lineNum = 0;
    String line;
    boolean isFirstCoord = true;
    boolean isFirstTC = true;
    int numFaces = 0;

    try {
      while (((line = br.readLine()) != null) && isLoaded) {
        lineNum++;
        if (line.length() > 0) {
          line = line.trim();
                    
          if (line.startsWith("v ")) {   // vertex
            isLoaded = addVert(line, isFirstCoord);
            if (isFirstCoord)
              isFirstCoord = false;
          }
          else if (line.startsWith("vt")) {   // tex coord
            isLoaded = addTexCoord(line, isFirstTC);
            if (isFirstTC)
              isFirstTC = false;
          }
          else if (line.startsWith("vn"))    // normal
            isLoaded = addNormal(line);
          else if (line.startsWith("f ")) {  // face
            isLoaded = faces.addFace(line);
            numFaces++;
          }
          else if (line.startsWith("mtllib "))   // load material
            materials = new Materials( line.substring(7) );
          else if (line.startsWith("usemtl "))   // use material
            faceMats.addUse( numFaces, line.substring(7));   
          else if (line.charAt(0) == 'g') {  // group name
            // not implemented
          }
          else if (line.charAt(0) == 's') {  // smoothing group
            // not implemented
          }
          else if (line.charAt(0) == '#')   // comment line
            continue;
          else
            System.out.println("Ignoring line " + lineNum + " : " + line);
        }
      }
    }
    catch (IOException e) {
      System.out.println( e.getMessage() );
      System.exit(1);
    }

    if (!isLoaded) {
      System.out.println("Error loading model");  
      System.exit(1);
    }
  } // end of readModel()



  private boolean addVert(String line, boolean isFirstCoord)
  /* Add vertex from line "v x y z" to vert ArrayList, 
     and update the model dimension's info. */
  {
	Tuple3 vert = readTuple3(line);
    if (vert != null) {
      verts.add(vert);
      if (isFirstCoord)
        modelDims.set(vert);
      else
        modelDims.update(vert);
      return true;
    }
    return false;
  } // end of addVert()



  private Tuple3 readTuple3(String line)
  /* The line starts with an OBJ word ("v" or "vn"), followed
     by three floats (x, y, z) separated by spaces
  */
  {
    StringTokenizer tokens = new StringTokenizer(line, " ");
    tokens.nextToken();    // skip the OBJ word
		
    try {
      float x = Float.parseFloat(tokens.nextToken());
      float y = Float.parseFloat(tokens.nextToken());
      float z = Float.parseFloat(tokens.nextToken());
			
      // System.out.println("Read tuple " + x + ", " + y + ", " + z);
      return new Tuple3(x,y,z);
    } 
    catch (NumberFormatException e) 
    {  System.out.println(e.getMessage());  }

    return null;   // means an error occurred
  }  // end of readTuple3()



  private boolean addTexCoord(String line, boolean isFirstTC)
  /* Add the texture coordinate from the line "vt x y z" to 
     the texCoords ArrayList. There may only be two tex coords
     on the line, which is determined by looking at the first
     tex coord line. */
  {
    if (isFirstTC) {
      hasTCs3D = checkTC3D(line);
      System.out.println("Using 3D tex coords: " + hasTCs3D);
    }

	Tuple3 texCoord = readTCTuple(line);
    if (texCoord != null) {
      texCoords.add( texCoord );
      return true;
    }
    return false;
  }  // end of addTexCoord()


  private boolean checkTC3D(String line)
  /* Check if the line has 4 tokens, which will be
     the "vt" token and 3 tex coords in this case. */
  {
    String[] tokens = line.split("\\s+");
    return (tokens.length == 4);
  }  // end of checkTC3D()


  private Tuple3 readTCTuple(String line)
  /* The line starts with a "vt" OBJ word and
     two or three floats (x, y, z) for the tex coords separated
     by spaces. If there are only two coords, then the z-value
     is assigned a dummy value, DUMMY_Z_TC.
  */
  {
    StringTokenizer tokens = new StringTokenizer(line, " ");
    tokens.nextToken();    // skip "vt" OBJ word
		
    try {
      float x = Float.parseFloat(tokens.nextToken());
      float y = Float.parseFloat(tokens.nextToken());

      float z = DUMMY_Z_TC;
      if (hasTCs3D)
        z = Float.parseFloat(tokens.nextToken());
			
      return new Tuple3(x,y,z);
    } 
    catch (NumberFormatException e) 
    {  System.out.println(e.getMessage());  }

    return null;   // means an error occurred
  }  // end of readTCTuple()


  private boolean addNormal(String line)
  // add normal from line "vn x y z" to the normals ArrayList
  {
	Tuple3 normCoord = readTuple3(line);
    if (normCoord != null) {
      normals.add( normCoord );
      return true;
    }
    return false;
 }  // end of addNormal()



  private void centerScale()
  /* Position the model so it's center is at the origin,
     and scale it so its longest dimension is no bigger
     than maxSize. */
  {
    // get the model's center point
    Tuple3 center = modelDims.getCenter();

    // calculate a scale factor
    float scaleFactor = 1.0f;
    float largest = modelDims.getLargest();
    // System.out.println("Largest dimension: " + largest);
    if (largest != 0.0f)
      scaleFactor = (maxSize / largest);
    System.out.println("Scale factor: " + scaleFactor);

    // modify the model's vertices
    Tuple3 vert;
    float x, y, z;
    for (int i = 0; i < verts.size(); i++) {
      vert = (Tuple3) verts.get(i);
      x = (vert.getX() - center.getX()) * scaleFactor;
      vert.setX(x);
      y = (vert.getY() - center.getY()) * scaleFactor;
      vert.setY(y);
      z = (vert.getZ() - center.getZ()) * scaleFactor;
      vert.setZ(z);
    }
  } // end of centerScale()



  private void drawToList(GL gl)
  /* render the model to a display list, so it can be
     drawn quicker later */
  {
    modelDispList = gl.glGenLists(1);
    gl.glNewList(modelDispList, GL.GL_COMPILE);

    gl.glPushMatrix();
    // render the model face-by-face
    String faceMat;
    for (int i = 0; i < faces.getNumFaces(); i++) {
      faceMat = faceMats.findMaterial(i);       // get material used by face i
      if (faceMat != null)
        flipTexCoords = 
           materials.renderWithMaterial(faceMat, gl);  // render using that material
      faces.renderFace(i, flipTexCoords, gl);                  // draw face i
    }
    materials.switchOffTex(gl);
    gl.glPopMatrix();

    gl.glEndList();
  } // end of drawToList()



  public void draw(GL gl)
  {  gl.glCallList(modelDispList);  } 



  private void reportOnModel()
  {
    System.out.println("No. of vertices: " + verts.size());
    System.out.println("No. of normal coords: " + normals.size());
    System.out.println("No. of tex coords: " + texCoords.size());
    System.out.println("No. of faces: " + faces.getNumFaces());

    modelDims.reportDimensions();  
        // dimensions of model (before centering and scaling)

    if (materials != null)
      materials.showMaterials();   // list defined materials 
    faceMats.showUsedMaterials();  // show what materials have been used by faces
  }  // end of reportOnModel()


} // end of OBJModel class
