package com.mediasmiths.std.guice.hibernate.usertype;

import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.Timecode;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class TimecodeUserTypeTest
{
	private final Timecode TC = Timecode.getInstance("01:02:03:04", Framerate.HZ_25);

	private final TimecodeUserType svc = TimecodeUserType.INSTANCE;


	@Test
	public void testSqlTypes() throws Exception
	{
		assertEquals(Types.VARCHAR, svc.sqlTypes()[0]);
	}


	@Test
	public void testReturnedClass() throws Exception
	{
		assertEquals(Timecode.class, svc.returnedClass());
	}


	@Test
	public void testIsMutable() throws Exception
	{
		assertEquals(false, svc.isMutable());
	}


	@Test
	public void testDisassemble() throws Exception
	{
		assertEquals("01:02:03:04@25", svc.disassemble(TC));
	}


	@Test
	public void testAssemble() throws Exception
	{
		assertEquals(TC, svc.assemble("01:02:03:04@25", null));
	}
}
