package io.prelink.critbit.sharedbytearray;

public final class ThinSBA extends AbstractSBA {

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

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < start || end > bytes.length) {
            throw new IndexOutOfBoundsException();
        }
        if(start == end) {
            return EmptySBA.INSTANCE;
        } else if(start == 0 && end == length()) {
            return this;
        } else if(start == 0) {
            return new PrefixSBA(bytes, end);
        } else if(end == length()) {
            return new SuffixSBA(bytes, start);
        } else {
            return new ThickSBA(bytes, start, end);
        }
    }

    public void toByteArray(int start, byte[] target, int targetStart, int len) {
        System.arraycopy(bytes, start, target, targetStart, len);
    }
}
