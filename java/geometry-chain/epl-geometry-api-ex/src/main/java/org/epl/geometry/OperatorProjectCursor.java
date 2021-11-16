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
 * Created by davidraleigh on 5/12/17.
 */
public class OperatorProjectCursor extends GeometryCursor {
    ProjectionTransformation m_projectionTransformation;
    ProgressTracker m_progressTracker;

    OperatorProjectCursor(
            GeometryCursor inputGeoms,
            ProjectionTransformation projectionTransformation,
            ProgressTracker progressTracker) {
        m_inputGeoms = inputGeoms;
        m_projectionTransformation = projectionTransformation;
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        if (m_inputGeoms.hasNext()) {
            Geometry geometry = m_inputGeoms.next();
            return Projector.project(geometry, m_projectionTransformation, m_progressTracker);
        }
        return null;
    }
}
