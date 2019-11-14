package com.jantvrdik.intellij.latte.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Complete class names
 */
public class ClassCompletionProvider extends CompletionProvider<CompletionParameters> {

	public ClassCompletionProvider() {
		super();
	}

	@Override
	protected void addCompletions(
			@NotNull CompletionParameters params,
			ProcessingContext ctx,
			@NotNull CompletionResultSet results
	) {
		PsiElement curr = params.getPosition().getOriginalElement();
		boolean incompleteKey = isIncompleteKey(curr);
		if (!incompleteKey) {
			return;
		}

		PrefixMatcher prefixMatcher = results.getPrefixMatcher();
		String prefix = prefixMatcher.getPrefix();
		String namespace = null;
		if (prefix.contains("\\")) {
			int index = prefix.lastIndexOf("\\");
			//namespace = prefix.substring(0, index);
			prefixMatcher = prefixMatcher.cloneWithPrefix(prefix.substring(index + 1));
		}

		Project project = params.getPosition().getProject();
		Collection<String> classNames = LattePhpUtil.getAllExistingClassNames(project, prefixMatcher);
		Collection<PhpNamedElement> variants = LattePhpUtil.getAllClassNamesAndInterfaces(project, classNames);

		// Add variants
		for (PhpNamedElement item : variants) {
			PhpLookupElement lookupItem = new PhpLookupElement(item) {
				@Override
				public Set<String> getAllLookupStrings() {
					Set<String> original = super.getAllLookupStrings();
					Set<String> strings = new HashSet<String>(original.size() + 1);
					strings.addAll(original);
					strings.add(this.getNamedElement().getFQN());
					return strings;
				}
			};
			lookupItem.handler = PhpReferenceInsertHandler.getInstance();

			results.addElement(lookupItem);
		}
	}

	private static boolean isIncompleteKey(PsiElement el) {
		ASTNode node = el.getNode();
		if (node.getElementType() == LatteTypes.T_MACRO_ARGS_VAR_TYPE || node.getElementType() == LatteTypes.T_MACRO_ARGS) {
			return true;
		}
		return false;
	}

}