package com.tfc.bytecode.loading;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForceLoad {
	private static final Method m;
	
	static {
		try {
			m = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void forceLoad(ClassLoader loader, byte[] bytes) throws InvocationTargetException, IllegalAccessException {
		m.setAccessible(true);
		m.invoke(loader, bytes, 0, bytes.length);
	}
	
	@CallerSensitive
	public static void forceLoad(byte[] bytes) throws InvocationTargetException, IllegalAccessException {
		Class<?> caller = Reflection.getCallerClass();
		m.setAccessible(true);
		m.invoke(caller.getClassLoader(), bytes, 0, bytes.length);
	}
}
