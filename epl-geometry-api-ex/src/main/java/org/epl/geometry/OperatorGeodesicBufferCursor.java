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

/**
 * Created by davidraleigh on 2/20/16.
 */
public class OperatorGeodesicBufferCursor extends GeometryCursor {
    private SpatialReferenceExImpl m_spatialReference;
    private ProgressTracker m_progressTracker;
    private double[] m_distances;
    private double m_maxDeviation;
    private Envelope2D m_currentUnionEnvelope2D;
    private boolean m_bUnion;

    private int m_dindex;

    // GeometryCursor inputGeometries, SpatialReference sr, int curveType, double[] distancesMeters, double maxDeviationMeters, boolean bReserved, boolean bUnion, ProgressTracker progressTracker
    OperatorGeodesicBufferCursor(GeometryCursor inputGeoms,
                                 SpatialReferenceEx sr,
                                 double[] distances,
                                 double maxDeviation,
                                 boolean bReserved,
                                 boolean b_union,
                                 ProgressTracker progressTracker) {
        m_inputGeoms = inputGeoms;
        m_spatialReference = (SpatialReferenceExImpl) sr;
        m_distances = distances;
        m_maxDeviation = maxDeviation;
        m_bUnion = b_union;
        m_currentUnionEnvelope2D = new Envelope2D();
        m_currentUnionEnvelope2D.setEmpty();
        m_dindex = -1;
        m_progressTracker = progressTracker;
    }


    @Override
    public Geometry next() {
        if (m_bUnion) {
            OperatorGeodesicBufferCursor cursor = new OperatorGeodesicBufferCursor(m_inputGeoms, m_spatialReference, m_distances, m_maxDeviation, false, false, m_progressTracker);
            return ((OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Union)).execute(cursor, m_spatialReference.toSpatialReference(), m_progressTracker).next();
        }

        if (hasNext()) {
            if (m_dindex + 1 < m_distances.length)
                m_dindex++;

            return geodesicBuffer(m_inputGeoms.next(), m_distances[m_dindex]);
        }

        return null;
    }

    // virtual bool IsRecycling() OVERRIDE { return false; }
    Geometry geodesicBuffer(Geometry geom, double distance) {
        return GeodesicBufferer.buffer(geom, distance, m_spatialReference, m_maxDeviation, 96, m_progressTracker);
    }
}
