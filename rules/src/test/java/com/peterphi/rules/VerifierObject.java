package com.peterphi.rules;

/**
 * Created by bmcleod on 08/09/2016.
 */
public class VerifierObject
{
	private int p = 0;
	private int f = 0;


	public void pass(int i)
	{
		p += i;
	}


	public void fail(int i)
	{
		f += i;
	}


	public int getP()
	{
		return p;
	}


	public int getF()
	{
		return f;
	}
}
