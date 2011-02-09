package io.prelink.critbit;

/**
 * Most of this based on a similar implementation from 
 * (https://github.com/rkapsi/patricia-trie).
 */
class StringBitChecker implements BitChecker<String> {
	private static final int MSB = 1 << Character.SIZE-1;
	public boolean isSet(String key, int i) {
        if (key == null) return false;
        
        int charIndex = (int)(i / Character.SIZE);
        if (charIndex >= key.length()) return false;
        
        int bit = (int)(i % Character.SIZE);
        int mask = (MSB >>> bit);
        return (key.charAt(charIndex) & mask) != 0;			
	}
	
	private static final int indexFor(int charIndex, int bitIndex) {
		return (charIndex * Character.SIZE) + 
		       (Integer.numberOfLeadingZeros(bitIndex) - Character.SIZE);
	}
	
	public int firstDiff(String k1, String k2) {
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
}
