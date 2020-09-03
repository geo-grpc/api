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

import com.esri.core.geometry.EditShape;
import com.esri.core.geometry.NumberUtils;
import com.esri.core.geometry.Point2D;

import java.util.ArrayList;

/**
 * Created by davidraleigh on 4/19/16.
 */
public class GeneralizeComparator extends TreapEx.Comparator {
    class EditShapeTriangle {
        int m_prevVertexIndex;
        int m_nextVertexIndex;
        int m_vertexIndex;

        private Point2D m_point;
        private Point2D m_prevPoint;
        private Point2D m_nextPoint;
        private double m_area;
        private int m_orientation;


        EditShapeTriangle() {

        }


        EditShapeTriangle(EditShape editShape, int iVertex) {
            setTriangle(editShape, iVertex);
        }


        void setTriangle(EditShape editShape, int iVertex) {
            int prevVertex = editShape.getPrevVertex(iVertex);
            int nextVertex = editShape.getNextVertex(iVertex);
            Point2D prevPoint = editShape.getXY(prevVertex);
            Point2D currentPoint = editShape.getXY(iVertex);
            Point2D nextPoint = editShape.getXY(nextVertex);

            m_point = currentPoint;
            m_prevPoint = prevPoint;
            m_nextPoint = nextPoint;
            m_vertexIndex = iVertex;
            m_prevVertexIndex = prevVertex;
            m_nextVertexIndex = nextVertex;
            updateArea();
            updateOrientation();
        }


        int getIndex() {
            return m_vertexIndex;
        }


        double queryArea() {
            return m_area;
        }


        int queryOrientation() {
            return m_orientation;
        }


        void updateArea() {
            m_area = Point2DEx.calculateTriangleArea2D(m_point, m_nextPoint, m_prevPoint);
        }


        void updateOrientation() {
            m_orientation = Point2D.orientationRobust(m_prevPoint, m_point, m_nextPoint);
        }
    }

    EditShape m_editShape;
    int m_vertex_1 = -1;
    int m_vertex_2 = -1;
    int m_modulus_distribution = 0;
    int m_subDivisions = 8;
    GeneralizeType m_generalizeType;

    EditShapeTriangle m_temp_triangle_1 = null;
    EditShapeTriangle m_temp_triangle_2 = null;

    ArrayList<EditShapeTriangle> m_triangle_nodes_buffer;
    ArrayList<EditShapeTriangle> m_triangle_nodes_recycle;
    ArrayList<EditShapeTriangle> m_triangle_nodes_cache;

    GeneralizeComparator(EditShape editShape, GeneralizeType generalizeType) {
        super(true);
        m_editShape = editShape;

        m_generalizeType = generalizeType;

        m_triangle_nodes_buffer = new ArrayList<EditShapeTriangle>();
        m_triangle_nodes_recycle = new ArrayList<EditShapeTriangle>();
        m_triangle_nodes_cache = new ArrayList<EditShapeTriangle>();

        m_temp_triangle_1 = new EditShapeTriangle();
        m_temp_triangle_2 = new EditShapeTriangle();

        m_modulus_distribution = 0;

        int s = Math.min(editShape.getTotalPointCount() * 3 / 2, (int) (67 /* SIMPLEDGE_CACHESIZE */));
        int cache_size = Math.min((int) 7, s);

        // TODO is this necessary or would a reserve call work?
        for (int i = 0; i < cache_size; i++) {
            m_triangle_nodes_cache.add(null);
        }
    }

    EditShapeTriangle createTriangle(int value) {
        EditShapeTriangle triangle = new EditShapeTriangle(m_editShape, value);
        return triangle;
    }

    // Returns a cached edge for the given value. May return NULL.
    EditShapeTriangle tryGetCachedTriangle_(int value) {
        int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
        EditShapeTriangle triangle = m_triangle_nodes_cache.get(ind);
        if (triangle != null) {
            if (triangle.m_vertexIndex == value)
                return triangle;
            else {
                // int i = 0;
                // cache collision
            }
        }
        return null;
    }

    // Removes cached edge from the cache for the given value.
    void tryDeleteCachedTriangle_(int value) {
        int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
        EditShapeTriangle se = m_triangle_nodes_cache.get(ind);
        if (se != null && se.m_vertexIndex == value) {// this value is cached
            m_triangle_nodes_recycle.add(se);
            m_triangle_nodes_cache.set(ind, null);
        } else {
            // The value has not been cached
        }
    }

    EditShapeTriangle tryCreateCachedTriangle_(int value) {
        int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
        EditShapeTriangle triangle = m_triangle_nodes_cache.get(ind);
        if (triangle == null) {
            if (m_triangle_nodes_recycle.isEmpty()) {
                m_triangle_nodes_buffer.add(new EditShapeTriangle(m_editShape, value));
                triangle = m_triangle_nodes_buffer.get(m_triangle_nodes_buffer.size() - 1);
            } else {
                triangle = m_triangle_nodes_recycle.get(m_triangle_nodes_recycle.size() - 1);
                m_triangle_nodes_recycle.remove(m_triangle_nodes_recycle.size() - 1);
                triangle.setTriangle(m_editShape, value);
            }

            m_triangle_nodes_cache.set(ind, triangle);
            return triangle;
        } else {
            assert (triangle.getIndex() != value);
        }

        return null;
    }

    void setPathCount(int pathCount) {
        if (pathCount < m_subDivisions * 2)
            m_modulus_distribution = pathCount / 2;
        else
            m_modulus_distribution = pathCount / m_subDivisions;
        if (m_modulus_distribution == 5)
            m_modulus_distribution *= 2;
    }


