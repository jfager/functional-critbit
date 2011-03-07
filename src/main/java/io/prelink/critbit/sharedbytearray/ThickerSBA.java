package io.prelink.critbit.sharedbytearray;

class ThickerSBA implements SharedByteArray {
    private final byte[] bytes;
    private final int sharedStart;
    private final int sharedEnd;

    public ThickerSBA(byte[] bytes, int start, int end) {
        this.bytes = bytes;
        this.sharedStart = start;
        this.sharedEnd = end;
    }

    public int length() {
        return sharedEnd - sharedStart;
    }

    public byte byteAt(int index) {
        if(index > length()) {
            throw new IndexOutOfBoundsException();
        }
        return bytes[sharedStart + index];
    }

    public int indexOf(byte[] needle) {
        return ThinSBA.easyIndexOf(this, needle);
    }

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException();
        }
        if(start == 0 && end == length()) {
            return this;
        } else {
            return new ThickerSBA(bytes, sharedStart + start, sharedStart + end);
        }
    }

    public SharedByteArray sub(int start) {
        return sub(start, length());
    }

    public byte[] toByteArray() {
        byte[] out = new byte[length()];
        toByteArray(out);
        return out;
    }

    public void toByteArray(byte[] target) {
        System.arraycopy(bytes, sharedStart, target, 0, length());
    }
}
