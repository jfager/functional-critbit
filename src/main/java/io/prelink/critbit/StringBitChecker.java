package io.prelink.critbit;

/**
 * Most of this based on a similar implementation from
 * (https://github.com/rkapsi/patricia-trie).
 */
public final class StringBitChecker implements BitChecker<String> {
    private static final int MSB = 1 << Character.SIZE-1;
    public boolean isSet(final String key, final int i) {
        if(i >= bitLength(key)) {
            return false;
        }

        final int charIndex = (int)(i / Character.SIZE);
        final int bit = (int)(i % Character.SIZE);
        final int mask = (MSB >>> bit);
        return (key.charAt(charIndex) & mask) != 0;
    }

    private static int indexFor(final int charIndex,
                                final int bitIndex) {
        return (charIndex * Character.SIZE) +
               (Integer.numberOfLeadingZeros(bitIndex) - Character.SIZE);
    }

    public int firstDiff(final String k1, final String k2) {
        String shorter = k1.length() < k2.length() ? k1 : k2;
        int ci;
        for(ci = 0; ci < shorter.length(); ci++) {
            char chk1 = k1.charAt(ci);
            char chk2 = k2.charAt(ci);
            if(chk1 != chk2) {
                int x = chk1 ^ chk2;
                return indexFor(ci, x);
            }
        }

        String longer = k1.length() > k2.length() ? k1 : k2;
        for(;ci < longer.length(); ci++) {
            char chk = longer.charAt(ci);
            if(chk != 0) {
                return indexFor(ci, chk);
            }
        }

        return -1; //they are the same
    }

    public int bitLength(final String key) {
        return key.length() * Character.SIZE;
    }

    public boolean startsWith(final String key, final String prefix) {
        return key.startsWith(prefix);
    }
}
