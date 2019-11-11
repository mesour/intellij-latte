package com.jantvrdik.intellij.latte.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.dic.ClassReference;
import com.jantvrdik.intellij.latte.stubs.indexes.LatteStubIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
/*
		psiReferenceRegistrar.registerReferenceProvider(PhpElementsUtil.getMethodWithFirstStringPattern(), new PhpStringLiteralExpressionReference(EventDispatcherEventReference.class)
				.addCall("\\Symfony\\Component\\EventDispatcher\\EventDispatcherInterface", "dispatch")
		);
*/
		psiReferenceRegistrar.registerReferenceProvider(
				PlatformPatterns.psiElement(PsiElement.class),
				new PsiReferenceProvider() {
					@NotNull
					@Override
					public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
																 @NotNull ProcessingContext
																		 context) {
				/*		LatteMacro literalExpression = (LatteMacro) element;
						String value = literalExpression.getValue() instanceof String ?
								(String) literalExpression.getValue() : null;
						if (value != null && value.startsWith("simple" + ":")) {
							return new PsiReference[]{
									new SimpleReference(element, new TextRange(8, value.length() + 1))};
						}*/
						return PsiReference.EMPTY_ARRAY;
					}
				}
		);
		psiReferenceRegistrar.registerReferenceProvider(
				//PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
				//PlatformPatterns.psiElement(PhpClassImpl.class).withLanguage(PhpLanguage.INSTANCE),
				StandardPatterns.instanceOf(PsiElement.class),
				new PsiReferenceProvider() {
					@NotNull
					@Override
					public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                        Set<String> templates = new HashSet<>();

                        Project project = psiElement.getProject();
                        FileBasedIndex.getInstance()
								.getAllKeys(
								        LatteStubIndex.KEY,
                                        project
                                ).forEach((templateName) -> templates.add(templateName));

                        if (templates.size() > 0) {
                            //PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                            //String value = literalExpression.getValue() instanceof String ?
                            //        (String) literalExpression.getValue() : null;

                            String value = "todo";
                            //return new PsiReference[]{ new ClassReference((StringLiteralExpression) psiElement) };
                        }

						//return new PsiReference[]{ new ClassReference((StringLiteralExpression) psiElement) };
						return new PsiReference[0];
/*
						if (!Symfony2ProjectComponent.isEnabled(psiElement)) {
							return new PsiReference[0];
						}

						if (!phpStringLiteralExpressionClassReference("\\Symfony\\Component\\DependencyInjection\\Reference", 0, psiElement) &&
								!phpStringLiteralExpressionClassReference("\\Symfony\\Component\\DependencyInjection\\Alias", 0, psiElement) &&
								!phpStringLiteralExpressionClassReference("\\Symfony\\Component\\DependencyInjection\\DefinitionDecorator", 0, psiElement)
						) {
							return new PsiReference[0];
						}

						return new PsiReference[]{ new ServiceReference((StringLiteralExpression) psiElement, true) };*/
					}
				}
		);
	}
/*
	private static boolean phpStringLiteralExpressionClassReference(String signature, int index, PsiElement psiElement) {

		if (!(psiElement.getContext() instanceof ParameterList)) {
			return false;
		}

		ParameterList parameterList = (ParameterList) psiElement.getContext();
		if (parameterList == null || !(parameterList.getContext() instanceof NewExpression)) {
			return false;
		}

		if(PsiElementUtils.getParameterIndexValue(psiElement) != index) {
			return false;
		}

		NewExpression newExpression = (NewExpression) parameterList.getContext();
		ClassReference classReference = newExpression.getClassReference();
		if(classReference == null) {
			return false;
		}

		return classReference.getSignature().equals("#C" + signature);
	}
*/
}