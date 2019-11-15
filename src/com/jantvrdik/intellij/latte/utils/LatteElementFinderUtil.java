package com.jantvrdik.intellij.latte.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.jantvrdik.intellij.latte.psi.*;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LatteElementFinderUtil {

	static int MAX_ITERATION = 20;

	static int MAX_LINE_ITERATION = 40;


	@Nullable
	public static LattePhpVariableType findVariableType(PsiElement psiElement) {
		PsiElement parent = psiElement.getParent();
		if (parent instanceof LatteVariableElement && !((LatteVariableElement) parent).isProperty() && !((LatteVariableElement) parent).isDefinition()) {
			List<PsiPositionedElement> definitions = LatteUtil.findVariablesInFile(
					parent.getProject(),
					parent.getContainingFile().getOriginalFile().getVirtualFile(),
					((LatteVariableElement) parent).getVariableName()
			);
			PsiPositionedElement definition = definitions.stream().findFirst().isPresent() ? definitions.stream().findFirst().get() : null;
			if (definition != null) {
				PsiElement varType = findNextType(definition.getElement(), LatteTypes.ARGS_VAR_TYPE, 4);
				if ((varType instanceof LatteVariableTypeElement)) {
					PsiElement lastElement = findLastElement(varType.getFirstChild());
					if (lastElement != null && lastElement.getNode().getElementType() != LatteTypes.T_MACRO_ARGS_VAR_TYPE) {
						return findMethodReturnType(lastElement.getFirstChild());
					} else {
						return new LattePhpVariableType(((LatteVariableTypeElement) varType).getVariableType(), false);
					}
				}
			}
		}

		PsiElement prev = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (prev != null) {
			return new LattePhpVariableType(psiElement.getText(), prev.getText());
		}

		PsiElement parentType = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (parentType != null) {
			return new LattePhpVariableType(psiElement.getText(), parentType.getText());
		}
		return null;
	}

	@Nullable
	public static LattePhpVariableType findMethodReturnType(PsiElement psiElement) {
		PsiElement prevVariable = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.T_MACRO_ARGS_VAR, 3);
		if (prevVariable != null) {
			LattePhpVariableType type = findVariableType(prevVariable);
			if (type == null) {
				return null;
			}

			if (type.isClassOrInterfaceType()) {
				return type;

			} else {
				PhpClass first = type.getFirstPhpClass(psiElement.getProject());
				if (first == null) {
					return null;
				}

				for (Field fields : first.getFields()) {
					String variableName = type.getName() != null ? type.getName() : psiElement.getText();
					if (fields.getName().equals(variableName)) {
						return new LattePhpVariableType(fields.getType().toString(), fields.getType().isNullable());
					}
				}
			}
			return null;
		}

		PsiElement prev = LatteElementFinderUtil.findPrevType(psiElement, LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (prev != null) {
			return new LattePhpVariableType(psiElement.getText(), prev.getText());
		}

		PsiElement parent = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.T_MACRO_ARGS_VAR_TYPE, 3);
		if (parent != null) {
			return new LattePhpVariableType(psiElement.getText(), parent.getText());
		}

		PsiElement parentMethod = LatteElementFinderUtil.findPrevType(psiElement.getParent(), LatteTypes.ARGS_PHP_METHOD, 5);
		if (parentMethod instanceof LattePhpMethod) {
			LattePhpVariableType type = findMethodReturnType(parentMethod.getFirstChild());
			if (type == null) {
				return null;
			}
			PhpClass first = type.getFirstPhpClass(psiElement.getProject());
			if (first == null) {
				return null;
			}

			for (Method phpMethod : first.getMethods()) {
				String methodName = type.getName() != null ? type.getName() : ((LattePhpMethod) parentMethod).getMethodName();
				if (phpMethod.getName().equals(methodName)) {
					return new LattePhpVariableType(phpMethod.getType().toString(), phpMethod.getType().isNullable());
				}
			}
		}
		return null;
	}

	@Nullable
	public static PsiElement findPrevWithSkippedWhitespaces(@Nullable PsiElement psiElement) {
		if (psiElement == null) {
			return null;
		}
		return findPrevWithSkippedWhitespaces(psiElement, 0);
	}

	@Nullable
	public static PsiElement findNextWithSkippedWhitespaces(@Nullable PsiElement psiElement) {
		if (psiElement == null) {
			return null;
		}
		return findNextWithSkippedWhitespaces(psiElement, 0);
	}

	@Nullable
	public static PsiElement findLastElement(@Nullable PsiElement psiElement) {
		if (psiElement == null) {
			return null;
		}
		return findLastElement(psiElement, 0);
	}

	@Nullable
	public static PsiElement findPrevType(@Nullable PsiElement psiElement, @NotNull IElementType elementType, int maxCounter) {
		if (psiElement == null) {
			return null;
		}
		return findPrevType(psiElement, elementType, maxCounter, 0);
	}

	@Nullable
	public static PsiElement findNextType(@Nullable PsiElement psiElement, @NotNull IElementType elementType, int maxCounter) {
		if (psiElement == null) {
			return null;
		}
		return findNextType(psiElement, elementType, maxCounter, 0);
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
	private static PsiElement findLastElement(@NotNull PsiElement psiElement, int counter) {
		counter++;
		if (counter > MAX_LINE_ITERATION) {
			return null;
		}

		if (psiElement.getNextSibling() == null) {
			return psiElement;
		}
		return findLastElement(psiElement.getNextSibling(), counter);
	}

	@Nullable
	private static PsiElement findPrevType(@NotNull PsiElement psiElement, @NotNull IElementType elementType, int maxCounter, int counter) {
		counter++;
		if (counter > maxCounter) {
			return null;
		}

		PsiElement out = findPrevWithSkippedWhitespaces(psiElement);
		if (out == null) {
			return null;
		}

		if (out.getNode().getElementType() == elementType) {
			return out;
		}
		return findPrevType(out, elementType, maxCounter, counter);
	}

	@Nullable
	private static PsiElement findNextType(@NotNull PsiElement psiElement, @NotNull IElementType elementType, int maxCounter, int counter) {
		counter++;
		if (counter > maxCounter) {
			return null;
		}

		PsiElement out = findNextWithSkippedWhitespaces(psiElement);
		if (out == null) {
			return null;
		}

		if (out.getNode().getElementType() == elementType) {
			return out;
		}
		return findNextType(out, elementType, maxCounter, counter);
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

	@Nullable
	private static PsiElement findNextWithSkippedWhitespaces(@NotNull PsiElement psiElement, int counter) {
		counter++;
		if (counter > MAX_ITERATION) {
			return null;
		}

		PsiElement out = psiElement.getNextSibling();
		if (out instanceof PsiWhiteSpace) {
			return findNextWithSkippedWhitespaces(out, counter);
		}
		return out;
	}
}