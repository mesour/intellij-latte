package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.jantvrdik.intellij.latte.psi.LattePhpMethodArgsElement;
import org.jetbrains.annotations.NotNull;

public abstract class LattePhpMethodArgsElementImpl extends ASTWrapperPsiElement implements LattePhpMethodArgsElement {
    public LattePhpMethodArgsElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}