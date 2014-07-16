package io.prelink.critbit;

import io.prelink.critbit.sharedbytearray.SBAKeyAnalyzer;
import io.prelink.critbit.sharedbytearray.SharedByteArray;
import io.prelink.critbit.sharedbytearray.ThinSBA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.StringKeyAnalyzer;
import org.junit.Test;

public class CritBitTest extends TestCase {

    private static interface CBWrapper<K> {
        void put(K key, String val);
        void remove(K key);
        AbstractCritBitTree<K,String> get();
    }

    private <K> void commonTests(CBWrapper<K> wrap, Keyifier<K> k) {
        assertTrue(wrap.get().isEmpty());
        assertNull(wrap.get().get(k.key("u")));
        assertNull(wrap.get().min());
        assertNull(wrap.get().max());
        wrap.get().traverse(new AssertDoNothingCursor<K>());
        wrap.get().traverseWithPrefix(k.key("u"), new AssertDoNothingCursor<K>());
        
        wrap.put(k.key("a"), "a");
        assertEquals("a", wrap.get().get(k.key("a")));
        assertNull(wrap.get().get(k.key("b")));
        wrap.remove(k.key("a"));
        assertTrue(wrap.get().isEmpty());

        wrap.put(k.key("a"), "a");
        wrap.put(k.key("b"), "b");
        assertEquals("a", wrap.get().get(k.key("a")));
        assertEquals("b", wrap.get().get(k.key("b")));
        wrap.remove(k.key("a"));
        wrap.remove(k.key("b"));
        assertTrue(wrap.get().isEmpty());
        assertNull(wrap.get().get(k.key("a")));
        assertNull(wrap.get().get(k.key("b")));

        String[] items = {
                "u", "un", "unh", "uni", "unj", "unim", "unin", "unio",
                "uninc", "unind", "unine", "unindd", "uninde", "unindf",
                "unindew", "unindex", "unindey", "a", "z"
        };

        for(String s: items) {
            wrap.put(k.key(s), s);
            assertTrue("Tree must contain key "+s, wrap.get().containsKey(k.key(s)));
            assertTrue("Tree must contain val "+s, wrap.get().containsValue(s));
            assertEquals(s, wrap.get().get(k.key(s)));
        }

        assertFalse(wrap.get().isEmpty());
        assertEquals(items.length, wrap.get().size());
        assertFalse(wrap.get().containsKey(k.key("monkey")));
        assertFalse(wrap.get().containsValue("monkey"));

        assertEquals("a", wrap.get().min().getValue());
        assertEquals("z", wrap.get().max().getValue());

        final List<String> target
            = Arrays.asList(new String[]{"unin", "uninc", "unind", "unindd",
                    "uninde", "unindew", "unindex", "unindey",
                    "unindf", "unine"});

        final List<String> gathered = new ArrayList<String>();
        wrap.get().traverseWithPrefix(k.key("unin"), new ValueListCursor<K>(gathered));
        assertEquals(target, gathered);

        final List<String> filtered = new ArrayList<String>();
        wrap.get().traverseWithPrefix(k.key("unin"),
                new FilterLimitCursor<K>(filtered));
        assertEquals(Arrays.asList(
                new String[]{"unindd","uninde","unindew"}), filtered);

        int size = items.length;
        for(String s: target) {
            wrap.remove(k.key(s));
            assertFalse(wrap.get().containsKey(k.key(s)));
            assertFalse(wrap.get().containsValue(k.key(s)));
            assertNull(wrap.get().get(k.key(s)));
            assertEquals(--size, wrap.get().size());
            wrap.remove(k.key(s));
            assertEquals(size, wrap.get().size());
        }

        assertFalse(wrap.get().isEmpty());
        assertEquals(items.length - target.size(), wrap.get().size());
        assertEquals("a", wrap.get().min().getValue());
        assertEquals("z", wrap.get().max().getValue());

        wrap.get().traverseWithPrefix(k.key("unin"), new AssertDoNothingCursor<K>());
    }

