package io.prelink.critbit.sharedbytearray;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

public class SharedByteArrayTest extends TestCase {

    private static byte[] bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("won't happen");
        }
    }

    private static byte b(char c) {
        return (byte)c;
    }

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static final int alen = alphabet.length();

    private static final String as = "aaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final String toss = "toss";
    private static final int tlen = toss.length();

    private void doTest(SharedByteArray sba) {
        assertEquals(26, sba.length());
        assertEquals(sba, sba);
        assertEquals(new ThinSBA(bytes(alphabet)), sba);
        assertFalse(sba.equals(null));
        assertFalse(sba.equals(new ThinSBA(bytes(toss))));
        assertFalse(sba.equals(new ThinSBA(bytes(as))));
        assertEquals(bytes("a")[0], sba.byteAt(0));
        assertEquals(bytes("d")[0], sba.byteAt(3));
        assertEquals(bytes("z")[0], sba.byteAt(25));
        assertEquals(-1, sba.indexOf(bytes("monkey"), 0));
        assertEquals(0, sba.indexOf(bytes("abc"), 0));
        assertEquals(0, sba.indexOf(bytes("a"), 0));
        assertEquals(0, sba.indexOf(b('a'), 0));
        assertEquals(0, sba.indexOf(bytes(alphabet), 0));
        assertEquals(-1, sba.indexOf(bytes(alphabet+toss), 0));
        assertEquals(23, sba.indexOf(bytes("xyz"), 0));
        assertEquals(25, sba.indexOf(bytes("z"), 0));
        assertEquals(25, sba.indexOf(b('z'), 0));
        assertEquals(3, sba.indexOf(bytes("def"), 0));
        assertEquals(3, sba.indexOf(bytes("d"), 0));
        assertEquals(3, sba.indexOf(b('d'), 0));
        assertEquals(-1, sba.indexOf(bytes(""), 0));
        assertEquals(-1, sba.indexOf(bytes("aa"), 0));
        assertEquals(-1, sba.indexOf(bytes("dd"), 0));
        assertEquals(-1, sba.indexOf(bytes("zz"), 0));
        assertEquals(-1, sba.indexOf(b('9'), 0));
        assertEquals(-1, sba.lastIndexOf(bytes("monkey"), 26));
        assertEquals(0, sba.lastIndexOf(bytes("abc"), 26));
        assertEquals(0, sba.lastIndexOf(bytes("a"), 26));
        assertEquals(0, sba.lastIndexOf(b('a'), 26));
        assertEquals(0, sba.lastIndexOf(bytes(alphabet), 26));
        assertEquals(-1, sba.lastIndexOf(bytes(alphabet+toss), 26));
        assertEquals(23, sba.lastIndexOf(bytes("xyz"), 26));
        assertEquals(25, sba.lastIndexOf(bytes("z"), 26));
        assertEquals(25, sba.lastIndexOf(b('z'), 26));
        assertEquals(3, sba.lastIndexOf(bytes("def"), 26));
        assertEquals(3, sba.lastIndexOf(bytes("d"), 26));
        assertEquals(3, sba.lastIndexOf(b('d'), 26));
        assertEquals(-1, sba.lastIndexOf(bytes(""), 26));
        assertEquals(-1, sba.lastIndexOf(bytes("aa"), 26));
        assertEquals(-1, sba.lastIndexOf(bytes("dd"), 26));
        assertEquals(-1, sba.lastIndexOf(bytes("zz"), 26));
        assertEquals(-1, sba.lastIndexOf(b('9'), 0));
        assertTrue(Arrays.equals(bytes(alphabet), sba.toByteArray()));
        assertTrue(sba.sub(0,0) == EmptySBA.INSTANCE);
        assertTrue(sba.sub(0,26) == sba);
        assertFalse(new ThinSBA(bytes("abc")).equals(sba));
        assertEquals(new ThinSBA(bytes("abcde")), sba.prefix(5));
        assertEquals(new ThinSBA(bytes("abcdefghij")), sba.prefix(10));
        assertEquals(new ThinSBA(bytes("abcdefghijklmno")), sba.prefix(15));
        assertEquals(new ThinSBA(bytes("klmnopqrstuvwxyz")), sba.suffix(10));
        assertEquals(new ThinSBA(bytes("uvwxyz")), sba.suffix(20));
        assertEquals(new ThinSBA(bytes("cde")), sba.sub(2, 5));
        assertEquals(new ThinSBA(bytes("fghijklmnopqrst")), sba.sub(5, 20));
        assertEquals(new ThinSBA(bytes("klmnopqrst")), sba.sub(10, 20));
        assertEquals(new ThinSBA(bytes("mnopqrst")), sba.sub(12, 20));

        byte[] target = new byte[5];
        sba.toByteArray(0, target, 0, 0);
        assertTrue(Arrays.equals(new byte[5], target));

        sba.toByteArray(0, target, 0, 5);
        assertTrue(Arrays.equals(bytes("abcde"), target));

        sba.toByteArray(21, target, 0, 5);
        assertTrue(Arrays.equals(bytes("vwxyz"), target));

        try {
            sba.byteAt(-1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.byteAt(26);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.sub(-1, 26);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.sub(4, 27);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.sub(10, 5);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(-1, new byte[1], 0, 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(26, new byte[1], 0, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[1], 0, 2);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[1], 1, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[26], -1, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[26], 0, -1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[26], 27, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[26], 0, 27);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, new byte[26], 1, 26);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(1, new byte[26], 0, 26);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
    }

    @Test
    public void testThin() throws Exception {
        SharedByteArray sba = new ThinSBA(bytes(alphabet));
        assertTrue(sba instanceof ThinSBA);
        doTest(sba);
    }

    @Test
    public void testSuffix() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet)).suffix(tlen);
        assertTrue(sba instanceof SuffixSBA);
        doTest(sba);
    }

    @Test
    public void testSuffixSuffix() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + toss + alphabet))
                .suffix(tlen).suffix(tlen);
        assertTrue(sba instanceof SuffixSBA);
        doTest(sba);
    }

    @Test
    public void testPrefix() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(alphabet + toss)).prefix(alen);
        assertTrue(sba instanceof PrefixSBA);
        doTest(sba);
    }

    @Test
    public void testPrefixPrefix() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(alphabet + toss + toss))
                .prefix(alen + tlen).prefix(alen);
        assertTrue(sba instanceof PrefixSBA);
        doTest(sba);
    }

    @Test
    public void testThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet + toss))
                .sub(tlen, tlen+alen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testPrefixThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet + toss))
                .prefix(tlen+alen)
                .suffix(tlen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testSuffixThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet + toss))
                .suffix(tlen)
                .prefix(alen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testThickThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + toss + alphabet + toss + toss))
                .sub(tlen, tlen+tlen+alen+tlen)
                .sub(tlen, tlen+alen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testJoin() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(alphabet.substring(0, 10)))
            .append(new ThinSBA(bytes(alphabet.substring(10))));

        assertTrue(sba instanceof JoinedSBA);
        doTest(sba);
    }

    @Test
    public void testEmpty() throws Exception {
        SharedByteArray sba = EmptySBA.INSTANCE;
        String str = "";

        assertEquals(str.length(), sba.length());
        assertEquals(str.indexOf(""), sba.indexOf(bytes(""), 0));
        assertEquals(str.indexOf(""), sba.indexOf(new ThinSBA(bytes("")), 0));
        assertEquals(str.indexOf("something"), sba.indexOf(bytes("something"), 0));
        assertEquals(str.indexOf("something"), sba.indexOf(new ThinSBA(bytes("something")), 0));
        assertEquals(str.indexOf("s"), sba.indexOf(b('s'), 0));
        assertEquals(str.lastIndexOf(""), sba.lastIndexOf(bytes(""), 0));
        assertEquals(str.lastIndexOf(""), sba.lastIndexOf(new ThinSBA(bytes("")), 0));
        assertEquals(str.lastIndexOf("something"), sba.lastIndexOf(bytes("something"), 0));
        assertEquals(str.lastIndexOf("something"), sba.lastIndexOf(new ThinSBA(bytes("something")), 0));
        assertEquals(str.lastIndexOf("s"), sba.lastIndexOf(b('s'), 0));
        assertEquals(str.substring(0,0), "");
        assertEquals(sba.sub(0,0), EmptySBA.INSTANCE);
        assertEquals(sba.prefix(0), EmptySBA.INSTANCE);
        assertEquals(sba.suffix(0), EmptySBA.INSTANCE);
        assertEquals(0, sba.toByteArray().length);
        SharedByteArray appended = new ThinSBA(bytes(toss));
        assertTrue(sba.append(appended) == appended);
        try {
            str.charAt(0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.byteAt(0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            str.substring(0,1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.sub(0,1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.prefix(1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            str.substring(1,1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.sub(1,1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.suffix(1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}

        byte[] target = new byte[5];
        try {
            sba.toByteArray(1, target, 0, 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(-1, target, 0, 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, target, -1, 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, target, 0, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, target, 0, -1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        try {
            sba.toByteArray(0, target, 6, 0); //Same behavior as System.arraycopy
            fail("Should have thrown IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {}
        sba.toByteArray(0, target, 0, 0);
        sba.toByteArray(0, target, 1, 0);
        sba.toByteArray(0, target, 5, 0);  //Same behavior as System.arraycopy

    }
}
