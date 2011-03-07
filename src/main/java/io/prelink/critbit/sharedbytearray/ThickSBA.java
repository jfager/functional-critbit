package io.prelink.critbit.sharedbytearray;

class ThickSBA implements SharedByteArray {

    private final byte[] bytes;
    private final int sharedStart;

    public ThickSBA(byte[] bytes, int start) {
        this.bytes = bytes;
        this.sharedStart = start;
    }

    public int length() {
        return bytes.length - sharedStart;
    }

    public byte byteAt(int index) {
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
        } else if(end == length()) {
            return new ThickSBA(bytes, sharedStart + start);
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
