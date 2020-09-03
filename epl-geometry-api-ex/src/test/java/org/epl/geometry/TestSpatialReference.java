/*
 Copyright 1995-2017 Esri

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
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */

package org.epl.geometry;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSpatialReference extends TestCase {
    @Test
    public void testEquals() {
        String wktext1 = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]";
        String wktext2 = "PROJCS[\"WGS_1984_Web_Mercator_Auxiliary_Sphere\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Mercator_Auxiliary_Sphere\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",0.0],PARAMETER[\"Standard_Parallel_1\",0.0],PARAMETER[\"Auxiliary_Sphere_Type\",0.0],UNIT[\"Meter\",1.0]]";
        String proj4 = "+proj=utm +zone=30 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs ";
        SpatialReferenceEx a1 = SpatialReferenceEx.create(wktext1);
        SpatialReferenceEx b = SpatialReferenceEx.create(wktext2);
        SpatialReferenceEx a2 = SpatialReferenceEx.create(wktext1);
        SpatialReferenceEx c = SpatialReferenceEx.createFromProj4(proj4);

        assertTrue(a1.equals(a1));
        assertTrue(b.equals(b));

        assertTrue(a1.equals(a2));

        assertFalse(a1.equals(b));
        assertFalse(b.equals(a1));

        assertFalse(c.equals(a1));
        assertFalse(c.equals(b));
    }

    @Test
    public void testTolerance() {
        String wktWGS84 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
        SpatialReferenceEx a1 = SpatialReferenceEx.create(wktWGS84);
        SpatialReferenceEx a2 = SpatialReferenceEx.create(4326);
        assertEquals(a1.getTolerance(), a2.getTolerance());
    }

    @Test
    public void prjCreateFromProj4() {
        double longitude = 0.0;
        double latitude = 0.0;
        String proj4 = String.format(
                "+proj=laea +lat_0=%f +lon_0=%f +x_0=0.0 +y_0=0.0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                longitude, latitude);

        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4(proj4);
        assertNotNull(spatialReference);
        assertEquals(spatialReference.getProj4(), proj4);
    }

    @Test
    public void testWKTToWkid() {
        String test1 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
        Pattern pattern = Pattern.compile("^([\\w\\W]+AUTHORITY[\\s]*\\[[\\s]*\"EPSG\"[\\s]*,[\\s]*[\"]*([\\d]+)[\"]*]])$");
        Matcher matcher = pattern.matcher(test1);

        assertTrue(matcher.find());
        assertTrue(matcher.group(2).equals("4326"));

        SpatialReferenceEx spatialReference = SpatialReferenceEx.create(test1);
        assertEquals(spatialReference.getID(), 4326);
        assertNotNull(spatialReference.getProj4());
        assertNull(spatialReference.getText());

        String test2 = "GEOGCS[\n" +
                "\t\"WGS 84\",\n" +
                "\tDATUM[\n" +
                "\t\t\"WGS_1984\",\n" +
                "\t\tSPHEROID[\n" +
                "\t\t\t\"WGS 84\",\n" +
                "\t\t\t6378137,\n" +
                "\t\t\t298.257223563,\n" +
                "\t\t\tAUTHORITY[\"EPSG\",\"7030\"]\n" +
                "\t\t\t],\n" +
                "\t\t\tAUTHORITY[\"EPSG\",\"6326\"]\n" +
                "\t\t],\n" +
                "\t\tPRIMEM[\n" +
                "\t\t\t\"Greenwich\",\n" +
                "\t\t\t0,\n" +
                "\t\t\tAUTHORITY[\"EPSG\",\"8901\"]\n" +
                "\t\t],\n" +
                "\t\tUNIT[\n" +
                "\t\t\t\"degree\",\n" +
                "\t\t\t0.0174532925199433,\n" +
                "\t\t\tAUTHORITY[\"EPSG\",\"9122\"]\n" +
                "\t\t],\n" +
                "\t\t\n" +
                "\t\tAUTHORITY\n" +
                "\n" +
                "\t\t[\n" +
                "\n" +
                "\t\t\t\"EPSG\" ,\n" +
                "\t\t\t\"4326\"\n" +
                "\t\t\n" +
                "\t\t]\n" +
                "\n" +
                "\t]";
        SpatialReferenceEx spatialReference2 = SpatialReferenceEx.create(test2);
        assertEquals(spatialReference2.getID(), 4326);
        assertNotNull(spatialReference2.getProj4());
        assertNull(spatialReference2.getText());

        String test3 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
        SpatialReferenceEx spatialReference3 = SpatialReferenceEx.create(test3);
        assertEquals(spatialReference3.getID(), 4326);
        assertNotNull(spatialReference3.getProj4());
        assertNull(spatialReference3.getText());

        String test4 = "PROJCS[\"WGS 84 / UTM zone 17N\",\n" +
                "    GEOGCS[\"WGS 84\",\n" +
                "        DATUM[\"WGS_1984\",\n" +
                "            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "                AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "            AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4326\"]],\n" +
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",-81],\n" +
                "    PARAMETER[\"scale_factor\",0.9996],\n" +
                "    PARAMETER[\"false_easting\",500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"metre\",1,\n" +
                "        AUTHORITY[\"EPSG\",\"9001\"]],\n" +
                "    AXIS[\"Easting\",EAST],\n" +
                "    AXIS[\"Northing\",NORTH],\n" +
                "    AUTHORITY[\"EPSG\",\"32617\"]]\n" +
                "    ";
        SpatialReferenceEx spatialReference4 = SpatialReferenceEx.create(test4);
        assertEquals(spatialReference4.getID(), 32617);
        assertNotNull(spatialReference4.getProj4());
        assertNull(spatialReference4.getText());

        String customWKT = "PROJCS[\"Lo27_Cape\",GEOGCS[\"GCS_Cape\",DATUM[\"D_Cape\",SPHEROID[\"Clarke_1880_Arc\",6378249.145,293.466307656]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",27.0],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
        SpatialReferenceEx spatialReference5 = SpatialReferenceEx.create(customWKT);
        assertEquals(spatialReference5.getID(), 0);
        assertNull(spatialReference5.getProj4());
        assertNotNull(spatialReference5.getText());
    }

    @Test
    public void testProj4EPSG() {
        String epsg = "+init=epsg:26711";
        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4(epsg);
        assertEquals(26711, spatialReference.getID());
        assertEquals(SpatialReferenceEx.CoordinateSystemType.PROJECTED, spatialReference.getCoordinateSystemType());

        epsg = "+init=epsg:4326";
        spatialReference = SpatialReferenceEx.createFromProj4(epsg);
        assertEquals(4326, spatialReference.getID());
        assertEquals(SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC, spatialReference.getCoordinateSystemType());

        String test4 = "PROJCS[\"WGS 84 / UTM zone 17N\",\n" +
                "    GEOGCS[\"WGS 84\",\n" +
                "        DATUM[\"WGS_1984\",\n" +
                "            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "                AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "            AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4326\"]],\n" +
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",-81],\n" +
                "    PARAMETER[\"scale_factor\",0.9996],\n" +
                "    PARAMETER[\"false_easting\",500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"metre\",1,\n" +
                "        AUTHORITY[\"EPSG\",\"9001\"]],\n" +
                "    AXIS[\"Easting\",EAST],\n" +
                "    AXIS[\"Northing\",NORTH]]]\n" +
                "    ";
        SpatialReferenceEx spatialReference4 = SpatialReferenceEx.create(test4);
        assertEquals(SpatialReferenceEx.CoordinateSystemType.PROJECTED, spatialReference4.getCoordinateSystemType());

        spatialReference = SpatialReferenceEx.createFromProj4("+proj=longlat +datum=WGS84 +no_defs ");
        assertEquals(SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC, spatialReference.getCoordinateSystemType());
    }

    @Test
    public void testProj9001() {
        String tet5 = "PROJCS[\"NUTM30\",GEOGCS[\"ETRS89\",DATUM[\"European_Terrestrial_Reference_System_1989\",SPHEROID[\"GRS 1980\",6378137,298.2572221010042,AUTHORITY[\"EPSG\",\"7019\"]],AUTHORITY[\"EPSG\",\"6258\"]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433],AUTHORITY[\"EPSG\",\"4258\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-3],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]]]";
        String epsg = "+init=epsg:4326";
        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4(epsg);
        assertEquals(4326, spatialReference.getID());
        assertEquals(SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC, spatialReference.getCoordinateSystemType());

        SpatialReferenceEx spatialReference1 = SpatialReferenceEx.create(tet5);
        assertEquals(9001, spatialReference1.getID());
        assertEquals("+init=epsg:9001", spatialReference1.getProj4());
    }

    @Test
    public void testProjEquality() {
        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4("+proj=utm +zone=30 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs ");
        SpatialReferenceEx spatialReference2 = SpatialReferenceEx.createFromProj4("+proj=utm +zone=30 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs ");
        assertEquals(spatialReference, spatialReference2);
    }
}

