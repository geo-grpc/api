/*
Copyright 2017-2020 Echo Park Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

For additional information, contact:

email: davidraleigh@gmail.com
*/

package org.epl.geometry;

public class InverseResult {
	double az12_rad = 0;
	double az21_rad = 0;
	double distance_m = 0;

	public InverseResult(double az12_rad, double az21_rad, double distance_m) {
		this.az12_rad = az12_rad;
		this.az21_rad = az21_rad;
		this.distance_m = distance_m;
	}


	public double getAz12_rad() {
		return az12_rad;
	}

	public void setAz12_rad(double az12_rad) {
		this.az12_rad = az12_rad;
	}

	public double getAz21_rad() {
		return az21_rad;
	}

	public void setAz21_rad(double az21_rad) {
		this.az21_rad = az21_rad;
	}

	public double getDistance_m() {
		return distance_m;
	}

	public void setDistance_m(double distance_m) {
		this.distance_m = distance_m;
	}

}
