package com.jantvrdik.intellij.latte.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface LatteVariableTypeElement extends PsiNameIdentifierOwner {

    public abstract String getVariableType();

}