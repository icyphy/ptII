#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>

#include "config.h"
#include "fish.h"

FILE *output_fp; /* Pointer to output file */
int outputp;

int n_fish = 10; /* Other defaults in fish-lib.c. */
fish_t *fish; /* Global array of fish */

struct Timer total_timer;

void init_fish();
void interact_fish(fish_t* fish_, const int n_fish);
void bounce_fish (fish_t* fish);
void move_fish (fish_t* fish, const int n_fish, const double dt);
double compute_norm (const fish_t* fish, const int n_fish);
void output_fish(FILE* output_fp, const double t, const double dt,
		 const fish_t* fish, const int n_fish);

int
main(int argc, char **argv)
{
  double curr_time;
  double output_time;
  double dt = 0.0;
  double max_norm;
  int steps;

  get_options(argc, argv);
  srand48(clock());

  if (output_filename) {
    outputp = 1;
    output_fp = fopen(output_filename, "w");
    if (output_fp == NULL) {
      printf("Could not open %s for output\n", output_filename);
      exit(1);
    }
    fprintf(output_fp, "n_fish: %d\n", n_fish);
  }

  init_fish();

  initialize_timer (&total_timer);
  start_timer(&total_timer);

  for (output_time = 0.0, curr_time = 0.0, steps = 0;
       curr_time <= end_time && steps < max_steps;
       curr_time += dt, ++steps) {

    /* We only output once every output_interval time unit, at most.
     * Without that restriction, we can easily create a huge output
     * file.  Printing a record for ten fish takes about 300 bytes,
     * so for every 1000 steps, we could dump 300K of info.  Now
     * scale the number of fish by 1000...
     */
    if (outputp && curr_time >= output_time) {
      output_fish(output_fp, curr_time, dt, fish, n_fish);
      output_time = curr_time + output_interval;
    }

    interact_fish(fish, n_fish);

    max_norm = compute_norm(fish, n_fish);
    dt = max_norm_change / max_norm;
    dt = f_max(dt, min_dt);
    dt = f_min(dt, max_dt);

    move_fish(fish, n_fish, dt);
  }

  stop_timer(&total_timer);

  if (outputp) {
    output_fish(output_fp, curr_time, dt, fish, n_fish);
    printf("\tEnded at %g (%g), %d (%d) steps\n",
	   curr_time, end_time, steps, max_steps);
  }

  printf("Total time taken: %g\n", timer_duration(total_timer));

  return 0;
}


/* Compute the global maximum of all velocities / accelerations.  */
double
compute_norm (const fish_t* fish, const int n_fish)
{
  int i;
  double max_norm = 0.0;

  for (i = 0; i < n_fish; ++i) {
    max_norm = f_max (max_norm, fabs(fish[i].vx));
    max_norm = f_max (max_norm, fabs(fish[i].vy));
    max_norm = f_max (max_norm, fabs(fish[i].ax));
    max_norm = f_max (max_norm, fabs(fish[i].ay));
  }

  return max_norm;
}


/* Compute the accelerations (force/mass) for each fish */
void
interact_fish(fish_t* fish_, const int n_fish)
{
  fish_t* restrict fish = fish_;
  int i, j;

  for (i = 0; i < n_fish; ++i) {

    fish[i].ax = 0.0;
    fish[i].ay = 0.0;

    for (j = 0; j < n_fish; ++j) {
      double dx, dy;

      dx = fish[j].x - fish[i].x;
      dy = fish[j].y - fish[i].y;

      if (dx != 0 || dy != 0) {
	double r2, r, a;

	r2 = f_max(dx * dx + dy * dy, min_r2);
	r = sqrt(r2);
	a = G * FISH_MASS / r2;

	fish[i].ax += a * dx / r;
	fish[i].ay += a * dy / r;
      }
    }
  }
}


/* Allocate and initialize the fish positions / velocities / accelerations. */
void
init_fish()
{
  int i;
  fish = (fish_t *) malloc(n_fish * sizeof(fish_t));
  for (i = 0; i < n_fish; ++i) {
    if (uniformp) {
      fish[i].x = unscale_coord(drand48());
      fish[i].y = unscale_coord(drand48());
    } else {
      const double angle = i * (2.0 * M_PI / n_fish);
      fish[i].x = unscale_coord(0.5 * cos(angle) + 0.5);
      fish[i].y = unscale_coord(0.5 * sin(angle) + 0.5);
    }
    fish[i].vx = fish[i].vy = fish[i].ax = fish[i].ay = 0.0;
  }
}


/* Apply reflective boundary conditions (fish bounce off walls). */
void
bounce_fish (fish_t* fish)
{
  while (fish->x < LEFT_WALL || fish->x > RIGHT_WALL) {
    if (fish->x < LEFT_WALL) {
      fish->x = 2.0 * LEFT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
    if (fish->x > RIGHT_WALL) {
      fish->x = 2.0 * RIGHT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
  }
  while (fish->y < LEFT_WALL || fish->y > RIGHT_WALL) {
    if (fish->y < LEFT_WALL) {
      fish->y = 2.0 * LEFT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
    if (fish->y > RIGHT_WALL) {
      fish->y = 2.0 * RIGHT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
  }
}


/* Actually move the fish. */
void
move_fish(fish_t* fish, const int n_fish, const double dt)
{
  int i;
  for (i = 0; i < n_fish; ++i) {
    fish[i].x += dt * fish[i].vx;
    fish[i].y += dt * fish[i].vy;
    fish[i].vx += dt * fish[i].ax;
    fish[i].vy += dt * fish[i].ay;
    bounce_fish(&fish[i]);
  }
}


/*
  Dump out all the fishies (and their center of gravity)
  in a format that the viewer understands.
*/
void
output_fish(FILE* output_fp, const double t, const double dt,
	    const fish_t* fish, const int n_fish)
{
  int i;
  double cg_x = 0.0;
  double cg_y = 0.0;

  fprintf(output_fp, "%.5g (%.5g):\n", t, dt);
  for (i = 0; i < n_fish; ++i) {
    cg_x += fish[i].x;
    cg_y += fish[i].y;
    fprintf(output_fp, "  %d: (%g, %g)\n", i,
	    scale_coord(fish[i].x), scale_coord(fish[i].y));
  }
  cg_x /= n_fish;
  cg_y /= n_fish;
  fprintf(output_fp, "  cg: (%g, %g)\n", scale_coord(cg_x),
	  scale_coord(cg_y));
}


