Distiller
---------
To create a PDF File
1. Open the book file
2. Select Edit -> Update Book (In frame 5.5, this was File->Generate/Update)
3. After generating the toc and idx, edit the idx and
   move the first index entry for the UML symbol - to below the header.
4. Select File->Print
   Select 'Print Only to File' and select a file ending in .ps
   Select Distiller Assistant v3.01 as your printer
   Print the file, which will create a PostScript file

In Acrobat distiller, if you select Screen Optimized, then by default
images are downsampled to 72 dpi.  This seems dumb.  Turning this off
fixes the printing problems with chapter 3, with no noticable increase
in file size.  See the file ~eal/TMP/design.pdf.  I've also checked in
some minor fixes in the domain chapters, and regenerated everything.
The pdf file in ~eal/TMP is the latest version.

BTW, in Distiller, If you also turn off compression of images, the
file size increases from about 3.7M to about 8M.  So don't do that!

In Distiller, if you select Job Options: Print Optimized, then you
will see messages:

  %%[ Warning: Helvetica not found, using Font Substitution. Font cannot
  be embedded.]%%
  %%[ Warning: Helvetica-Bold not found, using Font Substitution. Font
  cannot be embedded.]%%

These messages can probably be ignored.

Sizes:
Screen Optimized, Acrobat 3.0 compatibility: 4.80Mb
Print Optimized, Acrobat 4.0 compatiblity:   4.96Mb

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
      + No private fields, no methods from parent classes
      + When all done, generate wmf files, Don't import by reference.


Hardcopy
--------
     double sided
     plastic spiral bound
     clear plastic front cover, solid color back cover
     Front page to be color, the rest is black and white
