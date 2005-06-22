
/* Program to load a wave file and loop playing it using SDL sound */

/* loopwaves.c is much more robust in handling WAVE files -- 
	This is only for simple WAVEs
*/

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

#include "SDL.h"
#include "SDL_audio.h"
#include "SDL_thread.h"


#define USE_CALLBACK 0
#define NUM_SOUNDS 100


struct sample {
    Uint8 *data;
    Uint32 dpos;
    Uint32 dlen;
} sounds[NUM_SOUNDS];

int g_index;
int done;

SDL_sem *sem;
SDL_mutex *mut;
SDL_AudioCVT cvt;
SDL_AudioSpec fmt;

putSample (short data) {
	fprintf(stderr, "putSample running ( %d )\n", data);

	SDL_AudioCVT cvt;
	int l_index;
	int i;

	/////////////// LOCK //////////////////////////////
	if (SDL_SemWait(sem) == -1) {
		fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
		exit(1);
	}
	/////////////// LOCK //////////////////////////////


	/* Look for an empty (or finished) sound slot */
	fprintf(stderr, "putSample looking for empty slot\n");

	for ( l_index=0; l_index<NUM_SOUNDS; ++l_index ) {
		if ( sounds[l_index].dpos == sounds[l_index].dlen ) {
			break;
		}
	}
	fprintf(stderr, "putSample finishes looking\n");

	if ( l_index == NUM_SOUNDS ) {
		// FIXME: use semaphore
		SDL_Delay(1000);
	}

	fprintf(stderr, "putSample convert audio\n");

	SDL_BuildAudioCVT(&cvt, AUDIO_S16, 2, 22050, AUDIO_S16,2,22050);
	cvt.buf = malloc(16*cvt.len_mult);
	memcpy(cvt.buf, &data, 16);
	cvt.len = 16;
	SDL_ConvertAudio(&cvt);

	fprintf(stderr, "putSample finishes convert audio\n");

    /* Put the sound data in the slot (it starts playing immediately) */
	fprintf(stderr, "putSample going to lock audio\n");

	SDL_LockAudio();

	fprintf(stderr, "putSample locked audio\n");
    sounds[l_index].data = cvt.buf;
    sounds[l_index].dlen = cvt.len_cvt;
    sounds[l_index].dpos = 0;
	fprintf(stderr, "putSample going to unlock\n");
    SDL_UnlockAudio();

	fprintf(stderr, "putSample exit\n");
}


void mixaudio(void *unused, Uint8 *stream, int len)
{
	if(SDL_mutexP(mut)==-1){
		fprintf(stderr, "Couldn't lock mutex\n");
		exit(-1);
	}

	fprintf(stderr, "*******************************mixaudio running (%d)\n", g_index);

	SDL_MixAudio(stream, &sounds[g_index].data[sounds[g_index].dpos], 1, SDL_MIX_MAXVOLUME);

	sounds[g_index].dpos = sounds[g_index].dlen;

	g_index++;
	g_index %= NUM_SOUNDS;
	///////////// LOCK //////////////////////////////
	if (SDL_SemPost(sem) == -1) {
		fprintf(stderr, "Error unlocking semaphor: %s\n",SDL_GetError());
		exit(1);
	}
	///////////// LOCK //////////////////////////////

	fprintf(stderr, "*******************************mixaudio exit\n");

	if(SDL_mutexV(mut)==-1){
		fprintf(stderr, "Couldn't unlock mutex\n");
		exit(-1);
	}
}


int fireAudio (void *data) {

	fprintf(stderr, "fireAudio running\n");

    /* Set 16-bit stereo audio at 22Khz */
    fmt.freq = 22050;
    fmt.format = AUDIO_S16;
    fmt.channels = 2;
    fmt.samples = 512;        /* A good value for games */
    fmt.callback = mixaudio;
    fmt.userdata = NULL;

    /* Open the audio device and start playing sound! */
    if ( SDL_OpenAudio(&fmt, NULL) < 0 ) {
        fprintf(stderr, "Unable to open audio: %s\n", SDL_GetError());
        exit(1);
    }
    SDL_PauseAudio(0);
	while ( done == 0 ) {
		// **have really short delays for the mixing thread
		SDL_Delay(1);	
	}

	fprintf(stderr, "closing audio\n");

	SDL_CloseAudio();
	return 0;
}






int main(int argc, char *argv[])
{
	/*************** initialize ************************/
	done = 0;
	g_index = 0;
	
	mut=SDL_CreateMutex();
	if ((sem = SDL_CreateSemaphore(NUM_SOUNDS)) == NULL) {
        fprintf(stderr, "Error creating semaphor: %s\n",SDL_GetError());
		exit(1);
	}
	fprintf(stderr, "main running\n");


	SDL_Thread *fireAudioThread = SDL_CreateThread(fireAudio, NULL);

	int* data;
	int i;

	/*************** fire iterations *******************/
	for (i=0; i<100000000; i++) {
		putSample (i);			//((i%300)*11);
		SDL_Delay(50);
	}

		
	/****************** wrap-up ************************/
	done = 1;
	SDL_WaitThread (fireAudioThread, NULL);

	SDL_DestroyMutex(mut);
	if (sem != NULL) {
		SDL_DestroySemaphore(sem);
		sem = NULL;
	}
	fprintf(stderr, "main finishes\n");
	return 0;
}

