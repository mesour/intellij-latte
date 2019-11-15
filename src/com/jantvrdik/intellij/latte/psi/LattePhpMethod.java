package com.jantvrdik.intellij.latte.psi;

import com.jantvrdik.intellij.latte.utils.LattePhpVariableType;
import org.jetbrains.annotations.Nullable;

public interface LattePhpMethod extends LattePhpBaseElement {

    public abstract String getMethodName();

    @Nullable
    public LattePhpVariableType getReturnType();

}