package com.jantvrdik.intellij.latte.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LatteUtil {
	public static List<LatteVariableElement> findVariables(Project project, String key) {
		List<LatteVariableElement> result = null;
		Collection<VirtualFile> virtualFiles =
				FileTypeIndex.getFiles(LatteFileType.INSTANCE, GlobalSearchScope.allScope(project));
		for (VirtualFile virtualFile : virtualFiles) {
			LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(virtualFile);
			if (simpleFile != null) {
				List<LatteVariableElement> properties = new ArrayList<LatteVariableElement>();
				for (PsiElement element : simpleFile.getChildren()) {
					findElementsByType(properties, element);
				}

				if (properties != null) {
					for (LatteVariableElement variable : properties) {
						String varName = variable.getVariableName();
						if (key.equals(variable.getVariableName()) && variable.isDefinition()) {
							if (result == null) {
								result = new ArrayList<LatteVariableElement>();
							}
							result.add(variable);
						}
					}
				}
			}
		}
		return result != null ? result : Collections.<LatteVariableElement>emptyList();
	}

	public static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file) {
		return findVariablesInFile(project, file, null);
	}

	public static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key) {
		List<LatteVariableElement> result = null;
		LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(file);
		if (simpleFile != null) {
			List<LatteVariableElement> properties = new ArrayList<LatteVariableElement>();
			for (PsiElement element : simpleFile.getChildren()) {
				findElementsByType(properties, element);
			}

			if (properties != null) {
				for (LatteVariableElement variable : properties) {
					String varName = variable.getVariableName();
					if ((key == null || key.equals(variable.getVariableName())) && variable.isDefinition()) {
						if (result == null) {
							result = new ArrayList<LatteVariableElement>();
						}
						result.add(variable);
					}
				}
			}
		}
		return result != null ? result : Collections.<LatteVariableElement>emptyList();
	}

	@NotNull
	private static List<PsiElement> collectPsiElementsRecursive(@NotNull PsiElement psiElement) {
		final List<PsiElement> elements = new ArrayList<PsiElement>();
		elements.add(psiElement.getContainingFile());

		psiElement.acceptChildren(new PsiRecursiveElementVisitor() {
			@Override
			public void visitElement(PsiElement element) {
				elements.add(element);
				super.visitElement(element);
			}
		});
		return elements;
	}

	public static void findElementsByType(List<LatteVariableElement> properties, PsiElement psiElement) {
		for (PsiElement element : collectPsiElementsRecursive(psiElement)) {
			if (element instanceof LatteVariableElement) {
				properties.add((LatteVariableElement) element);
			}
		}
	}

	public static List<LatteVariableElement> findVariables(Project project) {
		List<LatteVariableElement> result = new ArrayList<LatteVariableElement>();
		Collection<VirtualFile> virtualFiles =
				FileTypeIndex.getFiles(LatteFileType.INSTANCE, GlobalSearchScope.allScope(project));
		for (VirtualFile virtualFile : virtualFiles) {
			LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(virtualFile);
			if (simpleFile != null) {
				LatteVariableElement[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, LatteVariableElement.class);
				if (properties != null) {
					Collections.addAll(result, properties);
				}
			}
		}
		return result;
	}
}