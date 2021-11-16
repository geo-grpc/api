package com.epl.protobuf.v1;

import com.esri.core.geometry.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.epl.protobuf.v1.ShapefileByteReader.geometryTypeFromShpType;

public class ShapefileChunkedReader extends GeometryCursor {
    private final List<MixedEndianDataInputStream> inputStreamList;
    private ArrayDeque<ByteBuffer> m_byteBufferDeque;
    private SimpleByteBufferCursor m_byteBufferCursor;
    private OperatorImportFromESRIShapeCursor m_operatorImport;
    private long fileLengthBytes;
    private final Envelope2D envelope2D;
    private int position; //keeps track of where inputstream is
    private int currentRecordNumber;
    private int nextRecordNumber;
    private int recordVertexCount;
    private int partsPerVertex;
    private final Geometry.Type geomType;

    ShapefileChunkedReader(InputStream in, int chunk_size) throws IOException {
        if (chunk_size < 108) {
            throw new IllegalArgumentException("An InputStream must have more than 100 bytes to initialize ShapefileChunkedReader");
        }
        m_byteBufferDeque = new ArrayDeque<>();
        m_byteBufferCursor = new SimpleByteBufferCursor(m_byteBufferDeque, null);
        m_operatorImport = new OperatorImportFromESRIShapeCursor(0, 0, m_byteBufferCursor);
        position = 0;
        inputStreamList = Collections.synchronizedList(new ArrayList<MixedEndianDataInputStream>());
        synchronized (inputStreamList) {
            MixedEndianDataInputStream mixedEndianDataInputStream = new MixedEndianDataInputStream(in, chunk_size);
            inputStreamList.add(mixedEndianDataInputStream);

            /*
                Byte 0 File Code 9994 Integer Big
            */
            int fileCode = mixedEndianDataInputStream.readInt();
            if (fileCode != 9994) {
                throw new IOException("file code " + fileCode + " is not supported.");
            }

            /*
                Byte 4 Unused 0 Integer Big
                Byte 8 Unused 0 Integer Big
                Byte 12 Unused 0 Integer Big
                Byte 16 Unused 0 Integer Big
                Byte 20 Unused 0 Integer Big
             */
            mixedEndianDataInputStream.skipBytes(20);

            /* Byte 24 File Length File Length Integer Big */
            // TODO what is the times 2 here? is it the number of doubles? the number of xy coordinates? would this be * 3 if we had zs?
            fileLengthBytes = mixedEndianDataInputStream.readInt() * 2;

            /* Byte 28 Version 1000 Integer Little */
            int v = mixedEndianDataInputStream.readLittleEndianInt();

            if (v != 1000) {
                throw new IOException("version " + v + " is not supported.");
            }

            /* Byte 32 Shape Type Shape Type Integer Little */
            int shpTypeId = mixedEndianDataInputStream.readLittleEndianInt();
            geomType = geometryTypeFromShpType(shpTypeId);

            /* Byte 36 Bounding Box Xmin Double Little
               Byte 44 Bounding Box Ymin Double Little
               Byte 52 Bounding Box Xmax Double Little
               Byte 60 Bounding Box Ymax Double Little */
            double xmin = mixedEndianDataInputStream.readLittleEndianDouble();
            double ymin = mixedEndianDataInputStream.readLittleEndianDouble();
            double xmax = mixedEndianDataInputStream.readLittleEndianDouble();
            double ymax = mixedEndianDataInputStream.readLittleEndianDouble();
            partsPerVertex = 2;

            /* Byte 68* Bounding Box Zmin Double Little
               Byte 76* Bounding Box Zmax Double Little */
            double zmin = mixedEndianDataInputStream.readLittleEndianDouble();
            double zmax = mixedEndianDataInputStream.readLittleEndianDouble();
            // if has z
            // partsPerVertex += 1;

            /* Byte 84* Bounding Box Mmin Double Little
               Byte 92* Bounding Box Mmax Double Little */
            double mmin = mixedEndianDataInputStream.readLittleEndianDouble();
            double mmax = mixedEndianDataInputStream.readLittleEndianDouble();
            // if has m
            // partsPerVertex += 1;

            envelope2D = new Envelope2D(xmin, ymin, xmax, ymax);
            //  envelope3D = new Envelope3D(xmin, ymin, zmin, xmax, ymax, zmax);

            position = 2 * 50; //header is always 50 words long
            mixedEndianDataInputStream.updateSize(2 * 50);

            nextRecordNumber = mixedEndianDataInputStream.readInt();//1 based
            recordVertexCount = mixedEndianDataInputStream.readInt();
            position += 8;
            mixedEndianDataInputStream.updateSize(8);

            inputStreamList.notify();
        }
    }

