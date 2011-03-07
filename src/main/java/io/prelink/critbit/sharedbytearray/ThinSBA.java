package io.prelink.critbit.sharedbytearray;

import java.util.Arrays;

public class ThinSBA implements SharedByteArray {

    private final byte[] bytes;

    public ThinSBA(byte[] bytes) {
        this.bytes = bytes;
    }

    public int length() {
        return bytes.length;
    }

    public byte byteAt(int index) {
        return bytes[index];
    }

    static int easyIndexOf(SharedByteArray sba, byte[] needle) {
        if(needle.length == 0 || needle.length > sba.length()) {
            return -1;
        }
        byte first = needle[0];
        int maxIndex = sba.length() - needle.length;
        for (int i = 0; i <= maxIndex; i++) {
            if (sba.byteAt(i) != first) {
                while (++i <= maxIndex && sba.byteAt(i) != first);
            }

            if (i <= maxIndex) {
                int j = i + 1;
                int k = 1;
                for (;k < needle.length && sba.byteAt(j) == needle[k]; j++, k++);

                if (k == needle.length) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOf(byte[] needle) {
        return easyIndexOf(this, needle);
    }

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < 0 || end < start || end > bytes.length) {
            throw new IndexOutOfBoundsException();
        }
        if(start == 0 && end == bytes.length) {
            return this;
        } else if(end == bytes.length) {
            return new ThickSBA(bytes, start);
        } else {
            return new ThickerSBA(bytes, start, end);
        }
    }

    public SharedByteArray sub(int start) {
        return sub(start, bytes.length);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public void toByteArray(byte[] target) {
        System.arraycopy(bytes, 0, target, 0, bytes.length);
    }
}