    @Override
    int compare(TreapEx treap, int left, int node) {
        int right = treap.getElement(node);

        return compareTriangles(left, left, right, right);
    }

    int compareTriangles(int leftElm, int left_vertex, int right_elm, int right_vertex) {
        EditShapeTriangle triangleLeft = tryGetCachedTriangle_(leftElm);
        if (triangleLeft == null) {
            if (m_vertex_1 == left_vertex) {
                triangleLeft = m_temp_triangle_1;
            } else {
                m_vertex_1 = left_vertex;
                triangleLeft = tryCreateCachedTriangle_(leftElm);
                if (triangleLeft == null) {
                    triangleLeft = m_temp_triangle_1;
                    m_temp_triangle_1.setTriangle(m_editShape, leftElm);
                }

            }
        } else {
            m_vertex_1 = left_vertex;
        }

        EditShapeTriangle triangleRight = tryGetCachedTriangle_(right_elm);
        if (triangleRight == null) {
            if (m_vertex_2 == right_vertex) {
                triangleRight = m_temp_triangle_2;
            } else {
                m_vertex_2 = right_vertex;
                triangleRight = tryCreateCachedTriangle_(right_elm);
                if (triangleRight == null) {
                    triangleRight = m_temp_triangle_2;
                    m_temp_triangle_2.setTriangle(m_editShape, right_elm);
                }
            }
        } else {
            m_vertex_2 = right_vertex;
        }

        return compare(triangleLeft, triangleRight);
    }

    @Override
    void onDelete(int elm) {
////            EditShapeTriangle triangle = tryGetCachedTriangle_(elm);
////            if (triangle == null) {
////                triangle = tryCreateCachedTriangle_(elm);
////            }
////
////            int prevVertexIndex = triangle.m_prevVertexIndex;
////            int nextVertexIndex = triangle.m_nextVertexIndex;

        tryDeleteCachedTriangle_(elm);

////            EditShapeTriangle trianglePrev = tryGetCachedTriangle_(prevVertexIndex);
////            if (trianglePrev == null) {
////                trianglePrev = tryCreateCachedTriangle_(prevVertexIndex);
////            }
////            EditShapeTriangle triangleNext = tryGetCachedTriangle_(nextVertexIndex);
////            if (triangleNext == null) {
////                triangleNext = tryCreateCachedTriangle_(nextVertexIndex);
////            }
    }
//
//        @Override
//        void onSet(int oldelm) {
//            tryDeleteCachedTriangle_(oldelm);
//        }
//
//        @Override
//        void onEndSearch(int elm) {
//            tryDeleteCachedTriangle_(elm);
//        }
//
//        @Override
//        void onAddUniqueElementFailed(int elm) {
//            tryDeleteCachedTriangle_(elm);
//        }

    int compare(EditShapeTriangle tri1, EditShapeTriangle tri2) {

        if (m_generalizeType != GeneralizeType.Neither) {
            // 1 for obtuse angle counter-clockwise,
            // -1 for obtuse angle clockwise
            // 0 for collinear
            int orientation1 = tri1.queryOrientation();
            int orientation2 = tri2.queryOrientation();

            if (m_generalizeType == GeneralizeType.ResultContainsOriginal) {
                // if the result contains the original no vertices with a
                // counter clockwise obtuse angle rotation (1) can be removed
                if (orientation1 < 0 && orientation2 > 0) {
                    return 1;
                } else if (orientation2 < 0 && orientation1 > 0) {
                    return -1;
                } else if (orientation1 > 0 && orientation2 > 0) {
                    // Treap requires a unique definition of the positions in the case
                    // of deletions. no 0 returns allowed

                    // for cases where there is a really even distribution of points this seperates the group
                    if (tri1.m_vertexIndex % m_modulus_distribution > tri1.m_vertexIndex % m_modulus_distribution)
                        return -1;
                    else if (tri1.m_vertexIndex % m_modulus_distribution < tri1.m_vertexIndex % m_modulus_distribution)
                        return 1;
                    else if (tri1.m_vertexIndex > tri2.m_vertexIndex)
                        return -1;
                    else if (tri1.m_vertexIndex < tri2.m_vertexIndex)
                        return 1;
                    return 0;
                }
            } else if (m_generalizeType == GeneralizeType.ResultWithinOriginal) {
                if (orientation1 < 0 && orientation2 > 0) {
                    return -1;
                } else if (orientation2 < 0 && orientation1 > 0) {
                    return 1;
                } else if (orientation1 < 0 && orientation2 < 0) {
                    if (tri1.m_vertexIndex % m_modulus_distribution > tri1.m_vertexIndex % m_modulus_distribution)
                        return -1;
                    else if (tri1.m_vertexIndex % m_modulus_distribution < tri1.m_vertexIndex % m_modulus_distribution)
                        return 1;
                    else if (tri1.m_vertexIndex > tri2.m_vertexIndex)
                        return -1;
                    else if (tri1.m_vertexIndex < tri2.m_vertexIndex)
                        return 1;
                    return 0;
                }
            }
        }

        // else if GeneralizeType.Neither
        double area1 = tri1.queryArea();
        double area2 = tri2.queryArea();

        if (area1 < area2) {
            return -1;
        } else if (area2 < area1) {
            return 1;
        } else if (tri1.m_vertexIndex % m_modulus_distribution > tri1.m_vertexIndex % m_modulus_distribution)
            return -1;
        else if (tri1.m_vertexIndex % m_modulus_distribution < tri1.m_vertexIndex % m_modulus_distribution)
            return 1;
        else if (tri1.m_vertexIndex > tri2.m_vertexIndex) {
            return -1;
        } else if (tri1.m_vertexIndex < tri2.m_vertexIndex) {
            return 1;
        }

        return 0;
    }
}