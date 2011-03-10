package io.prelink.critbit.sharedbytearray;

public interface SharedByteArray {
    int length();
    byte byteAt(int index);
    int indexOf(byte[] needle);
    SharedByteArray prefix(int end);
    SharedByteArray suffix(int start);
    SharedByteArray sub(int start, int end);
    SharedByteArray append(SharedByteArray sba);
    byte[] toByteArray();
    void toByteArray(int from, byte[] target, int targetStart, int len);
}
