package org.epl.geometry;

import com.esri.core.geometry.*;

import java.util.HashMap;

public class OperatorSimpleRelationEx extends OperatorEx {
	public HashMap<Integer, Boolean> execute(Geometry inputGeom1,
	                                         GeometryCursor geometryCursor2,
	                                         SpatialReference sr,
	                                         ProgressTracker progressTracker,
	                                         Operator.Type operatorType) {
		HashMap<Integer, Boolean> hashMap = new HashMap<>();
		Geometry inputGeom2;
		while ((inputGeom2 = geometryCursor2.next()) != null) {
			int index = geometryCursor2.getGeometryID();
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
