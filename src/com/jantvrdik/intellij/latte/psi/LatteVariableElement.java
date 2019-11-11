package com.jantvrdik.intellij.latte.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface LatteVariableElement extends PsiNameIdentifierOwner {

    public abstract String getVariableName();

    public abstract boolean isDefinition();

}