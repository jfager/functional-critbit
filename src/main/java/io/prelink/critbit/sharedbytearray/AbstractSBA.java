package io.prelink.critbit.sharedbytearray;

public abstract class AbstractSBA implements SharedByteArray {

    public boolean equals(Object other) {
        if(other != null && other instanceof SharedByteArray) {
            SharedByteArray sba = (SharedByteArray)other;
            if(length() != sba.length()) {
                return false;
            }
            for(int i=0; i<length(); i++) {
                if(byteAt(i) != sba.byteAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int indexOf(byte[] needle) {
        if(needle.length == 0 || needle.length > length()) {
            return -1;
        }
        byte first = needle[0];
        int maxIndex = length() - needle.length;
        for (int i = 0; i <= maxIndex; i++) {
            if (byteAt(i) != first) {
                while (++i <= maxIndex && byteAt(i) != first);
            }

            if (i <= maxIndex) {
                int j = i + 1;
                int k = 1;
                while(k < needle.length && byteAt(j) == needle[k]) {
                    j++; k++;
                }
                if (k == needle.length) {
                    return i;
                }
            }
        }
        return -1;
    }

    public SharedByteArray prefix(int end) {
        return sub(0, end);
    }

    public SharedByteArray suffix(int start) {
        return sub(start, length());
    }

    public SharedByteArray append(SharedByteArray sba) {
        return new JoinedSBA(this, sba);
    }

    public byte[] toByteArray() {
        byte[] out = new byte[length()];
        toByteArray(0, out, 0, out.length);
        return out;
    }

}
