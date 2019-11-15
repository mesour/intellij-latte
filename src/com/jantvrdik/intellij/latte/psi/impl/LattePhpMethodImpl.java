package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.jantvrdik.intellij.latte.psi.LattePhpMethod;
import com.jantvrdik.intellij.latte.utils.LattePhpVariableType;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LattePhpMethodImpl extends ASTWrapperPsiElement implements LattePhpMethod {
    public LattePhpMethodImpl(@NotNull ASTNode node) {
        super(node);
    }

    public boolean isStatic() {
        return LattePhpUtil.isStatic(this);
    }

    public boolean isGlobal() {
        return LattePhpUtil.isGlobal(this);
    }

    @Nullable
    public LattePhpVariableType getReturnType() {
        return LatteUtil.findVariableType(this);
    }

    @Nullable
    public PsiReference getReference() {
        PsiReference[] references = getReferences();
        return references.length == 0 ? null : references[0];
    }

    @NotNull
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}