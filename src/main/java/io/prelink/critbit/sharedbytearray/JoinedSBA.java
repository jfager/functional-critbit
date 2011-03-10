package io.prelink.critbit.sharedbytearray;

final class JoinedSBA extends AbstractSBA {
    private final SharedByteArray head;
    private final SharedByteArray tail;

    public JoinedSBA(SharedByteArray head, SharedByteArray tail) {
        this.head = head;
        this.tail = tail;
    }

    public int length() {
        return head.length() + tail.length();
    }

    public byte byteAt(int index) {
        if(index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException();
        }

        if(index < head.length()) {
            return head.byteAt(index);
        } else {
            return tail.byteAt(index - head.length());
        }
    }

    public SharedByteArray sub(int start, int end) {
        if(start < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException();
        }
        if(start == end) {
            return EmptySBA.INSTANCE;
        } else if(start == 0 && end == length()) {
            return this;
        } else if(start == 0 && end == head.length()) {
            return head;
        } else if(start == head.length() && end == length()) {
            return tail;
        } else if(end < head.length()) {
            return head.sub(start, end);
        } else if(start >= head.length()) {
            return tail.sub(start-head.length(), end-head.length());
        } else {
            return new JoinedSBA(head.suffix(start),
                                 tail.prefix(end-head.length()));
        }
    }

    public void toByteArray(int start, byte[] target, int targetStart, int len) {
        int end = start+len;
        if(start < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException();
        }
        if(start == end) {
            //do nothing
        } else if(end < head.length()) {
            head.toByteArray(start, target, targetStart, len);
        } else if(start >= head.length()) {
            tail.toByteArray(start-head.length(), target, targetStart, len);
        } else {
            int headlen = head.length() - start;
            head.toByteArray(start, target, targetStart, headlen);
            tail.toByteArray(0, target, targetStart+headlen, len - headlen);
        }
    }

}
