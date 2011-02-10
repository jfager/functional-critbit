package io.prelink.critbit;

import io.prelink.critbit.CritBitTree;
import io.prelink.critbit.MCritBitTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

//import org.ardverk.collection.PatriciaTrie;
//import org.ardverk.collection.StringKeyAnalyzer;

public class PatriciaTrieTest {
    private static final int WARMUPS = 1000;
    private static final int ITERS = 50000;
    private static final int SEED = 42;

    private static interface Putter {
        String name();
        void put(String key, String val);
    }
    private static String randomString(Random rand) {
        int len = RandomUtils.nextInt(rand, 100) + 1;
        return RandomStringUtils.random(len, 0, 0, false, false, null, rand);
    }
    private static void putTest(Putter p) {
        Random rand = new Random(SEED);
        System.out.print("Warming up " + p.name());
        for(int i=0; i<WARMUPS; i++) {
            String key = randomString(rand);
            p.put(key, ""+i);
        }
        System.out.print(" Starting test...");
        final long start = System.currentTimeMillis();
        for(int i=0; i<ITERS; i++) {
            String key = RandomStringUtils.random(RandomUtils.nextInt(100) + 1);
            p.put(key, ""+i);
        }
        final long end = System.currentTimeMillis();
        System.out.println(
            String.format("Done!  Put %s keys in %s ms", ITERS, end-start));
    }

    private static class FCBFlipper {
        //Since FCB is purely functional, this helper class will handle
        //tracking the current FCB.
        private CritBitTree<String,String> fcb;
        public FCBFlipper() {
            this.fcb = new CritBitTree<String, String>(new StringBitChecker());
        }
        public void put(String key, String val) {
            this.fcb = fcb.put(key, val);
        }
    }

    public static void main(final String[] args) throws Exception {

        final Map<String, String> hm = new HashMap<String, String>();
        putTest(new Putter() {
            public String name() { return "HashMap"; }
            public void put(String k, String v) { hm.put(k, v); }
        });

        final Map<String, String> pshm =
            new HashMap<String, String>(WARMUPS+ITERS);
        putTest(new Putter() {
            public String name() { return "Presized HashMap"; }
            public void put(String k, String v) { pshm.put(k, v); }
        });

        final FCBFlipper fcb = new FCBFlipper();
        putTest(new Putter() {
            public String name() { return "Functional Crit Bit"; }
            public void put(String k, String v) { fcb.put(k, v); }
        });

        final MCritBitTree<String, String> mcb =
            new MCritBitTree<String, String>(new StringBitChecker());
        putTest(new Putter() {
            public String name() { return "Mutable Crit Bit"; }
            public void put(String k, String v) { mcb.put(k, v); }
        });

//      final PatriciaTrie<String, String> ptrie =
//          new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
//      putTest(new Putter() {
//          public String name() { return "Patricia Trie"; }
//          public void put(String k, String v) { ptrie.put(k, v); }
//      });



        //Give us a chance to look at the heap.
        for(int i=0;i<Integer.MAX_VALUE;i++) Thread.sleep(1000);

        //Added to avoid garbage collection.
        System.out.println(hm);
        System.out.println(pshm);
        System.out.println(fcb);
        System.out.println(mcb);
//      System.out.println(ptrie);
    }

}
