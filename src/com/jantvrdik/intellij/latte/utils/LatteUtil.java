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
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
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

	public static Collection<PhpClass> findPhpClasses(PsiElement psiElement) {
		String variableType = findVariableType(psiElement);
		if (variableType == null) {
			return null;
		}
		return LattePhpUtil.getClassesByFQN(psiElement.getProject(), variableType);
	}

	@Nullable
	public static String findVariableType(PsiElement psiElement) {
		PsiElement prev = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (prev != null) {
			return prev.getText();
		}

		PsiElement parent = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (parent != null) {
			return parent.getText();
		}
		return null;
	}

	public static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file) {
		return findVariablesInFile(project, file, null, false, false);
	}

	public static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, boolean withDefinitions) {
		return findVariablesInFile(project, file, null, withDefinitions, false);
	}

	public static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key) {
		return findVariablesInFile(project, file, key, false, false);
	}

	public static List<LatteVariableElement> findPropertiesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key) {
		return findVariablesInFile(project, file, key, false, true);
	}

	private static List<LatteVariableElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key, boolean withDefinitions, boolean onlyProperties) {
		List<LatteVariableElement> result = null;
		LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(file);
		if (simpleFile != null) {
			List<LatteVariableElement> properties = new ArrayList<LatteVariableElement>();
			for (PsiElement element : simpleFile.getChildren()) {
				findElementsByType(properties, element);
			}

			for (LatteVariableElement variable : properties) {
				String varName = variable.getVariableName();
				if ((key == null || key.equals(variable.getVariableName())) && ((onlyProperties && variable.isProperty()) || (withDefinitions || variable.isDefinition()))) {
					if (result == null) {
						result = new ArrayList<LatteVariableElement>();
					}
					result.add(variable);
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