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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;

/**
 * Created by davidraleigh on 4/17/16.
 * Generalizes geometries using Visvalingam algorithm
 */
public abstract class OperatorGeneralizeByArea extends OperatorEx {
    @Override
    public OperatorEx.Type getType() {
        return OperatorEx.Type.GeneralizeByArea;
    }

    /**
     * Performs the Generalize operation on a geometry set. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract GeometryCursor execute(GeometryCursor geoms,
                                           double percentReduction,
                                           boolean bRemoveDegenerateParts,
                                           GeneralizeType generalizeType,
                                           SpatialReferenceEx spatialReference,
                                           ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a geometry set. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract GeometryCursor execute(GeometryCursor geoms,
                                           boolean bRemoveDegenerateParts,
                                           int maxPointCount,
                                           GeneralizeType generalizeType,
                                           SpatialReferenceEx spatialReference,
                                           ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom,
                                     double percentReduction,
                                     boolean bRemoveDegenerateParts,
                                     GeneralizeType generalizeType,
                                     SpatialReferenceEx spatialReference,
                                     ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom,
                                     boolean bRemoveDegenerateParts,
                                     int maxPointCount,
                                     GeneralizeType generalizeType,
                                     SpatialReferenceEx spatialReference,
                                     ProgressTracker progressTracker);

    public static OperatorGeneralizeByArea local() {
        return (OperatorGeneralizeByArea) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeneralizeByArea);
    }
}
