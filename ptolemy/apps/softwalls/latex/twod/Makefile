NAME = twod
TEX = $(NAME).tex
DVI = $(NAME).dvi
PDF = $(NAME).pdf
AUX = $(NAME).aux
LOG = $(NAME).log
TOC = $(NAME).toc
OUT = $(NAME).out

all : $(DVI)
	dvipdfm $(DVI)

$(PDF) : $(DVI)
	dvipdfm $(DVI)

$(DVI) : $(TEX)
	latex $(TEX)
	latex $(TEX)	%Used twice to update labels and table of contents.

clean : 
	rm $(AUX)
	rm $(LOG)
	rm $(OUT)
	#rm $(TOC)
	rm $(DVI)
	rm $(PDF)

cleanmess :
	rm $(AUX)
	rm $(LOG)
	rm $(OUT)
	#rm $(TOC)
	rm $(DVI)

cleanpdf :
	rm $(PDF)

edit :
	emacs $(TEX) &

make : Makefile
	emacs Makefile &

view :
	acrobat $(PDF) &

viewdvi : $(DVI)
	yap $(DVI) &

viewps : $(PS)
	gsview32 $(PS) &

viewpdf : $(PDF)
	acrobat $(PDF) &



