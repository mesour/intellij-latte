package com.jantvrdik.intellij.latte.util;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.reference.ClassReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import gnu.trove.THashMap;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LatteUtil {

	/**
	 * Visit all possible Twig include file pattern
	 */
	public static void visitTemplateIncludes(@NotNull LatteFile latteFile, @NotNull Consumer<ClassReference> consumer) {
		PsiTreeUtil.collectElements(latteFile, psiElement -> {
			if (psiElement instanceof LatteFile) {
				return false;
			}

			// {% include %}
			if (psiElement.getNode().getElementType() == LatteTypes.MACRO_CLASSIC) {
				for (String templateName : getIncludeTagStrings(psiElement)) {
					if (StringUtils.isNotBlank(templateName)) {
						consumer.consume(new ClassReference(psiElement, templateName));
					}
				}
			}

			return true;
		});
	}

	@NotNull
	public static Collection<String> getIncludeTagStrings(@NotNull PsiElement psiElement) {

		//if(twigTagWithFileReference.getNode().getElementType() != TwigElementTypes.INCLUDE_TAG) {
		//	return Collections.emptySet();
		//}
		Set<String> out = new HashSet<>(Collections.emptySet());

		out.add("test");

		return out;
	}

}