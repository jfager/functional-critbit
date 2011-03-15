package io.prelink.critbit.sharedbytearray;

public abstract class AbstractSBA implements SharedByteArray {

    private void checkBoundsIncl(int index) {
        if(index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException();
        }
    }
    private void checkBoundsExcl(int index) {
        if(index < 0 || index > length()) {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
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

    public int indexOf(byte needle, int from) {
        checkBoundsIncl(from);
        for (int i = from; i < length(); i++) {
            if (byteAt(i) == needle) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(byte[] needle, int from) {
        return indexOf(new ThinSBA(needle), from);
    }

    public int indexOf(SharedByteArray needle, int from) {
        checkBoundsIncl(from);

        int effectiveLength = length() - from;
        if(needle.length() == 0 || needle.length() > effectiveLength) {
            return -1;
        }
        byte first = needle.byteAt(from);
        int maxIndex = length() - needle.length();
        for (int i = from; i <= maxIndex; i++) {
            if (byteAt(i) != first) {
                while (++i <= maxIndex && byteAt(i) != first);
            }

            if (i <= maxIndex) {
                int j = i + 1;
                int k = 1;
                while(k < needle.length() && byteAt(j) == needle.byteAt(k)) {
                    j++; k++;
                }
                if (k == needle.length()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int lastIndexOf(byte needle, int to) {
        checkBoundsExcl(to);
        for (int i = to-1; i >= 0; i--) {
            if (byteAt(i) == needle) {
                return i;
            }
        }
        return -1;
    }
    public int lastIndexOf(byte[] needle, int to) {
        return lastIndexOf(new ThinSBA(needle), to);
    }
    public int lastIndexOf(SharedByteArray needle, int to) {
        checkBoundsExcl(to);
        if(needle.length() == 0 || needle.length() > to) {
            return -1;
        }
        byte first = needle.byteAt(0);
        int maxIndex = to - needle.length();
        for (int i = maxIndex; i >= 0; i--) {
            if (byteAt(i) != first) {
                while (--i >= 0 && byteAt(i) != first);
            }

            if (i >= 0) {
                int j = i + 1;
                int k = 1;
                while(k < needle.length() && byteAt(j) == needle.byteAt(k)) {
                    j++; k++;
                }
                if (k == needle.length()) {
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
