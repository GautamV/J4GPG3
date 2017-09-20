package com.j4gpg3.control;

public class Func {

	public static byte[] concatenate (byte[] a, byte[] b){
		int aLen = a.length;
		int bLen = b.length;
		byte[] c = new byte[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen); 
		return c;
	}
}
