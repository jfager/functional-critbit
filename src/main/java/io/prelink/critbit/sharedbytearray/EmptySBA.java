package io.prelink.critbit.sharedbytearray;

final class EmptySBA implements SharedByteArray {

    public static EmptySBA INSTANCE = new EmptySBA();
    private static final byte[] EMPTY_ARRAY = new byte[0];

    private EmptySBA() {}

    public int length() {
        return 0;
    }

    public byte byteAt(int index) {
        throw new IndexOutOfBoundsException();
    }

    public int indexOf(byte[] needle) {
        if(needle.length == 0) {
            return 0;
        }
        return -1;
    }

    public SharedByteArray prefix(int end) {
        if(end != 0) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public SharedByteArray suffix(int start) {
        if(start != 0) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public SharedByteArray sub(int start, int end) {
        if(!(start == 0 && end == 0)) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    public SharedByteArray append(SharedByteArray sba) {
        return sba;
    }

    public byte[] toByteArray() {
        return EMPTY_ARRAY;
    }

    public void toByteArray(int from, byte[] target, int targetStart, int len) {
        if(from != 0 || len != 0 || targetStart < 0 || targetStart > target.length) {
            throw new IndexOutOfBoundsException();
        }
    }
}
