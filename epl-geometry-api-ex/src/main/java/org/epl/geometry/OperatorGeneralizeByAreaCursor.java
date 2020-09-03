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

import java.util.ArrayList;
import java.util.stream.DoubleStream;

/**
 * Created by davidraleigh on 4/17/16.
 */
public class OperatorGeneralizeByAreaCursor extends GeometryCursor {
    ProgressTracker m_progressTracker;
    boolean m_bRemoveDegenerateParts;
    GeneralizeType m_generalizeType;
    double m_percentReduction;
    SpatialReferenceEx m_spatialReference;
    int m_maxPointCount;

    public OperatorGeneralizeByAreaCursor(GeometryCursor geoms,
                                          double percentReduction,
                                          boolean bRemoveDegenerateParts,
                                          GeneralizeType generalizeType,
                                          SpatialReferenceEx spatialReference,
                                          ProgressTracker progressTracker) {
        m_inputGeoms = geoms;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeType = generalizeType;
        m_percentReduction = percentReduction;
        m_spatialReference = spatialReference;

        m_maxPointCount = 0;
    }

    public OperatorGeneralizeByAreaCursor(GeometryCursor geoms,
                                          boolean bRemoveDegenerateParts,
                                          int maxPointCount,
                                          GeneralizeType generalizeType,
                                          SpatialReferenceEx spatialReference,
                                          ProgressTracker progressTracker) {
        m_inputGeoms = geoms;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeType = generalizeType;
        m_spatialReference = spatialReference;

        m_maxPointCount = maxPointCount;
    }

    @Override
    public Geometry next() {
        if (hasNext())
            return GeneralizeArea(m_inputGeoms.next());

        return null;
    }

    private Geometry GeneralizeArea(Geometry geom) {
        Geometry.Type gt = geom.getType();

        if (Geometry.isPoint(gt.value()))
            return geom;

        if (gt == Geometry.Type.Envelope) {
            Polygon poly = new Polygon(geom.getDescription());
            poly.addEnvelope((Envelope) geom, false);
            return GeneralizeArea(poly);
        }

        if (geom.isEmpty())
            return geom;

        if (m_maxPointCount > 0) {
            int pointCount = ((MultiVertexGeometry)geom).getPointCount();
            m_percentReduction = 100 - 100.0 * ((double)m_maxPointCount) / ((double)pointCount);
        }

        EditShape editShape = new EditShape();
        editShape.addGeometry(geom);

        GeneralizeAreaPath(editShape);

        // TODO  this simplify is a cheat. maybe there's a better way for making sure our geometry isn't screwed up.
        return GeometryEngine.simplify(editShape.getGeometry(editShape.getFirstGeometry()), m_spatialReference.toSpatialReference());
    }


    private void GeneralizeAreaPath(EditShape editShape) {

        TreapEx treap = new TreapEx();
        GeneralizeComparator areaComparator = new GeneralizeComparator(editShape, m_generalizeType);
        treap.disableBalancing();
        treap.setComparator(areaComparator);

        // TODO fix this. path removal stuff. It's a messy solution to the whole treap cleanup problem


        for (int iGeometry = editShape.getFirstGeometry(); iGeometry != -1; iGeometry = editShape.getNextGeometry(iGeometry)) {
            for (int iPath = editShape.getFirstPath(iGeometry); iPath != -1; iPath = editShape.getNextPath(iPath)) {
                int n = editShape.getPathSize(iPath);
                treap.setCapacity(n);
                int ptCountToRemove = (int)Math.ceil(n * m_percentReduction / 100.0);

                // if there are points that will remain after removals, then first create the treap
                int iVertex = editShape.getFirstVertex(iPath);
                areaComparator.setPathCount(n * 5);
                for (int i = 0; i < n; iVertex = editShape.getNextVertex(iVertex), i++) {
                    treap.addElement(iVertex, -1);
                }


                while (0 < ptCountToRemove-- && treap.size(-1) > 0) {

                    int vertexNode = treap.getFirst(-1);
                    int vertexElm = treap.getElement(vertexNode);

                    GeneralizeComparator.EditShapeTriangle triangle = areaComparator.tryGetCachedTriangle_(vertexElm);
                    if (triangle == null) {
                        triangle = areaComparator.tryCreateCachedTriangle_(vertexElm);
                        if (triangle == null) {
                            triangle = areaComparator.createTriangle(vertexElm);
                        }
                    }

                    if ((m_generalizeType == GeneralizeType.ResultContainsOriginal && triangle.queryOrientation() < 0) ||
                            (m_generalizeType == GeneralizeType.ResultWithinOriginal && triangle.queryOrientation() > 0)) {
                        break;
                    }


                    if (treap.size(-1) == 1) {
                        treap.deleteNode(vertexNode, -1);
                        editShape.removeVertex(vertexElm, false);
                    } else {
                        int prevElement = triangle.m_prevVertexIndex;
                        int nextElement = triangle.m_nextVertexIndex;

                        int prevNodeIndex = treap.search(prevElement, -1);
                        int nextNodeIndex = treap.search(nextElement, -1);

                        if (prevNodeIndex > -1)
                            treap.deleteNode(prevNodeIndex, -1);
                        if (nextNodeIndex > -1)
                            treap.deleteNode(nextNodeIndex, -1);

                        treap.deleteNode(vertexNode, -1);
                        editShape.removeVertex(vertexElm, false);

                        if (prevNodeIndex > -1)
                            treap.addElement(prevElement, -1);
                        if (nextNodeIndex > -1)
                            treap.addElement(nextElement, -1);
                    }
                }
                treap.clear();
            }
        }
    }
}
