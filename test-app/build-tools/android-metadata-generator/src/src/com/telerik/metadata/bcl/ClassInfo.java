package com.telerik.metadata.bcl;

import com.telerik.metadata.ClassUtil;
import com.telerik.metadata.desc.MetadataInfoAnnotationDescriptor;
import com.telerik.metadata.desc.ClassDescriptor;
import com.telerik.metadata.desc.FieldDescriptor;
import com.telerik.metadata.desc.MethodDescriptor;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class ClassInfo implements ClassDescriptor {
    private final JavaClass clazz;

    public ClassInfo(JavaClass clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        if (clazz.getClassName().contains("$")) {
            String name = ClassUtil.getSimpleName(this);
            boolean isAnonymousClass;
            try {
                Integer.parseInt(name);
                isAnonymousClass = true;
            } catch (Exception e) {
                isAnonymousClass = false;
            }
            if (isAnonymousClass) {
                return;
            }
            boolean found = false;
            String fullClassName = getClassName(clazz.getClassNameIndex());
            if (fullClassName == null) {
                return;
            }

            for (Attribute a : clazz.getAttributes()) {
                if (a instanceof InnerClasses) {
                    InnerClass[] i = ((InnerClasses) a).getInnerClasses();
                    for (InnerClass ic : i) {
                        String innerClassName = getClassName(ic.getInnerClassIndex());

                        if (fullClassName.equals(innerClassName)) {
                            int flags = ic.getInnerAccessFlags();
                            clazz.setAccessFlags(flags);
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        }
    }

    private String getClassName(int classIndex) {
        ConstantPool constantPool = clazz.getConstantPool();
        ConstantClass innerClassNameIndex = (ConstantClass)constantPool.getConstant(classIndex);
        if (innerClassNameIndex == null) {
            return null;
        }
        ConstantUtf8 className = (ConstantUtf8)constantPool.getConstant(innerClassNameIndex.getNameIndex());
        if (className == null) {
            return null;
        }
        return className.getBytes();
    }

    @Override
    public boolean isClass() {
        return clazz.isClass();
    }

    @Override
    public boolean isInterface() {
        return clazz.isInterface();
    }

    @Override
    public boolean isEnum() {
        return clazz.isEnum();
    }

    @Override
    public boolean isStatic() {
        return clazz.isStatic();
    }

    @Override
    public boolean isPublic() {
        return clazz.isPublic();
    }

    @Override
    public boolean isProtected() {
        return clazz.isProtected();
    }

    @Override
    public MethodDescriptor[] getMethods() {
        Method[] ms = clazz.getMethods();
        MethodDescriptor[] methods = new MethodDescriptor[ms.length];
        for (int i=0; i<methods.length; i++) {
            methods[i] = new MethodInfo(ms[i]);
        }
        return methods;
    }

    @Override
    public FieldDescriptor[] getFields() {
        Field[] fs = clazz.getFields();
        FieldDescriptor[] fields = new FieldDescriptor[fs.length];
        for (int i=0; i<fields.length; i++) {
            fields[i] = new FieldInfo(fs[i]);
        }
        return fields;
    }

    @Override
    public MetadataInfoAnnotationDescriptor getMetadataInfoAnnotation() {
        return null;
    }

    @Override
    public String[] getInterfaceNames() {
        return clazz.getInterfaceNames();
    }

    @Override
    public String getPackageName() {
        return clazz.getPackageName();
    }

    @Override
    public String getClassName() {
        return clazz.getClassName();
    }

    @Override
    public String getSuperclassName() {
        return clazz.getSuperclassName();
    }
}
