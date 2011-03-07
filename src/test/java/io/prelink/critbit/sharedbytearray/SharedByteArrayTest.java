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

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static final int alen = alphabet.length();

    private static final String toss = "toss";
    private static final int tlen = toss.length();

    private void doTest(SharedByteArray sba) {
        assertEquals(26, sba.length());
        assertEquals(bytes("a")[0], sba.byteAt(0));
        assertEquals(bytes("d")[0], sba.byteAt(3));
        assertEquals(bytes("z")[0], sba.byteAt(25));
        assertEquals(-1, sba.indexOf(bytes("monkey")));
        assertEquals(0, sba.indexOf(bytes("abc")));
        assertEquals(0, sba.indexOf(bytes("a")));
        assertEquals(0, sba.indexOf(bytes(alphabet)));
        assertEquals(23, sba.indexOf(bytes("xyz")));
        assertEquals(25, sba.indexOf(bytes("z")));
        assertEquals(3, sba.indexOf(bytes("def")));
        assertEquals(3, sba.indexOf(bytes("d")));
        assertEquals(-1, sba.indexOf(bytes("")));
        assertEquals(-1, sba.indexOf(bytes("aa")));
        assertEquals(-1, sba.indexOf(bytes("dd")));
        assertEquals(-1, sba.indexOf(bytes("zz")));
        assertTrue(Arrays.equals(bytes(alphabet), sba.toByteArray()));
    }

    @Test
    public void testThin() throws Exception {
        SharedByteArray sba = new ThinSBA(bytes(alphabet));
        assertTrue(sba instanceof ThinSBA);
        doTest(sba);
    }

    @Test
    public void testThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet)).sub(tlen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testThickThick() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + toss + alphabet))
                .sub(tlen).sub(tlen);
        assertTrue(sba instanceof ThickSBA);
        doTest(sba);
    }

    @Test
    public void testThicker() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet + toss))
                .sub(tlen, tlen+alen);
        assertTrue(sba instanceof ThickerSBA);
        doTest(sba);
    }

    @Test
    public void testThickerThicker() throws Exception {
        SharedByteArray sba =
            new ThinSBA(bytes(toss + alphabet + toss + toss))
                .sub(tlen, tlen+alen+tlen)
                .sub(0, alen);
        assertTrue(sba instanceof ThickerSBA);
        doTest(sba);
    }
}
