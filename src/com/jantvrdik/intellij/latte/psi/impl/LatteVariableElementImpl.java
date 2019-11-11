package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LatteVariableElementImpl extends ASTWrapperPsiElement implements LatteVariableElement {
    public LatteVariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public boolean isDefinition()
    {
        return this.getParent() != null
                && this.getParent().getPrevSibling() != null
                && this.getParent().getPrevSibling().getText().equals("var");
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