package com.jantvrdik.intellij.latte.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LatteElementFinderUtil {

	static int MAX_ITERATION = 20;

	@Nullable
	public static PsiElement findPrevWithSkippedWhitespaces(@Nullable PsiElement psiElement) {
		if (psiElement == null) {
			return null;
		}
		return findPrevWithSkippedWhitespaces(psiElement, 0);
	}

	@Nullable
	public static PsiElement findPrevType(@Nullable PsiElement psiElement, @NotNull IElementType elementType, int maxCounter) {
		if (psiElement == null) {
			return null;
		}
		return findPrevType(psiElement, elementType, maxCounter, 0);
	}

	@Nullable
	public static PsiElement findMacroNameElement(@NotNull PsiElement psiElement) {
		PsiElement prev = findPrevType(psiElement, LatteTypes.T_MACRO_NAME, 2);
		if (prev != null && prev.getNode().getElementType() == LatteTypes.T_MACRO_NAME) {
			return prev;
		}

		if (psiElement.getParent() == null || psiElement.getParent().getPrevSibling() == null) {
			return null;
		}

		PsiElement element = findPrevWithSkippedWhitespaces(psiElement.getParent());
		if (element == null) {
			return null;
		}

		if (element.getNode().getElementType() == LatteTypes.T_MACRO_NAME) {
			return psiElement.getParent().getPrevSibling();
		}

		PsiElement parent = findPrevWithSkippedWhitespaces(element);
		if (parent != null && parent.getNode().getElementType() == LatteTypes.T_MACRO_NAME) {
			return parent;
		}
		return null;
	}

	@Nullable
	private static PsiElement findPrevType(@NotNull PsiElement psiElement, @NotNull IElementType elementType, int maxCounter, int counter) {
		counter++;
		if (counter > maxCounter) {
			return null;
		}

		PsiElement out = psiElement.getPrevSibling();
		if (out == null) {
			return null;
		}

		if (out.getNode().getElementType() == elementType) {
			return out;
		}
		return findPrevType(out, elementType, maxCounter, counter);
	}

	@Nullable
	private static PsiElement findPrevWithSkippedWhitespaces(@NotNull PsiElement psiElement, int counter) {
		counter++;
		if (counter > MAX_ITERATION) {
			return null;
		}

		PsiElement out = psiElement.getPrevSibling();
		if (out instanceof PsiWhiteSpace) {
			return findPrevWithSkippedWhitespaces(out, counter);
		}
		return out;
	}
}