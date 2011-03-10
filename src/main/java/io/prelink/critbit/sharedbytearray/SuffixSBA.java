package io.prelink.critbit.sharedbytearray;

final class SuffixSBA extends AbstractSBA {

    private final byte[] bytes;
    private final int sharedStart;

    public SuffixSBA(byte[] bytes, int start) {
        this.bytes = bytes;
        this.sharedStart = start;
    }

    public int length() {
        return bytes.length - sharedStart;
    }

    public byte byteAt(int index) {
        if(index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return bytes[sharedStart + index];
    }

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException();
        }
        if(start == end) {
            return EmptySBA.INSTANCE;
        } else if(start == 0 && end == length()) {
            return this;
        } else if(end == length()) {
            return new SuffixSBA(bytes, sharedStart + start);
        } else {
            return new ThickSBA(bytes, sharedStart + start, sharedStart + end);
        }
    }

    public void toByteArray(int start, byte[] target, int targetStart, int len) {
        if(start < 0) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(bytes, sharedStart + start, target, targetStart, len);
    }
}
