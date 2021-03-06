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

class OperatorBufferExCursor extends GeometryCursor {
    OperatorBufferExCursor(GeometryCursor inputGeoms,
                           SpatialReferenceEx srEx,
                           double[] distances,
                           double max_deviation,
                           int max_vertices,
                           boolean b_union,
                           ProgressTracker progress_tracker) {
        SpatialReference sr = null;
        if (srEx != null)
            sr = srEx.toSpatialReference();

        m_inputGeoms = ((OperatorBuffer) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Buffer)).
                execute(inputGeoms, sr, distances, max_deviation, max_vertices, b_union, progress_tracker);
    }

    @Override
    public Geometry next() {
        return m_inputGeoms.next();
    }
}
