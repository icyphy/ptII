#################################################
## Makefile for Byacc/Java
## Date: 01 Oct 97
## Author: Bob Jamison
#################################################

CC     = gcc
CFLAGS = -O
OBJ 	 = closure.o \
	error.o \
	lalr.o \
	lr0.o \
	main.o \
	mkpar.o \
	output.o \
	reader.o \
	skeleton.o \
	symtab.o \
	verbose.o \
	warshall.o

all: ptbyacc

SRCS   = closure.c \
	error.c \
	lalr.c \
	lr0.c \
	main.c \
	mkpar.c \
	output.c \
	reader.c \
	skeleton.c \
	symtab.c \
	verbose.c \
	warshall.c

# We rename the binary to ptbyacc so as to differentiate
# it from the regular yacc and byacc
ptbyacc: $(OBJ)
	$(CC) -o $@ $(OBJ)
	@echo "Run 'make update_lang_java' to update the files in"
	@echo "$(PTII)/ptolemy/lang/java"

# This command will overwrite the following files in
# $PTII/ptolemy/lang/java
# 	JavaParser.java
# 	JavaParserval.java
#	*.tbl
update_lang_java: ptbyacc
	(cd $(PTII)/ptolemy/lang/java; \
		$(PTII)/vendors/misc/byacc/ptbyacc -j \
		-p ptolemy.lang.java -f JavaParser jparser.y)
	@echo "After running this rule, you might want to update"
	@echo "the .bin files in $(PTII)/ptolemy/lang/java"
	@echo "by running"
	@echo "(cd $(PTII)/ptolemy/lang/java; make update_tables)"
	@echo "(cd $(PTII)/ptolemy/lang/java; cvs commit -m \"Modified byacc\")"

clean:
	/bin/rm -rf $(OBJS)

###
closure.o: closure.c defs.h
error.o: error.c defs.h
lalr.o: lalr.c defs.h
lr0.o: lr0.c defs.h
main.o: main.c defs.h
mkpar.o: mkpar.c defs.h
output.o: output.c defs.h
reader.o: reader.c defs.h
skeleton.o: skeleton.c defs.h
symtab.o: symtab.c defs.h
verbose.o: verbose.c defs.h
warshall.o: warshall.c defs.h

