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

import com.esri.core.geometry.Point2D;

public class Point2DEx {
	public static double calculateTriangleArea2D(Point2D pt1, Point2D pt2, Point2D pt3) {
		double a = Point2D.distance(pt1, pt2);
		double b = Point2D.distance(pt1, pt3);
		double c = Point2D.distance(pt2, pt3);
		double temp;


		if (a < b) {
			// set a >= b
			temp = b;
			b = a;
			a = temp;
		}

		if (c > a) {
			// set a >= c
			temp = c;
			c = a;
			a = temp;
		}

		if (c > b) {
			temp = c;
			c = b;
			b = temp;
		}

		//		First sort a, b, c so that a ≥ b ≥ c ; this can be done at the cost of at most three comparisons.
		//				If c-(a-b) < 0 then the data are not side-lengths of a real triangle
		if (c - (a - b) < 0)
			return 0.0;

		//double result = 0.5 * Math.abs((x - pt2.x)*(pt1.y - y) - (x - pt1.x)*(pt2.y - y));
		return Math.sqrt((a + (b + c)) * (c - (a - b)) * (c + (a - b)) * (a + (b - c))) / 4.0;

	}

}
