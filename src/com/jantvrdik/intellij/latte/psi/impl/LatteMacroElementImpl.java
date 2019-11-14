package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.jantvrdik.intellij.latte.psi.LatteMacroElement;
import org.jetbrains.annotations.NotNull;

public abstract class LatteMacroElementImpl extends ASTWrapperPsiElement implements LatteMacroElement {
    public LatteMacroElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}