    @Test
    public void testMutable() {
        final MCritBitTree<String, String> test =
            new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);

        commonTests(new MutableCBWrapper<String>(test), skier);

        List<String> iterkeys = new LinkedList<String>();
        List<String> itervals = new LinkedList<String>();
        for(Map.Entry<String, String> e: test.entrySet()) {
        	iterkeys.add(e.getKey());
        	itervals.add(e.getValue());
        }

        List<String> curskeys = new LinkedList<String>();
        List<String> cursvals = new LinkedList<String>();
        test.traverse(new EntryListsCursor<String, String>(curskeys, cursvals));

        assertEquals(curskeys, iterkeys);
        assertEquals(cursvals, itervals);
    }

    @Test
    public void testImmutable() {
        final CritBitTree<String, String> cb =
            new CritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);

        commonTests(new ImmutableCBWrapper<String>(cb), skier);
    }

    @Test
    public void testSBAKey() {
        final MCritBitTree<SharedByteArray, String> test =
            new MCritBitTree<SharedByteArray, String>(SBAKeyAnalyzer.INSTANCE);

        commonTests(new MutableCBWrapper<SharedByteArray>(test), bytekier);
    }

    private static class ImmutableCBWrapper<K> implements CBWrapper<K> {
        private CritBitTree<K, String> test;
        public ImmutableCBWrapper(CritBitTree<K,String> cb) {
            this.test = cb;
        }
        public void put(K key, String val) {
            test = test.put(key, val);
        }
        public void remove(K key) {
            test = test.remove(key);
        }
        public AbstractCritBitTree<K, String> get() {
            return test;
        }
    }
    private static class MutableCBWrapper<K> implements CBWrapper<K> {
        private MCritBitTree<K, String> test;
        public MutableCBWrapper(MCritBitTree<K,String> cb) {
            this.test = cb;
        }
        public void put(K key, String val) {
            test.put(key, val);
        }
        public void remove(K key) {
            test.remove(key);
        }
        public AbstractCritBitTree<K, String> get() {
            return test;
        }
    }

    private static interface Keyifier<K> {
        K key(String s);
    }
    private static Keyifier<String> skier = new Keyifier<String>() {
        public String key(String s) { return s; }
    };
    private static Keyifier<SharedByteArray> bytekier = new Keyifier<SharedByteArray>() {
        public SharedByteArray key(String s) { return sba(s); }
    };

    private static SharedByteArray sba(String s) {
        return new ThinSBA(bytes(s));
    }

    private static byte[] bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("won't happen");
        }
    }

    private class AssertDoNothingCursor<K> implements Cursor<K,String> {
        public Decision select(Map.Entry<? extends K, ? extends String> entry) {
            throw new RuntimeException("Shouldn't do anything");
        }
    }

    private class ValueListCursor<K> implements Cursor<K,String> {
        private final List<String> list;
        public ValueListCursor(List<String> basket) {
            this.list = basket;
        }
        public Decision select(Map.Entry<? extends K, ? extends String> entry) {
            list.add(entry.getValue());
            return Decision.CONTINUE;
        }
    }

    private class EntryListsCursor<K,V> implements Cursor<K,V> {
    	private final List<K> klist;
    	private final List<V> vlist;
        public EntryListsCursor(List<K> klist, List<V> vlist) {
            this.klist = klist;
            this.vlist = vlist;
        }
        public Decision select(Map.Entry<? extends K, ? extends V> entry) {
            klist.add(entry.getKey());
            vlist.add(entry.getValue());
            return Decision.CONTINUE;
        }
    }

    private class FilterLimitCursor<K> implements Cursor<K,String> {
        private final List<String> list;
        private int counter = 0;
        public FilterLimitCursor(List<String> basket) {
            this.list = basket;
        }
        public Decision select(Map.Entry<? extends K, ? extends String> entry) {
            String val = entry.getValue();
            if(val.length() > 5) {
                list.add(val);
                counter++;
            }
            return (counter < 3) ? Decision.CONTINUE : Decision.EXIT;
        }
    }
}
