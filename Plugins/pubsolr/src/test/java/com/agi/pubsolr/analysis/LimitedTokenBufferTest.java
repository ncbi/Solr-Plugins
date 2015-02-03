package com.agi.pubsolr.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

import com.agi.pubsolr.analysis.LimitedTokenBuffer;

public class LimitedTokenBufferTest {
	
	@Test
	public void testSimpleProperties() throws Exception {
		LimitedTokenBuffer buf = new LimitedTokenBuffer(3);
		assertTrue(buf.isEmpty());
		StringBuilder out = new StringBuilder();
		char[] a = "0123456789".toCharArray();
		
		buf.add(a, 0, 3, 100, 105);
		buf.extract(0, 1, out);
		assertEquals("012", out.toString());
		
		buf.add(a, 3, 4, 200, 204);
		buf.extract(0, 1, out);
		assertEquals("012", out.toString());
		buf.extract(0, 2, out);
		assertEquals("012 3456", out.toString());
		buf.extract(1, 2, out);
		assertEquals("3456", out.toString());
		
		buf.add(a, 7, 2, 300, 302);
		buf.extract(0, 1, out);
		assertEquals("012", out.toString());
		buf.extract(0, 2, out);
		assertEquals("012 3456", out.toString());
		buf.extract(0, 3, out);
		assertEquals("012 3456 78", out.toString());
		
		buf.add(a, 9, 1, 400, 401);
		buf.extract(0, 1, out);
		assertEquals("3456", out.toString());
		buf.extract(0, 2, out);
		assertEquals("3456 78", out.toString());
		buf.extract(0, 3, out);
		assertEquals("3456 78 9", out.toString());
		buf.extract(2, 3, out);
		assertEquals("9", out.toString());
	}

}
