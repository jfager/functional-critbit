package io.prelink.critbit;

import io.prelink.critbit.sharedbytearray.SBAKeyAnalyzer;
import io.prelink.critbit.sharedbytearray.SharedByteArray;
import io.prelink.critbit.sharedbytearray.ThinSBA;

import java.util.ArrayList;
import java.util.Arrays;
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
        AbstractCritBitTree<K,String> getCB();
    }

    private <K> void commonTests(CBWrapper<K> wrapper, Keyifier<K> k) {
        AbstractCritBitTree<K,String> cb = wrapper.getCB();
        assertTrue(cb.isEmpty());
        assertNull(cb.get(k.key("u")));
        cb.traverse(new AssertDoNothingCursor<K>());
        cb.traverseWithPrefix(k.key("u"), new AssertDoNothingCursor<K>());

        String[] items = {
                "u", "un", "unh", "uni", "unj", "unim", "unin", "unio",
                "uninc", "unind", "unine", "unindd", "uninde", "unindf",
                "unindew", "unindex", "unindey", "a", "z"
        };

        for(String s: items) {
            wrapper.put(k.key(s), s);
            cb = wrapper.getCB();
            assertTrue("Tree must contain key "+s, cb.containsKey(k.key(s)));
            assertTrue("Tree must contain val "+s, cb.containsValue(s));
            assertEquals(s, cb.get(k.key(s)));
        }

        assertFalse(cb.isEmpty());
        assertEquals(items.length, cb.size());
        assertFalse(cb.containsKey(k.key("monkey")));
        assertFalse(cb.containsValue("monkey"));

        assertEquals("a", cb.min().getValue());
        assertEquals("z", cb.max().getValue());

        final List<String> target
            = Arrays.asList(new String[]{"unin", "uninc", "unind", "unindd",
                    "uninde", "unindew", "unindex", "unindey",
                    "unindf", "unine"});

        final List<String> gathered = new ArrayList<String>();
        cb.traverseWithPrefix(k.key("unin"), new ValueListCursor<K>(gathered));
        assertEquals(target, gathered);

        final List<String> filtered = new ArrayList<String>();
        cb.traverseWithPrefix(k.key("unin"),
                new FilterLimitCursor<K>(filtered));
        assertEquals(Arrays.asList(
                new String[]{"unindd","uninde","unindew"}), filtered);

        for(String s: target) {
            wrapper.remove(k.key(s));
            cb = wrapper.getCB();
            assertFalse(cb.containsKey(k.key(s)));
            assertFalse(cb.containsValue(k.key(s)));
            assertNull(cb.get(k.key(s)));
        }

        cb = wrapper.getCB();
        assertFalse(cb.isEmpty());
        assertEquals(items.length - target.size(), cb.size());
        assertEquals("a", cb.min().getValue());
        assertEquals("z", cb.max().getValue());

        cb.traverseWithPrefix(k.key("unin"), new AssertDoNothingCursor<K>());
    }

    @Test
    public void testMutable() {
        final MCritBitTree<String, String> test =
            new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);

        commonTests(new MutableCBWrapper<String>(test), skier);
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
        public AbstractCritBitTree<K, String> getCB() {
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
        public AbstractCritBitTree<K, String> getCB() {
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
