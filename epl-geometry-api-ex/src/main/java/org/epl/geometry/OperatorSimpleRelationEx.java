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

import com.esri.core.geometry.*;

import java.util.HashMap;

public class OperatorSimpleRelationEx extends OperatorEx {
	public HashMap<Long, Boolean> execute(Geometry inputGeom1,
	                                         GeometryCursor geometryCursor2,
	                                         SpatialReference sr,
	                                         ProgressTracker progressTracker,
	                                         Operator.Type operatorType) {
		HashMap<Long, Boolean> hashMap = new HashMap<>();
		Geometry inputGeom2;
		while ((inputGeom2 = geometryCursor2.next()) != null) {
			long index = geometryCursor2.getGeometryID();
			if ((progressTracker != null) && !(progressTracker.progress(-1, -1)))
				throw new RuntimeException("user_canceled");
			Boolean result = ((OperatorSimpleRelation)OperatorFactoryLocal.getInstance().getOperator(operatorType))
					.execute(inputGeom1, inputGeom2, sr,null);
			hashMap.put(index, result);
		}
		return hashMap;
	}

	@Override
	public Type getType() {
		return Type.SimpleRelation;
	}
}
