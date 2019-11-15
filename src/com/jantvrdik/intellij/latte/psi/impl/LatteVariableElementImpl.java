package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jantvrdik.intellij.latte.utils.LatteElementFinderUtil;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LatteVariableElementImpl extends ASTWrapperPsiElement implements LatteVariableElement {
    public LatteVariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public boolean isProperty() {
        return this.isStatic();
    }

    public boolean isAnnotation() {
        PsiElement prev = LatteElementFinderUtil.findPrevWithSkippedWhitespaces(this);
        return prev != null && prev.getText().equals("**");
    }

    public boolean isStatic() {
        return LattePhpUtil.isStatic(this);
    }

    public boolean isGlobal() {
        return LattePhpUtil.isGlobal(this);
    }

    public boolean isDefinition()
    {
        if (isAnnotation()) {
            return true;
        }

        if (isProperty()) {
            return false;
        }

        // n:attr support
        PsiElement parent1 = this.getParent();
        PsiElement element = findMacroNameElement(this);

        if (element == null) {
            return false;
        }

        String macroName = element.getText();
        if (macroName.equals("var") || macroName.equals("for")) {
            return true;
        }

        PsiElement prev = LatteElementFinderUtil.findPrevWithSkippedWhitespaces(this.getPrevSibling());
        if (prev == null) {
            prev = LatteElementFinderUtil.findPrevWithSkippedWhitespaces(this);
        }
        return prev != null
                && (
                        (
                                (macroName.equals("foreach") || macroName.equals("n:foreach"))
                                && (prev.getText().equals("as") || prev.getText().equals(">") || prev.getText().equals("[") || prev.getText().equals(","))
                        )
                        || (macroName.equals("n:for") && (prev.getText().equals("\"")))
        );
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

    private static PsiElement findMacroNameElement(PsiElement psiElement) {
        PsiElement parent = psiElement.getParent().getPrevSibling() != null ? psiElement.getParent().getPrevSibling().getPrevSibling() : null;
        if (parent == null) {
            parent = psiElement.getParent().getParent() != null && psiElement.getParent().getParent().getPrevSibling() != null
                    ? psiElement.getParent().getParent().getPrevSibling().getPrevSibling()
                    : null;
        }

        PsiElement element;
        if (parent != null && parent.getNode().getElementType() == LatteTypes.T_HTML_TAG_NATTR_NAME) {
            element = parent;
        } else {
            element = LatteElementFinderUtil.findMacroNameElement(psiElement);
        }
        return element;
    }
}