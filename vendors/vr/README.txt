$Id$

Volume Rendering
----------------

This code came from
http://www.j3d.org/tutorials/quick_fix/volume.html

The copyright is:

Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.

Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
modify and redistribute this software in source and binary code form,
provided that i) this copyright notice and license appear on all copies of
the software; and ii) Licensee does not utilize the software in a manner
which is disparaging to Sun.

This software is provided "AS IS," without a warranty of any kind. ALL
EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
POSSIBILITY OF SUCH DAMAGES.

This software is not designed or intended for use in on-line control of
aircraft, air traffic, aircraft navigation or aircraft communications; or in
the design, construction, operation or maintenance of any nuclear
facility. Licensee represents and warrants that it will not use or
redistribute the Software for such purposes.

-----

We do not include the cubes.vrs, cubes64.vol and VolRend.jar files.
Instead of VolRend.jar, use src/ptvr.jar, which is also installed
in $PTII/lib

The original README file is reproduced below:
----

The application runs from the jarfile VolRend.jar, start it using "java -jar
VolRend.jar".

This program shows several volumetric algorithms:
	
	Axis Volume 2D textures: Volume rendering using "stack of 2D textures"
	Axis Volume 3D texture : Same as 2D, but using a single 3D texture map
	Slice Plane 3D texture : A "slice" through a 3D texture map
	Slice Plane 2D textures: A "simulation" of the 3D method using 2D 
				 texture maps.

Note: PC hardware usually doesn't support 3D texture maps, this will result in
white polygons.

The "color mode" refers to the colormap applied to the textures:
	
	Intensity:	8-bit intensity textures (no colormap)
	Linear Cmap:	32-bit textures 8->32 via R=G=B=A= Intensity
	Segy Cmap:	32-bit textures via a "siesmic" colormap

Hold down the left (main) mouse button and drag to rotate the model.


You can start the application with inital settings (*.vrs) or initial data
files (*.vol).  For example "java -mx128m -jar VolRend.jar cubes64.vol".

The files are:

   In VolRend.zip
	cubes64.vol	A very small test case 
	cubes128.vol    A small test case

   In CThead.zip
	CThead.vol	CT scan of human head, 256x256x113, 16 bit data 
	CThead.vrs	CThead.vol, intensity map, initial view

   In MRbrain.zip
	MRbrain.vol     MR of human head with skull removed, 256x256x109, 16 bit
	MRbrain.vrs     MRbrain.vol, intensity map, initial view
	MRslice.vrs	A 3D texture mapped "slice" through MRbrain.vol

   In Segy.zip
	segy256.vol	Oil and Gas dataset from Segy file, 256x256x256, 8 bit
	segy.vrs	Segy Oil and Gas dataset, segy256.vol

   In SegySmall.zip
	segy128.vol	Smaller version of segy256.vol, 128x128x128, 8 bit
	segySmall.vrs   Smaller version of segy.vrs, segy128.vol

Don't bother with the 256 sized data sets if you can't run cube128.vol very
well, since they are 3-8x bigger.

For optimal speed on an Elite3D on Solaris:

	setenv AFB_IMM_TEXTURE

You may need to increase your heap size (java -mx300m or more) to avoid 
running out of space.






