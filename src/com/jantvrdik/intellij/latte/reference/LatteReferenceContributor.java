package com.jantvrdik.intellij.latte.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.LatteLanguage;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jantvrdik.intellij.latte.psi.impl.LatteVariableElementImpl;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class LatteReferenceContributor extends PsiReferenceContributor {
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		registrar.registerReferenceProvider(
				//PlatformPatterns.psiElement(PsiLiteralExpression.class),
				//PlatformPatterns.psiElement().withElementType(LatteTypes.ARGS_VAR),
				//PlatformPatterns.psiElement(PsiLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
				//PlatformPatterns.psiElement(LatteTypes.T_MACRO_CLASSIC),
				//PlatformPatterns.psiElement(),
				PlatformPatterns.psiElement(LatteTypes.ARGS_VAR),
				//PlatformPatterns.psiElement(LatteVariableElement.class),
				//PlatformPatterns.psiElement(),
				//StandardPatterns.instanceOf(PsiElement.class),
				//StandardPatterns.instanceOf(PsiElement.class),
				new PsiReferenceProvider() {
					@NotNull
					@Override
					public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
						if (!(element instanceof LatteVariableElement)) {
							return PsiReference.EMPTY_ARRAY;
						}

						LatteVariableElement variableElement = (LatteVariableElement) element;
						String value = variableElement.getVariableName();
						if (value != null && value.startsWith("$")) {
							return new PsiReference[]{
									new LatteVariableReference((LatteVariableElement) element, new TextRange(0, value.length()))};
						}
						// ClassConstantReference
						return PsiReference.EMPTY_ARRAY;
					}
				});
	}
}