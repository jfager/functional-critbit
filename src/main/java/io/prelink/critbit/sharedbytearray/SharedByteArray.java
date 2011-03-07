package io.prelink.critbit.sharedbytearray;

public interface SharedByteArray {
    int length();
    byte byteAt(int index);
    int indexOf(byte[] needle);
    SharedByteArray sub(int start);
    SharedByteArray sub(int start, int end);
    byte[] toByteArray();
    void toByteArray(byte[] target);
}
