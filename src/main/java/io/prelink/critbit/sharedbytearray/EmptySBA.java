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

    private void ensureZero(int i) {
        if(i != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    public int indexOf(byte needle, int from) {
        ensureZero(from);
        return -1;
    }

    public int indexOf(byte[] needle, int from) {
        ensureZero(from);
        if(needle.length == 0) {
            return 0;
        }
        return -1;
    }

    public int indexOf(SharedByteArray needle, int from) {
        ensureZero(from);
        if(needle.length() == 0) {
            return 0;
        }
        return -1;
    }

    public int lastIndexOf(byte needle, int to) {
        ensureZero(to);
        return -1;
    }

    public int lastIndexOf(byte[] needle, int to) {
        ensureZero(to);
        if(needle.length == 0) {
            return 0;
        }
        return -1;
    }

    public int lastIndexOf(SharedByteArray needle, int to) {
        ensureZero(to);
        if(needle.length() == 0) {
            return 0;
        }
        return -1;
    }

    public SharedByteArray prefix(int end) {
        ensureZero(end);
        return this;
    }

    public SharedByteArray suffix(int start) {
        ensureZero(start);
        return this;
    }

    public SharedByteArray sub(int start, int end) {
        ensureZero(start);
        ensureZero(end);
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
