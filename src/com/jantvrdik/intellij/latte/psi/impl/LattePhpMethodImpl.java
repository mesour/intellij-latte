package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.jantvrdik.intellij.latte.psi.LattePhpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LattePhpMethodImpl extends ASTWrapperPsiElement implements LattePhpMethod {
    public LattePhpMethodImpl(@NotNull ASTNode node) {
        super(node);
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