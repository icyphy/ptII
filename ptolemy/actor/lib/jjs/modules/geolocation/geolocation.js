// geolocation module for CapeCode accessors host.
// 
// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//

/**
 *  Module for retrieving geolocation information.
 *
 *  @module geolocation
 *  @author Chadlia Jerad
 *  @version $$Id: geolocation.js 75980 2017-07-18 00:19:25Z chadlia.jerad $$
 */

var httpClient = require('@accessors-modules/httpClient');

var callerOnSuccessFunction;
var callerOnErrorFunction;

exports.getPosition = function(onSuccess, onError, options) {
	callerOnSuccessFunction = onSuccess;
	callerOnErrorFunction = onError;

	httpClient.get('https://ipinfo.io/json', function(response) {
		if (response.statusCode == 200) {
			var location = {};
			var loc = JSON.parse(response.body).loc;
			loc = loc.split(',');
			location.latitude = Number(loc[0]);
			location.latitude = Number(loc[1]);
		   	location.altitudeAccuracy = undefined;  
		   	location.heading = undefined;
		   	location.speed = undefined;
		   	location.timestamp = response.headers.date;
		   	location.error = false;
			onSuccess.call(this, location);
		} else {
			var location = {};
			location.error = true;
			location.errorCode = response.statusCode;
			onError.call(this, location);
		};
	});
}
