Distiller
---------
To create a PDF File
1. Open the book file
2. Select File->Generate/Update
3. After generating the toc and idx, edit the idx and
   move the first index entry for the UML symbol - to below the header.
4. Select File->Print
   Select 'Print Only to File' and select a file ending in .ps
   Select Distiller Assistant v3.01 as your printer
   Print the file, which will create a PostScript file   

Acrobat distiller has an option that by default "downsamples
images to 72 dpi."  This seems dumb.  Turning this off fixes
the printing problems with chapter 3, with no noticable increase
in file size.  See the file ~eal/TMP/design.pdf.  I've also checked
in some minor fixes in the domain chapters, and regenerated everything.
The pdf file in ~eal/TMP is the latest version.

BTW, in Distiller,
If you also turn off compression of images, the file size increases
from about 3.7M to about 8M.  So don't do that!

Figures
-------
Having the figures at the bottom of the page is right.
If you put them below the point where they are referred to,
Frame leaves orphaned lines below or above them.  The layout
ends up being far worse.

Converting to html
------------------
http://ptolemy/~cxh/sa/quadralay.html covers the details, but basically
1. As yourself, do xhost carson
2. Become ptuser on carson and execute the following commands
   setenv DISPLAY thesun:0
   xrdb -remove
   setenv QUADRALAYHOME /usr/tools/tools2/www/quadralay
   set path = ($path $QUADRALAYHOME/bi)n
   cd ~ptII/doc/design/src
   wpublish
3. Then open up design.wdt.     
