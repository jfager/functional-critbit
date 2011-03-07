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


    @Test
    public void testTraverse() {
        MCritBitTree<String, String> intTrie =
            new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);

        intTrie.put("u", "u");
        intTrie.put("un", "un");
        intTrie.put("unh", "unh");
        intTrie.put("uni", "uni");
        intTrie.put("unj", "unj");
        intTrie.put("unim", "unim");
        intTrie.put("unin", "unin");
        intTrie.put("unio", "unio");
        intTrie.put("uninc", "uninc");
        intTrie.put("unind", "unind");
        intTrie.put("unine", "unine");
        intTrie.put("unindd", "unindd");
        intTrie.put("uninde", "uninde");
        intTrie.put("unindf", "unindf");
        intTrie.put("unindew", "unindew");
        intTrie.put("unindex", "unindex");
        intTrie.put("unindey", "unindey");
        intTrie.put("a", "a");
        intTrie.put("z", "z");

        final List<String> target
            = Arrays.asList(new String[]{"unin", "uninc", "unind", "unindd",
                    "uninde", "unindew", "unindex", "unindey",
                    "unindf", "unine"});

        final List<String> gathered = new ArrayList<String>();
        intTrie.traverseWithPrefix("unin", new ValueListCursor<String, String>(gathered));
        assertEquals(target, gathered);
    }

    @Test
    public void testSBA() {
        MCritBitTree<SharedByteArray, String> intTrie =
            new MCritBitTree<SharedByteArray, String>(SBAKeyAnalyzer.INSTANCE);

        intTrie.put(sba("u"), "u");
        intTrie.put(sba("un"), "un");
        intTrie.put(sba("unh"), "unh");
        intTrie.put(sba("uni"), "uni");
        intTrie.put(sba("unj"), "unj");
        intTrie.put(sba("unim"), "unim");
        intTrie.put(sba("unin"), "unin");
        intTrie.put(sba("unio"), "unio");
        intTrie.put(sba("uninc"), "uninc");
        intTrie.put(sba("unind"), "unind");
        intTrie.put(sba("unine"), "unine");
        intTrie.put(sba("unindd"), "unindd");
        intTrie.put(sba("uninde"), "uninde");
        intTrie.put(sba("unindf"), "unindf");
        intTrie.put(sba("unindew"), "unindew");
        intTrie.put(sba("unindex"), "unindex");
        intTrie.put(sba("unindey"), "unindey");
        intTrie.put(sba("a"), "a");
        intTrie.put(sba("z"), "z");

        final List<String> target
            = Arrays.asList(new String[]{"unin", "uninc", "unind", "unindd",
                    "uninde", "unindew", "unindex", "unindey",
                    "unindf", "unine"});

        final List<String> gathered = new ArrayList<String>();
        intTrie.traverseWithPrefix(sba("unin"),
                new ValueListCursor<SharedByteArray, String>(gathered));
        assertEquals(target, gathered);
    }

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

    private class ValueListCursor<K,V> implements Cursor<K,V> {
        private final List<V> list;
        public ValueListCursor(List<V> basket) {
            this.list = basket;
        }
        public Decision select(Map.Entry<? extends K, ? extends V> entry) {
            list.add(entry.getValue());
            return Decision.CONTINUE;
        }
    }
}
