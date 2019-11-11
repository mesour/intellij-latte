package com.jantvrdik.intellij.latte.usages;

import com.intellij.lang.HelpID;
import com.intellij.lang.LangBundle;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.jetbrains.php.lang.findUsages.PhpWordsScanner;
import org.jetbrains.annotations.NotNull;

/**
 * @author cdr
 */
public class PropertiesFindUsagesProvider implements FindUsagesProvider {
	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
		return psiElement instanceof PsiNamedElement;
	}

	@Override
	public String getHelpId(@NotNull PsiElement psiElement) {
		return HelpID.FIND_OTHER_USAGES;
	}

	@Override
	@NotNull
	public String getType(@NotNull PsiElement element) {
		//if (element instanceof IProperty) return LangBundle.message("terms.property");
		return "";
	}

	@Override
	@NotNull
	public String getDescriptiveName(@NotNull PsiElement element) {
		return ((PsiNamedElement)element).getName();
	}

	@Override
	@NotNull
	public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
		return getDescriptiveName(element);
	}

	@Override
	public WordsScanner getWordsScanner() {
		return new PhpWordsScanner();
	}
}
