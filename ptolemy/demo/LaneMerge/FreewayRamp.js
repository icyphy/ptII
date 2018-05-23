/** Model of a freeway entrance ramp that provides a roadside service
 *  that suggests a ramp speed that will result in smooth joining on the
 *  freeway. This model accepts input events at the freewayCar port that
 *  represent cars on the freeway approaching the ramp entrance. It also
 *  accepts input events at the rampCar port, and in response to those
 *  events, provides a suggestedRampSpeed output. These events represent
 *  cars approaching the entrance on the ramp. Finally, it accepts
 *  events at the merge port, representing cars on the ramp that have
 *  arrived at the ramp entrance. In response to these events, it waits
 *  until the car can safely enter the freeway, and at that time,
 *  produces a mergeTime output indicating the total amount of time that
 *  the car had to wait between arriving at the entrance and entering the
 *  freeway.
 *  
 *  This model makes a number of unrealistic simplications. First, the
 *  speed of cars on the freeway is fixed and independent of congestion.
 *  Second, It assumes that cars on the ramp accelerate instantaneously
 *  to the suggested speed and, once reaching the ramp entrace, can
 *  accelerate instantaneously to freeway speed.
 *  
 *  For much better freeway traffic models, see <i>Freeway Traffic Modeling
 *  and Control</i>, by Antonella Ferrara, Simona Sacone, and Silvia Siri,
 *  Springer, 2018.
 *  
 *  @accessor FreewayRamp
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @parameter {number} sensorDistanceToMerge The distance in kilometers
 *   between a car sensor on the freeway and the ramp entrance. This is
 *   a number that defaults to 2.0.
 *  @parameter {number} freewaySpeed The speed of cars on the freeway in
 *   kilometers per hour.
 *  @parameter {number} rampLength The distance between the sensor that
 *   sees cars at the rampCar position and the freeway entrance (in
 *   kilometers). This is a number that defaults to 0.5
 *  @parameter {number} maxRampSpeed The maximum speed on the ramp in
 *   kilometers per hour. This is a number that defaults to 60.
 *  @parameter {number} minSpacing The minimum spacing between cars
 *   for a car to safely enter the freeway in kilometers. This is a
 *   number that defaults to 0.05.
 *   
 *  @input freewayCar An event indicating that a car on the freeway 
 *   is approaching the ramp entrace. At the time of this event, the car
 *   is distance sensorDistanceToMerge prior to the merge.
 *  @input rampCar An event indicating that a car on the ramp 
 *   is approaching the ramp entrace. At the time of this event, the car
 *   is distance rampLength prior to the merge.
 *  @input merge An event indicating that a car on the ramp 
 *   has arrived at the ramp entrace. At the time of this event, the car
 *   is exactly at the merge.
 *  
 *  @output {number} output The output for the doubled value.
 *  @output {number}
 */
exports.setup = function() {
	this.parameter('sensorDistanceToMerge', {'type':'number', 'value':2.0});
	this.parameter('freewaySpeed', {'type':'number', 'value':120.0});
	this.parameter('rampLength', {'type':'number', 'value':0.5});
	this.parameter('maxRampSpeed', {'type':'number', 'value':60.0});
	this.parameter('minSpacing', {'type':'number', 'value':0.05});
	
	this.input('freewayCar');
	this.input('rampCar');
	this.input('merge');
	
	this.output('suggestedRampSpeed', {'type':'number'});
	this.output('mergeTime', {'type':'number'});
}

exports.initialize = function() {
	var thiz = this;
	var blocked = [];
	
	this.addInputHandler('freewayCar', function() {
		var distance = thiz.getParameter('sensorDistanceToMerge');
		var speed = thiz.getParameter('freewaySpeed');
		var minSpacing = thiz.getParameter('minSpacing');
		
		var time = currentTime();
		
		var tmin = time + 3600 * (distance - minSpacing)/speed;
		var tmax = time + 3600 * (distance + minSpacing)/speed;
		blocked.push([tmin, tmax]);
		
		// When car is no longer relevant, remove its blocked interval.
		setTimeout(function() {
			blocked.shift();
		}, 1000 * tmax);
	});
	
	this.addInputHandler('rampCar', function() {
		var time = currentTime();
		var minSpacing = thiz.getParameter('minSpacing');
		var rampLength = thiz.getParameter('rampLength');
		var maxRampSpeed = thiz.getParameter('maxRampSpeed');
		
		var suggestedRampSpeed = maxRampSpeed;
		var arrivalTime = time + 3600 * rampLength / suggestedRampSpeed;
		
		for (var i = 0; i < blocked.length; i++) {
			if (blocked[i][0] < arrivalTime && blocked[i][1] > arrivalTime) {
				// Ramp will be blocked at current speed.
				suggestedRampSpeed = 3600 * rampLength/(blocked[i][1] - time);
			}
		}
		thiz.send('suggestedRampSpeed', suggestedRampSpeed);
	});
	
	// Queue of cars waiting to enter the freeway. This queue
	// contains the arrival time for each car.
	var waiting = [];
	var lastMergeTime = -Infinity;
	
	// Function to enter the freeway. If the freeway is not currently
	// blocked, this function allows a car to enter the freeway and
	// produces a mergeTime output. Otherwise, it continues to wait.
	// If a car is allowed to enter the freeway and there are one or
	// more additional cars waiting, then it waits a safe amount of
	// time before trying to enter again.
	function enter() {
		var arrivalTime = waiting[0];
		var time = currentTime();
		
		var minSpacing = thiz.getParameter('minSpacing');
		var speed = thiz.getParameter('freewaySpeed');
		var safeWaitingTime = 3600 * minSpacing / speed;
		
		// Check to see whether the most recent merge was too recent
		// (within clock resolution of 1ms).
		var wait = lastMergeTime + safeWaitingTime - time;
		if (wait >= 0.001) {
		    // Last merge was too recent. Wait.
		    setTimeout(enter, 1000 * wait);
		    return;
		}
				
		// Check to see whether the ramp is blocked.
		var timeToWait = 0.0;
		for (var i = 0; i < blocked.length; i++) {
			if (blocked[i][0] < time && blocked[i][1] > time) {
				// Ramp is blocked.
				timeToWait = blocked[i][1] - time;
			}
		}
		// Timing here has a resolution of ms, so if the timeToWait is
		// small enough that setTimeout() won't result in any delay, then
		// set it to zero.
		if (timeToWait < 0.001) {
			timeToWait = 0.0;
		}
		// console.log('At time ' + time + ', wait for ' + timeToWait);
		if (timeToWait <= 0.0) {
			thiz.send('mergeTime', time - arrivalTime);
			lastMergeTime = time;
			// Discard the car from the queue.
			waiting.shift();
			if (waiting.length > 0) {
				// There are additional cars waiting.
				// Try again after safe waiting time.
				setTimeout(enter, 1000 * safeWaitingTime);
			}
		} else {
			setTimeout(enter, 1000 * timeToWait);
		}
	}
	
	this.addInputHandler('merge', function() {
		var time = currentTime();
		waiting.push(time);
		if (waiting.length == 1) {
			// This is the only car waiting, so trigger a wait.
			// Otherwise, assume it will be triggered after the
			// currently waiting car enters the freeway.
			enter();
		}
	});
}
