NAME = twod
DIR = d:/ptII/ptolemy/apps/softwalls/latex/twod
TEX = $(NAME).tex
DVI = $(NAME).dvi
PS = $(NAME).ps
PDF = $(NAME).pdf
AUX = $(NAME).aux
LOG = $(NAME).log
TOC = $(NAME).toc
OUT = $(NAME).out

$(PDF) : $(PS)
	acrodist -n -q $(DIR)/$(PS)

$(PS) : $(DVI)
	dvips $(DVI)

$(DVI) : $(TEX)
	latex $(TEX)
	latex $(TEX)	%Used twice to update labels and table of contents.

clean : 
	rm $(AUX)
	rm $(LOG)
	rm $(OUT)
	#rm $(TOC)
	rm $(DVI)
	rm $(PS)
	rm $(PDF)

cleanmess :
	rm $(AUX)
	rm $(LOG)
	rm $(OUT)
	#rm $(TOC)
	rm $(DVI)
	rm $(PS)

cleanpdf :
	rm $(PDF)

edit :
	emacs $(TEX) &

make : Makefile
	emacs Makefile &

view : $(PDF)
	acrobat $(PDF) &

quickview :
	acrobat $(PDF) &

viewdvi : $(DVI)
	yap $(DVI) &

viewps : $(PS)
	gsview32 $(PS) &

viewpdf : $(PDF)
	acrobat $(PDF) &



