package io.prelink.critbit.sharedbytearray;

final class PrefixSBA extends AbstractSBA {

    private final byte[] bytes;
    private final int sharedEnd;

    public PrefixSBA(byte[] bytes, int end) {
        this.bytes = bytes;
        this.sharedEnd = end;
    }

    public int length() {
        return sharedEnd;
    }

    public byte byteAt(int index) {
        if(index < 0 || index >= sharedEnd) {
            throw new IndexOutOfBoundsException();
        }
        return bytes[index];
    }

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException();
        }
        if(start == end) {
            return EmptySBA.INSTANCE;
        } else if(start == 0 && end == length()) {
            return this;
        } else if(start == 0) {
            return new PrefixSBA(bytes, end);
        } else {
            return new ThickSBA(bytes, start, end);
        }
    }

    public void toByteArray(int start, byte[] target, int targetStart, int len) {
        if(start + len > sharedEnd) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(bytes, start, target, targetStart, len);
    }
}
