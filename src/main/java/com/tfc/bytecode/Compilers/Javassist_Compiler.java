package com.tfc.bytecode.Compilers;

import com.tfc.bytecode.utils.Parser;
import com.tfc.bytecode.utils.class_structure.*;
import com.tfc.bytecode.utils.Formatter;
import javassist.*;

import java.io.IOException;
import java.util.ArrayList;

public class Javassist_Compiler {
	public Javassist_Compiler() {
	}
	
	public byte[] compile(String name, String superName, ArrayList<FieldNode> fields, ArrayList<MethodNodeSource> methods) throws CannotCompileException, IOException, NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass(name);
		if (superName.equals("")) superName = "java.lang.Object";
		cc.setSuperclass(pool.getCtClass(superName));
		for (FieldNode node : fields)
			cc.addField(CtField.make(new FieldNodeSource(node).code, cc));
		for (MethodNodeSource node : methods)
			cc.addMethod(CtNewMethod.make(node.code, cc));
		return cc.toBytecode();
	}
	
	//Object must be a string, I just had to do it this way because elsewise the signatures would be the same
	public byte[] compile(Object name, String superName, ArrayList<FieldNodeSource> fields, ArrayList<MethodNodeSource> methods) throws CannotCompileException, IOException, NotFoundException {
		if (!(name instanceof String))
			throw new RuntimeException(new IllegalArgumentException("Name should be an instance of a string."));
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass((String) name);
		if (superName.equals("")) superName = "java.lang.Object";
		cc.setSuperclass(pool.getCtClass(superName));
		for (FieldNodeSource node : fields)
			cc.addField(CtField.make(node.code, cc));
		for (MethodNodeSource node : methods)
			cc.addMethod(CtNewMethod.make(node.code, cc));
		return cc.toBytecode();
	}
	
	public byte[] compile(ClassNode classNode) throws CannotCompileException, IOException, NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc;
		if (classNode.isInterface) cc = pool.makeInterface(classNode.name);
		else cc = pool.makeClass(classNode.name);
		cc.setModifiers(classNode.modifs);
		cc.setSuperclass(pool.getCtClass(classNode.superName));
		for (String interf : classNode.interfaces)
			cc.addInterface(pool.getCtClass(interf));
		for (FieldNodeSource node : classNode.fields)
			try {
				cc.addField(CtField.make(node.code, cc));
			} catch (Throwable err) {
				CtClass ctClass = null;
				String type = node.getType();
				if (type.equals(classNode.name))
					ctClass = cc;
				else
					pool.get(node.getType());
				assert ctClass != null;
				cc.addField(new CtField(ctClass, node.getName(), cc));
			}
		for (MethodNodeSource node : classNode.methods)
			cc.addMethod(CtNewMethod.make(node.code, cc));
		for (ConstructorNodeSource node : classNode.constructors)
			cc.addConstructor(CtNewConstructor.make(node.code, cc));
		return cc.toBytecode();
	}
	
	//T0DO: create a full runtime compiler using javassist
	//Whoops, I did it already
	public byte[] compile(String src) throws NotFoundException, CannotCompileException, IOException {
		src = Formatter.formatForCompile(src);
		ClassNode node = Parser.parse(src);
		return compile(node);
	}
}
