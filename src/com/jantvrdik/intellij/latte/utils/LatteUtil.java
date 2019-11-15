package com.jantvrdik.intellij.latte.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LattePhpMethod;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class LatteUtil {
	public static List<LatteVariableElement> findVariables(Project project, String key) {
		List<LatteVariableElement> result = null;
		Collection<VirtualFile> virtualFiles =
				FileTypeIndex.getFiles(LatteFileType.INSTANCE, GlobalSearchScope.allScope(project));
		for (VirtualFile virtualFile : virtualFiles) {
			LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(virtualFile);
			if (simpleFile != null) {
				List<PsiPositionedElement> properties = new ArrayList<PsiPositionedElement>();
				for (PsiElement element : simpleFile.getChildren()) {
					findElementsByType(properties, element);
				}

				for (PsiPositionedElement variable : properties) {
					if (!(variable.getElement() instanceof LatteVariableElement)) {
						continue;
					}

					String varName = ((LatteVariableElement) variable.getElement()).getVariableName();
					if (key.equals(varName) && ((LatteVariableElement) variable.getElement()).isDefinition()) {
						if (result == null) {
							result = new ArrayList<LatteVariableElement>();
						}
						result.add((LatteVariableElement) variable.getElement());
					}
				}
			}
		}
		return result != null ? result : Collections.<LatteVariableElement>emptyList();
	}

	public static Collection<PhpClass> findPhpClasses(PsiElement psiElement) {
		LattePhpVariableType variableType = findVariableType(psiElement);
		if (variableType == null || variableType.getType() == null) {
			return null;
		}
		return LattePhpUtil.getClassesByFQN(psiElement.getProject(), variableType.getType());
	}

	public static String capitalizeFirstLetter(@NotNull String customText){
		int count = customText.length();
		if (count == 0) {
			return customText;
		}
		if (count == 1) {
			return customText.toUpperCase();
		}
		return customText.substring(0, 1).toUpperCase() + customText.substring(1).toLowerCase();
	}

	@Nullable
	public static LattePhpVariableType findVariableType(PsiElement psiElement) {
		PsiElement prev = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (prev != null) {
			return new LattePhpVariableType(prev.getText(), false);
		}

		PsiElement parent = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (parent != null) {
			return new LattePhpVariableType(parent.getText(), false);
		}

		PsiElement method = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.ARGS_PHP_METHOD, 5);
		if (method instanceof LattePhpMethod) {
			return ((LattePhpMethod) method).getReturnType();
		}

		PsiElement parentMethod = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.ARGS_PHP_METHOD, 5);
		if (parentMethod == null) {
			parentMethod = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.T_PHP_METHOD, 5);
		}

		if (parentMethod != null) {
			LattePhpVariableType type = findVariableType(parentMethod);
			if (type == null) {
				return null;
			}
			Collection<PhpClass> phpClasses = type.getPhpClasses(psiElement.getProject());
			if (phpClasses == null) {
				return null;
			}
			PhpClass first = phpClasses.stream().findFirst().isPresent() ? phpClasses.stream().findFirst().get() : null;
			if (first == null) {
				return null;
			}

			for (Method phpMethod : first.getMethods()) {
				String name = phpMethod.getName();
				if (phpMethod.getName().equals(parentMethod instanceof LattePhpMethod ? ((LattePhpMethod) parentMethod).getMethodName() : parentMethod.getText())) {
					return new LattePhpVariableType(phpMethod.getType().toString(), phpMethod.getType().isNullable());
				}
			}
			//todo: search variables
			return null;
		}
		return null;
	}

	public static List<PsiPositionedElement> findVariablesInFileBeforeElement(@NotNull PsiElement element, @NotNull VirtualFile virtualFile) {
		return findVariablesInFileBeforeElement(element, virtualFile, null);
	}

	public static List<PsiPositionedElement> findVariablesInFileBeforeElement(@NotNull PsiElement element, @NotNull VirtualFile virtualFile, @Nullable String key) {
		List<PsiPositionedElement> variables = findVariablesInFile(
				element.getProject(),
				virtualFile,
				key,
				false,
				false
		);

		int offset = element.getNode().getStartOffsetInParent();
		return variables.stream()
			.filter(variableElement -> variableElement.getPosition() <= offset)
			.collect(Collectors.toList());
	}

	public static List<PsiPositionedElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, boolean withDefinitions) {
		return findVariablesInFile(project, file, null, withDefinitions, false);
	}

	public static List<PsiPositionedElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key) {
		return findVariablesInFile(project, file, key, false, false);
	}

	private static List<PsiPositionedElement> findVariablesInFile(@NotNull Project project, @NotNull VirtualFile file, @Nullable String key, boolean withDefinitions, boolean onlyProperties) {
		List<PsiPositionedElement> result = null;
		LatteFile simpleFile = (LatteFile) PsiManager.getInstance(project).findFile(file);
		if (simpleFile != null) {
			List<PsiPositionedElement> properties = new ArrayList<PsiPositionedElement>();
			for (PsiElement element : simpleFile.getChildren()) {
				findElementsByType(properties, element);
			}

			for (PsiPositionedElement variable : properties) {
				if (!(variable.getElement() instanceof LatteVariableElement)) {
					continue;
				}

				String varName = ((LatteVariableElement) variable.getElement()).getVariableName();
				if (
						(key == null || key.equals(varName))
						&& (
								(onlyProperties && ((LatteVariableElement) variable.getElement()).isProperty())
								|| (withDefinitions || ((LatteVariableElement) variable.getElement()).isDefinition())
						)
				) {
					if (result == null) {
						result = new ArrayList<PsiPositionedElement>();
					}
					result.add(variable);
				}
			}
		}
		return result != null ? result : Collections.<PsiPositionedElement>emptyList();
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

	public static void findElementsByType(List<PsiPositionedElement> properties, PsiElement psiElement) {
		for (PsiElement element : collectPsiElementsRecursive(psiElement)) {
			if (element instanceof LatteVariableElement) {
				properties.add(new PsiPositionedElement(psiElement.getNode().getStartOffsetInParent(), element));
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