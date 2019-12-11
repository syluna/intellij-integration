package com.jmonkeystore.ide;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;

public class TestCompilerTask implements CompileTask {

    @Override
    public boolean execute(CompileContext context) {
        System.out.println("IM RUNNING A COMPILE TASK");
        return true;
    }
}
