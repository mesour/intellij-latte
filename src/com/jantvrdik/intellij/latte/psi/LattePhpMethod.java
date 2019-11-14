package com.jantvrdik.intellij.latte.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface LattePhpMethod extends PsiNameIdentifierOwner {

    public abstract String getMethodName();

}