## modify the target, or call like: `make TARGET=NewName`

TARGET = HelloWorld

# `make` or `make all`    compiles the $(TARGET).tvm 
# `make auto`             compiles the $(TARGET).tvm, and downloads it
# `make install`          downloads the $(TARGET).tvm file to the RCX
# `make clean`            removes $(TARGET).class, and $(TARGET).tvm
# `make File.tvm`         will compile File.tvm from File.java
# `make inst_firmware`    downloads the firmware to the RCX.

## so that 'tvmld' can find the $(TARGET).class file:
export CLASSPATH=.

all: $(TARGET).tvm
	@echo  Run \'make install\' or 'tvm $@' to download the tvm file

auto: $(TARGET).tvm install 

install:
	tvm $(TARGET).tvm

clean:
	rm -f $(TARGET).class $(TARGET).tvm core

%.tvm: %.java
	@echo Making: $@
	tvmc $< 
	tvmld $* -o $@

inst_firmware:
	tvmfirmdl
