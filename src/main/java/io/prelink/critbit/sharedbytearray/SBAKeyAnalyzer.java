package io.prelink.critbit.sharedbytearray;

import java.io.Serializable;

import org.ardverk.collection.AbstractKeyAnalyzer;
import org.ardverk.collection.KeyAnalyzer;

public class SBAKeyAnalyzer
    extends AbstractKeyAnalyzer<SharedByteArray>
    implements Serializable
{
    public static final SBAKeyAnalyzer INSTANCE = new SBAKeyAnalyzer();

    private static final long serialVersionUID = 20110307L;

    private static final int MSB = 1 << Byte.SIZE-1;

    @Override
    public int compare(SharedByteArray o1, SharedByteArray o2) {
        if (o1 == null) {
            return (o2 == null) ? 0 : -1;
        } else if (o2 == null) {
            return (o1 == null) ? 0 : 1;
        }

        if (o1.length() != o2.length()) {
            return o1.length() - o2.length();
        }

        for (int i = 0; i < o1.length(); i++) {
            int diff = (o1.byteAt(i) & 0xFF) - (o2.byteAt(i) & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }

    @Override
    public int lengthInBits(SharedByteArray key) {
        return key.length() * Byte.SIZE;
    }

    @Override
    public boolean isPrefix(SharedByteArray key, SharedByteArray prefix) {
        if (key.length() < prefix.length()) {
            return false;
        }

        for (int i = 0; i < prefix.length(); i++) {
            if (key.byteAt(i) != prefix.byteAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a bit mask where the given bit is set
     */
    private static int mask(int bit) {
        return MSB >>> bit;
    }

    /**
     * Returns the {@code byte} value at the given index.
     */
    private static byte valueAt(SharedByteArray values, int index) {
         if (index >= 0 && index < values.length()) {
             return values.byteAt(index);
         }
         return 0;
    }

    @Override
    public boolean isBitSet(SharedByteArray key, int bitIndex) {
        if (bitIndex >= lengthInBits(key)) {
            return false;
        }

        int index = (int)(bitIndex / Byte.SIZE);
        int bit = (int)(bitIndex % Byte.SIZE);
        return (key.byteAt(index) & mask(bit)) != 0;
    }

    @Override
    public int bitIndex(SharedByteArray key, SharedByteArray otherKey) {
        int length = Math.max(key.length(), otherKey.length());

        boolean allNull = true;
        for (int i = 0; i < length; i++) {
            byte b1 = valueAt(key, i);
            byte b2 = valueAt(otherKey, i);

            if (b1 != b2) {
                int xor = b1 ^ b2;
                for (int j = 0; j < Byte.SIZE; j++) {
                    if ((xor & mask(j)) != 0) {
                        return (i * Byte.SIZE) + j;
                    }
                }
            }

            if (b1 != 0) {
                allNull = false;
            }
        }

        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

}
