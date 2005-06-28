/* 
 * audioPlayer.c converts some random sample data from type double to
 * 16-bit unsigned int. Then it puts the 16-bit sample into a buffer, while
 * SDL constantly clear the buffer by mixing the sample data in the buffer 
 * into the audio stream. No separate thread is created in this process.
 * However, a semaphore is used to avoid overfilling the buffer. Also, 
 * we have to avoid putting sample and mixing simultaneously by calling 
 * SDL_LockAudio() and SDL_UnlockAudio() to prevent the callback function 
 * interferring.
 * 
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "SDL.h"
#include "SDL_audio.h"
#include "SDL_thread.h"


/**
 * @author Man-Kit Leung
 * @version $Id$
 */

#define NUM_SOUNDS 2    // mono or stereo

// FIXME: how should we determine the buffer size??
#define BUFFER_SIZE 65500   // ~50 KB buffer size

#define _debug 0

struct sample {
    Uint8 *data;
    Uint32 dpos;
    Uint32 dlen;
} sounds[NUM_SOUNDS];


SDL_sem *sem;
SDL_mutex *mut;
SDL_AudioSpec fmt;


Uint16 convertSample(double sample) {
    return  (Uint16) floor(sample * 32767); //2^15-1
}


void mixaudio(void *unused, Uint8 *stream, int len)
{
    int i;
    int _resetSize = 0;
    Uint32 amount;

    if (_debug) {
        fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", sounds[0].dlen, len);
    }

    // mixing sound data into output stream
    for ( i=0; i<NUM_SOUNDS; i++ ) {
        while (sounds[i].dlen > sounds[i].dpos) {
            amount = (sounds[i].dlen-sounds[i].dpos);
            if ( amount > len ) {
                amount = len;
            }
            SDL_MixAudio(stream, &sounds[i].data[sounds[i].dpos], amount, SDL_MIX_MAXVOLUME);
            sounds[i].dpos += amount;
        }
    }
    // reset DPOS, so mixing would start from the beginning of the buffer next time 
    // reset DLEN, so we would put sample from the beginning
    for ( i=0; i<NUM_SOUNDS; i++ ) {
        _resetSize += sounds[i].dlen;
        sounds[i].dlen = 0;
        sounds[i].dpos = 0;
    }

    ///////////// UNLOCK //////////////////////////////
    
    for (i=0; i<_resetSize/2; i++) {
        if (SDL_SemPost(sem) == -1) {
            fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
            exit(1);
        }
    }
    ///////////// UNLOCK //////////////////////////////
}

putSample (Uint16 data, int channel) {
    int i;
    if (_debug) 
        fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue(sem));
    /////////////// LOCK //////////////////////////////
    if (SDL_SemWait(sem) != 0) {
        fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
        exit(1);
    }
    /////////////// LOCK //////////////////////////////
    SDL_LockAudio();

    sounds[channel].data[sounds[channel].dlen] = (data >> 8);
    if (_debug) {
        fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data >> 8, sounds[channel].data[sounds[channel].dlen]);
    }
    sounds[channel].dlen++;

    sounds[channel].data[sounds[channel].dlen] = data;
    if (_debug) {
        fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, sounds[channel].data[sounds[channel].dlen]);
    }
    sounds[channel].dlen++;

    if (_debug) {
        fprintf(stdout, "putSample unlocked audio\n");
    }

    SDL_UnlockAudio();
}




main(int argc, char *argv[])
{
    /*************** initialize ************************/   
    int* data;
    int i, j;
    double _sample;
    
    if ((sem = SDL_CreateSemaphore(BUFFER_SIZE/2)) == NULL) {
        fprintf(stderr, "Error creating semaphor: %s\n",SDL_GetError());
        exit(1);
    }

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
    for ( i=0; i<NUM_SOUNDS; ++i ) {
        sounds[i].data = (Uint8*) malloc(BUFFER_SIZE);
        sounds[i].dpos = 0;
        sounds[i].dlen = 0;
    }


    SDL_PauseAudio(0);


    /*************** fire iterations *******************/
    for (i=0; i<1000000; i++) {
        // ** we can feed some other interesting samples, this is just for testing
        for ( j=0; j<NUM_SOUNDS; ++j ) {
            _sample = i/31.3*(j+1);
            putSample (convertSample(_sample), j);
        }
    }

        
    /****************** wrap-up ************************/
    fprintf(stdout, "close audio\n");
    //SDL_PauseAudio(1);
    SDL_CloseAudio();

    fprintf(stdout, "kill semaphore\n");
    if (sem != NULL) {
        SDL_DestroySemaphore(sem);
        sem = NULL;
    }

    fprintf(stdout, "done\n");
}

