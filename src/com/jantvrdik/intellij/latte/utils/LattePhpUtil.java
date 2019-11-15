package com.jantvrdik.intellij.latte.utils;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class LattePhpUtil {

	private static String[] nativeClassConstants = new String[]{"class"};

	public static String[] getNativeClassConstants() {
		return nativeClassConstants;
	}

	public static boolean isNativeClassConstant(String constantName) {
		return Arrays.asList(nativeClassConstants).contains(constantName);
	}

	public static boolean isStatic(PsiElement element) {
		if (isPrevDoubleColon(element) || (element.getParent() != null && isPrevDoubleColon(element.getParent()))) {
			return true;
		}
		return false;
	}

	public static boolean isAfterObjectOperator(PsiElement element) {
		if (isPrevObjectOperator(element) || (element.getParent() != null && isPrevObjectOperator(element.getParent()))) {
			return true;
		}
		return false;
	}

	public static boolean isGlobal(PsiElement element) {
		if (isStatic(element)) {
			return false;
		} else if (isPrevObjectOperator(element) || (element.getParent() != null && isPrevObjectOperator(element.getParent()))) {
			return false;
		}
		return true;
	}

	private static boolean isPrevDoubleColon(@NotNull PsiElement element) {
		PsiElement prev = LatteElementFinderUtil.findPrevType(element, LatteTypes.T_PHP_DOUBLE_COLON, 2);
		return prev != null && prev.getNode().getElementType() == LatteTypes.T_PHP_DOUBLE_COLON;
	}

	private static boolean isPrevObjectOperator(@NotNull PsiElement element) {
		PsiElement prev = LatteElementFinderUtil.findPrevType(element, LatteTypes.T_PHP_OBJECT_OPERATOR, 2);
		return prev != null && prev.getNode().getElementType() == LatteTypes.T_PHP_OBJECT_OPERATOR;
	}

	public static Collection<PhpNamedElement> getAllClassNamesAndInterfaces(Project project, Collection<String> classNames) {
		Collection<PhpNamedElement> variants = new THashSet<PhpNamedElement>();
		PhpIndex phpIndex = getPhpIndex(project);

		for (String name : classNames) {
			variants.addAll(filterClasses(phpIndex.getClassesByName(name), null));
			variants.addAll(filterClasses(phpIndex.getInterfacesByName(name), null));
		}
		return variants;
	}

	public static Collection<PhpClass> getClassesByFQN(PsiElement element) {
		return getClassesByFQN(element.getProject(), element.getText());
	}

	public static Collection<PhpClass> getClassesByFQN(Project project, String className) {
		return getPhpIndex(project).getAnyByFQN(className);
	}


	public static Collection<String> getAllExistingClassNames(Project project, PrefixMatcher prefixMatcher) {
		return getPhpIndex(project).getAllClassNames(prefixMatcher);
	}

	private static PhpIndex getPhpIndex(Project project) {
		return PhpIndex.getInstance(project);
	}

	private static Collection<PhpClass> filterClasses(Collection<PhpClass> classes, String namespace) {
		if (namespace == null) {
			return classes;
		}
		namespace = "\\" + namespace + "\\";
		Collection<PhpClass> result = new ArrayList<PhpClass>();
		for (PhpClass cls : classes) {
			String classNs = cls.getNamespaceName();
			if (classNs.equals(namespace) || classNs.startsWith(namespace)) {
				result.add(cls);
			}
		}
		return result;
	}

}