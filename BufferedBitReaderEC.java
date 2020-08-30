import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

public class BufferedBitReaderEC {

    // Note that we need to look ahead 3 bytes, because when the
    // third byte is -1 (EOF indicator) then the second byte is a count
    // of the number of valid bits in the first byte.

    int current;    // Current byte being returned, bit by bit
    int next;       // Next byte to be returned (could be a count)
    int afterNext;  // Byte two after the current byte
    int bitMask;    // Shows which bit to return
    boolean firstTime = true;

    BufferedInputStream input;

    /**
     * Constructor
     * @param pathName the path name of the file to open
     * @throws IOException
     */
    public BufferedBitReaderEC(String pathName) throws IOException {
        input = new BufferedInputStream(new FileInputStream(pathName));
        bitMask = 128;   // a 1 in leftmost bit position
    }

    /**
     * Test to decide whether or not to read the next bit.
     * Input loop: while (reader.hasNext()) { boolean bit = reader.readBit(); }
     *
     * @return whether or not there remains a bit to get (else it's end of file, EOF)
     */
    public boolean hasNext() {
        return afterNext != -1 || next != 0;
    }

    /**
     * Reads a bit and returns it as a false or a true.
     * Throws an exception if there isn't one, so use hasNext() to check first,
     * or else catch the EOFException
     *
     * @return the bit read
     * @throws IOException
     */
    public boolean readBit() throws IOException {
        boolean returnBit;   // Hold the bit to return

        if(firstTime){
            current = input.read();
            if(current == -1)
                throw new EOFException("File did not have two bytes");

            next = input.read();
            if(next == -1)
                throw new EOFException("File did not have two bytes");

            afterNext = input.read();

            firstTime = false;
        }

        if(afterNext == -1)  // Are we emptying the last byte?

            // When afterNext == -1, next is the count of bits remaining.

            if(next == 0)   // No more bits in the final byte to return
                throw new EOFException("No more bits");
            else {
                if((bitMask & current) == 0)
                    returnBit = false;
                else
                    returnBit = true;

                next--;          // One fewer bit to return
                bitMask = bitMask >> 1;    // Shift to mask next bit
                return returnBit;
            }
        else {
            if((bitMask & current) == 0)
                returnBit = false;
            else
                returnBit = true;

            bitMask = bitMask >> 1;    // Shift to mask next bit

            if(bitMask == 0)  {        // Finished returning this byte?
                bitMask = 128;           // Leftmost bit next
                current = next;
                next = afterNext;
                afterNext = input.read();
            }
            return returnBit;
        }
    }

    public char readCharacter() throws IOException{
        char c = (char)input.read();
        if(c == (char)(-1)){throw new EOFException("No more characters.");}
        return c;
    }

    /**
     * Close this bitReader.
     * @throws IOException
     */
    public void close() throws IOException {
        input.close();
    }

}
