/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


package com.epl.protobuf.v1;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Augments DataInputStream with methods to read little-endian primitives, as the ESRI
 * shapefile format is mixed.
 */
class MixedEndianDataInputStream extends DataInputStream {
    private byte readBuffer[] = new byte[8];
    private int input_stream_size = 0;

    MixedEndianDataInputStream(InputStream inputStream) {
        super(inputStream);
    }

    MixedEndianDataInputStream(InputStream inputStream, int input_stream_size) {
        super(inputStream);
        this.input_stream_size = input_stream_size;
    }

    void updateSize(int subtract) {
        input_stream_size -= subtract;
    }

    int getSize() {
        return input_stream_size;
    }
    /**
     * modification of {@link DataInputStream#readInt()} reversing byte order
     */
    final int readLittleEndianInt() throws IOException {
        int ch4 = in.read();
        int ch3 = in.read();
        int ch2 = in.read();
        int ch1 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    /**
     * modification {@link DataInputStream#readLong()} ()} reversing the array indices
     */
    private long readLittleEndianLong() throws IOException {
        readFully(readBuffer, 0, 8);
        return (((long) readBuffer[7] << 56) +
                ((long) (readBuffer[6] & 255) << 48) +
                ((long) (readBuffer[5] & 255) << 40) +
                ((long) (readBuffer[4] & 255) << 32) +
                ((long) (readBuffer[3] & 255) << 24) +
                ((readBuffer[2] & 255) << 16) +
                ((readBuffer[1] & 255) << 8) +
                ((readBuffer[0] & 255)));
    }

    double readLittleEndianDouble() throws IOException {
        return Double.longBitsToDouble(readLittleEndianLong());
    }

}
