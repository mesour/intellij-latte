package com.jantvrdik.intellij.latte.reference;

import com.intellij.codeInsight.lookup.*;
import com.intellij.model.SymbolResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import org.jetbrains.annotations.*;

import java.util.*;

public class LatteVariableReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
	private String key;

	public LatteVariableReference(@NotNull LatteVariableElement element, TextRange textRange) {
		super(element, textRange);
		key = element.getVariableName();
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		Project project = myElement.getProject();
		final List<LatteVariableElement> properties = LatteUtil.findVariablesInFile(project, getElement().getContainingFile().getVirtualFile(), key);
		List<ResolveResult> results = new ArrayList<ResolveResult>();
		for (LatteVariableElement property : properties) {
			results.add(new PsiElementResolveResult(property));
		}
		return results.toArray(new ResolveResult[results.size()]);
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		Project project = myElement.getProject();
		List<LatteVariableElement> properties = LatteUtil.findVariables(project);
		List<LookupElement> variants = new ArrayList<LookupElement>();
		for (final LatteVariableElement property : properties) {
			if (property.getName() != null && property.getName().length() > 0) {
				variants.add(LookupElementBuilder.create(property).
						//withIcon(SimpleIcons.FILE).
						withTypeText(property.getContainingFile().getName())
				);
			}
		}
		return variants.toArray();
	}

}