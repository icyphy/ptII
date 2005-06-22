audioPlayer.exe: audioPlayer.c
	gcc -I/usr/local/include/SDL -mwindows $^ \
		-L/usr/local/lib -lmingw32 -lSDLmain -lSDL -lSDLmain \
		-mwindows -mno-cygwin -o audioPlayer.exe

loopwave.exe: loopwave.c
	gcc -I/usr/local/include/SDL -mwindows $^ \
		-L/usr/local/lib -lmingw32 -lSDLmain -lSDL -lSDLmain \
		-mwindows -mno-cygwin -o loopwave.exe
