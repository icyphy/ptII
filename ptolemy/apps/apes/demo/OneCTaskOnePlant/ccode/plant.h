/*
 *  plant.h
 *  
 *
 *  Created by Patricia Derler on 1/26/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */

double step(double lower) {
	lower ++;
	if (lower == 5)
		lower = 0; 
	return lower;
}