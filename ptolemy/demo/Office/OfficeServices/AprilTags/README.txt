This directory has AprilTags as PNG files.
=========================================
https://april.eecs.umich.edu/software/apriltag.html contains
https://april.eecs.umich.edu/media/apriltag/tag36h11.tgz, which is a
set of pregenerated tags as png and PostScript files.  However, these
are of low resolution.  To scale them, use nearest-neighbor interpolation to
avoid blurring.

To get the tags:

  wget https://april.eecs.umich.edu/media/apriltag/tag36h11.tgz
  tar -zxf tag36h11.tgz
  cd tag36h11

Under Mac OS X, ImageMagik can be installed using MacPorts
(https://www.macports.org/)

  sudo port install imagemagick

For example, with ImageMagik, to increase the size of the images use:

  mogrify -scale 400x400 *.png

To create a pdf with all the images:

   convert *.png tag36h11.pdf

To annotate an image with a string: 

   convert tag36_11_00026.png label:'26' -gravity Center -append tag36_11_00026eled.png


Below is a script that will generate a pdf file with labels
--start--
#!/bin/sh
convert tag36_11_00019.png -size 800x label:'AprilTag 19: Sound' -gravity Center -append tag36_11_00019_labeled.png
convert tag36_11_00020.png -size 800x label:'AprilTag 20: Robot' -gravity Center -append tag36_11_00020_labeled.png
convert tag36_11_00021.png -size 800x label:'AprilTag 21: Lights' -gravity Center -append tag36_11_00021_labeled.png

# Create a pdf with one page per tag
convert *labeled.png tag36h11-19-29.pdf 

# Create one page with 11 tiles
montage *labeled.png -geometry '1000x1200>+4+3' -tile 3x4 tag36h11-19-29-montage.png
--end--
