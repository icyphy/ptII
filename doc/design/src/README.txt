Distiller
---------
To create a PDF File
1. Open the book file
2. Select Edit -> Update Book (In frame 5.5, this was File->Generate/Update)
3. After generating the toc and idx, edit the idx and
   move the first index entry for the UML symbol - to below the header.
   Open up usingVergil.fm and remove the cross-reference target
   in the chapter title.  If you do not, then when you generate pdf,
   the TOC in the pdf file will say '2 2 Using Vergil' instead
   of '2 Using Vergil'
4. Select File->Print Book
   Select 'Print Only to File' and select a file ending in .ps
      The reason we print to a file is so that we can later 
      tweak the parameters in Distiller
   Select 'Generate Acrobat Data'
   Hit the "PDF Setup" button, then turn off
       'Create Named Destinations for All Paragraphs"

   Select Distiller Assistant v3.01 as your printer
   Print the file, which will create a 36Mb PostScript file

5. Start up Acrobat Distiller from 
   Start->Programs->Adobe Acrobat 4.0 -> Distiller
   Check out the settings
	 Job Options: PrintOptimized
	 Compatibility: Acrobat 4.0 

In Acrobat distiller, if you select Screen Optimized, then by default
images are downsampled to 72 dpi.  This seems dumb.  Turning this off
fixes the printing problems with chapter 3, with no noticable increase
in file size.

BTW, in Distiller, If you also turn off compression of images, the
file size increases by about 2x.  So don't do that!

Then open the Postscript file with File->Open

Select a location to save the pdf file.  Note that we
check a small pdf version of the Design Doc into
doc/design/design.pdf, so be sure not to overwrite that
version and then commit the new file - We want the 
doc/design/design.pdf, to be a small one page file


In Distiller, if you select Job Options: Print Optimized, then you
will see messages:

  %%[ Warning: Helvetica not found, using Font Substitution. Font cannot
  be embedded.]%%
  %%[ Warning: Helvetica-Bold not found, using Font Substitution. Font
  cannot be embedded.]%%

These messages can probably be ignored.

Sizes for Ptolemy II 2.0.1 design doc, Frame 6, Acrobat Distiller 4.0.5:

36Mb PS, Distiller, with Links, Print Optimized, Acrobat 4.0 compat:  12.1Mb
36Mb PS, Distiller, w/o Links, Print Optimized, Acrobat 3.0 compat:   11.6Mb
36Mb PS, Distiller, w/o Links, Print Optimized, Acrobat 4.0 compat:   11.6Mb
19Mb PS, HP 5Si/MX, w/o Links, Print Optimized, Acrobat 4.0 compat:   11.5Mb
  1200 dpi, downsampling bicubic/300 for color and greyscale

19Mb PS, HP 5Si/MX, w/o Links, Screen Optimized, Acrobat 4.0 compat:   5.5Mb
  600 dpi

19Mb PS, HP 5Si/MX, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, Changed downsampling from Average/72 to bixxx/300
  for color and greyscale images				       5.6Mb

19.0Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, Changed downsampling from Average/72 to bicubic/300
  for color and greyscale images				       5.6Mb

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, Changed downsampling from Average/72 to bicubic/300
  for color and greyscale images				       5.678Mb

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, downsampling: average/300 for color and greyscale images    5.678Mb

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, downsampling: average/300 for color and greyscale images    5.685Mb
  color and greyscale image quality changed from medium to high

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, downsampling: average/300 for color and greyscale images    5.815Mb
  maximum color and greyscale image quality

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, downsampling: average/300 for color and greyscale images    5.815Mb
  maximum color and greyscale image quality
  resampling off

36Mb PS, Distiller, w/o Links, Screen Optimized, Acrobat 4.0 compat:
  600 dpi, downsampling: average/300 for color and greyscale images    5.677Mb
  medium color and greyscale image quality
  resampling off

Note that we also include separate pdfs of the Plot and Using Vergil chapters.
$PTII/ptolemy/plot/doc/plot.pdf
$PTII/doc/design/usingVergil/usingVergil.pdf
The procedure for generating these files is similar to printing
the entire book

An html version of the UsingVergil chapter can be found at 
$PTII/doc/design/usingVergil/index.htm
To generate this file, do


Figures
-------
Having the figures at the bottom of the page is right.
If you put them below the point where they are referred to,
Frame leaves orphaned lines below or above them.  The layout
ends up being far worse.

Screenshots
-----------
To capture a screenshot, use Snagit to save the image as
a gif, then open the image with Paint copy all of the image into
the clipboard and then use Framemaker import special to import
it as a Windows Meta File (WMF).  

More specifically:

1. We have a few licenses for Snagit
http://www.techsmith.com/products/snagit/index.htm
Snagit is probably installed on the lab machines.
2. Start up Snagit and set the input to "Window" and
you probably don't want to include the cursor.
Set the output to gif.
3. Bring up the window you want to capture
4. Hit Control-Shift-P and click in the window you want to capture
5. Snagit capture preview should come up, do File-> Save As
and save the file as a gif or bmp.  Do not save as a jpg, which is lossy.
6. Open the file you save in Microsoft Paint by going
to the file right clicking and selecting Open with -> Microsoft Paint
7. Copy the file into the clipboard by going to Microsoft Paint
and selecting Edit -> Select all and then Edit -> Copy
8. Go to Framemaker and select the graphic frame where you would
like to place the image.  In framemaker, graphic frames can be created 
inside an anchor frame by doing Graphics -> Tools and then selecting
the icon that has a partial circle, a square and a triangle.
9. In Framemaker, do Edit - Paste Special, and select Metafile


Index entries
-------------
The top (T) is a text symbol for what is usually an index entry
the bottom (upside down T) is for a cross reference.

If you double click on a word that has a T so that it is highlighted
and then go to Special -> Marker, the Marker index will come up.
most of the Markers are of type index.

Keywords such as actor names should have index entries.  To create 
an index entry, double click on the word to highlight it, then go
to Special -> Marker, select a Marker Type of Index, type in the 
Marker Text, usually something like 'Ramp actor', then hit
Edit Marker

Usually, the cross references for are created automatically for the
figures, so one does not spend much effort working on figures. 


Converting to html
------------------
http://ptolemy/~cxh/sa/quadralay.html covers the details, but basically
1. As yourself, do xhost carson
2. Become ptuser on carson and execute the following commands
   setenv DISPLAY thesun:0
   xrdb -remove
   setenv QUADRALAYHOME /usr/tools/tools2/www/quadralay
   set path = ($path $QUADRALAYHOME/bin)
   cd ~ptII/doc/design/src
3. start maker, then
   open up design.book and update the cross references

   If you run into font problems, open up a frame file
   choose File -> Preferences -> Deselect Remember Missing Font Names
   then click Set.  Then open up each file and save it with the new fonts.

4. start wpublish, then open up design.wdt.


Visio
-----
    See ptII/doc/uml/README.txt for details
    Common mistakes:
      + No private fields, no methods from parent classes
      + When all done, generate wmf files, Don't import by reference.
      

Hardcopy
--------
     double sided
     plastic spiral bound
     clear plastic front cover, solid color back cover
     Front page to be color, the rest is black and white
