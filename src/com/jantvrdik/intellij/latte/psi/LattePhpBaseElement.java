package com.jantvrdik.intellij.latte.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface LattePhpBaseElement extends PsiNameIdentifierOwner {

    public abstract boolean isStatic();

    public abstract boolean isGlobal();

}