/*
 * Copyright 2005-2008 Roger Kapsi, Sam Berlin
 *
 * Modified 2011 Jason Fager
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection;

import io.prelink.critbit.MCritBitTree;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

public class ByteArrayKeyAnalyzerTest {

    private static final int SIZE = 20000;

    @Test
    public void bitSet() {
        byte[] key = toByteArray("10100110", 2);
        ByteArrayKeyAnalyzer ka = new ByteArrayKeyAnalyzer(key.length * 8);

        TestCase.assertTrue(ka.isBitSet(key, 0));
        TestCase.assertFalse(ka.isBitSet(key, 1));
        TestCase.assertTrue(ka.isBitSet(key, 2));
        TestCase.assertFalse(ka.isBitSet(key, 3));
        TestCase.assertFalse(ka.isBitSet(key, 4));
        TestCase.assertTrue(ka.isBitSet(key, 5));
        TestCase.assertTrue(ka.isBitSet(key, 6));
        TestCase.assertFalse(ka.isBitSet(key, 7));
    }

    @Test
    public void keys() {
        MCritBitTree<byte[], BigInteger> trie
            = new MCritBitTree<byte[], BigInteger>(
                    ByteArrayKeyAnalyzer.INSTANCE);

        Map<byte[], BigInteger> map
            = new TreeMap<byte[], BigInteger>(
                    ByteArrayKeyAnalyzer.INSTANCE);

        for (int i = 0; i < SIZE; i++) {
            BigInteger value = BigInteger.valueOf(i);
            byte[] key = toByteArray(value);

            BigInteger existing = trie.put(key, value);
            TestCase.assertNull(existing);

            map.put(key, value);
        }

        TestCase.assertEquals(map.size(), trie.size());

        for (byte[] key : map.keySet()) {
            BigInteger expected = new BigInteger(1, key);
            BigInteger value = trie.get(key);

            TestCase.assertEquals(expected, value);
        }
    }

    private static byte[] toByteArray(String value, int radix) {
        return toByteArray(Long.parseLong(value, radix));
    }

    private static byte[] toByteArray(long value) {
        return toByteArray(BigInteger.valueOf(value));
    }

    private static byte[] toByteArray(BigInteger value) {
        byte[] src = value.toByteArray();
        if (src.length <= 1) {
            return src;
        }

        if (src[0] != 0) {
            return src;
        }

        byte[] dst = new byte[src.length-1];
        System.arraycopy(src, 1, dst, 0, dst.length);
        return dst;
    }
}
