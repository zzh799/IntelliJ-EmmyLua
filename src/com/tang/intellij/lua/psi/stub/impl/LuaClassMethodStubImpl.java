package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaClassMethodStub;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubImpl extends StubBase<LuaClassMethodDef> implements LuaClassMethodStub {

    private String className;

    public LuaClassMethodStubImpl(String className, StubElement parent) {
        super(parent, LuaElementType.CLASS_METHOD_DEF);
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