    public Geometry next() {
        if (!hasNext()) {
            return null;
        }
        try {
            synchronized (inputStreamList) {
                MixedEndianDataInputStream inputStream = inputStreamList.get(0);

                int recordSizeBytes = (recordVertexCount * partsPerVertex);
                byte[] bytes = new byte[recordSizeBytes];
                int read = inputStream.read(bytes, 0, recordSizeBytes);

                // SequenceInputStream is stupid
                while (read < recordSizeBytes) {
                    int len = recordSizeBytes - read;
                    read += inputStream.read(bytes, read, len);
                }
                position += read;
                inputStream.updateSize(read);
                currentRecordNumber = nextRecordNumber;

                if (position < fileLengthBytes) {
                    nextRecordNumber = inputStream.readInt();//1 based
                    recordVertexCount = inputStream.readInt();
                    position += 8;

                    inputStream.updateSize(8);
                } else {
                    inputStream.close();
                    inputStreamList.remove(0);
                }

                inputStreamList.notify();
                m_byteBufferDeque.push(ByteBuffer.wrap(bytes));
                return m_operatorImport.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addStream(InputStream in, int chunk_size) {
        synchronized (inputStreamList) {
            MixedEndianDataInputStream mixedEndianDataInputStream = new MixedEndianDataInputStream(in, chunk_size);
            inputStreamList.add(mixedEndianDataInputStream);
            inputStreamList.notify();
        }
    }

    @Override
    public int getGeometryID() {
        return currentRecordNumber;
    }

    public Envelope2D getEnvelope2D() {
        return envelope2D;
    }

    public Geometry.Type getGeometryType() { return geomType; }

    @Override
    public boolean hasNext() {
        synchronized (inputStreamList) {
            if (inputStreamList.size() == 0 || position >= fileLengthBytes) {
                return false;
            }

            int count = 0;

            long value = inputStreamList.stream().mapToInt(MixedEndianDataInputStream::getSize).sum();
            while (count++ < 2 && position < fileLengthBytes && value < recordVertexCount) {
                try {
                    inputStreamList.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                value = inputStreamList.stream().mapToInt(MixedEndianDataInputStream::getSize).sum();
            }

            if (position < fileLengthBytes && value < recordVertexCount) {
                return false;
            }

            // plus 8 is
            //int recordSizeBytes = (recordVertexCount * partsPerVertex)
            long bytesRequired = recordVertexCount * partsPerVertex + 8;
            if (recordVertexCount == fileLengthBytes - position) {
                bytesRequired = recordVertexCount;
            }

            while (bytesRequired > inputStreamList.get(0).getSize() && inputStreamList.size() > 1) {
                if (inputStreamList.size() > 1) {
                    MixedEndianDataInputStream input1 = inputStreamList.remove(0);
                    int remaining_bytes_1 = input1.getSize();

                    MixedEndianDataInputStream input2 = inputStreamList.remove(0);
                    int remaining_bytes_2 = input2.getSize();

                    InputStream merged = new java.io.SequenceInputStream(input1, input2);
                    MixedEndianDataInputStream mixedEndianDataInputStream = new MixedEndianDataInputStream(merged, input1.getSize() + input2.getSize());
                    inputStreamList.add(0, mixedEndianDataInputStream);

//                    try {
//                        input1.close();
//                        input2.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }


                }
            }

            if (bytesRequired > inputStreamList.get(0).getSize()) {
                return false;
            }

            return position < fileLengthBytes;
        }
    }
}
