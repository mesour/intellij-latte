package com.jantvrdik.intellij.latte.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import com.jantvrdik.intellij.latte.utils.PsiPositionedElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import org.jetbrains.annotations.NotNull;
import com.jantvrdik.intellij.latte.config.LatteConfiguration;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.intentions.AddCustomAttrOnlyMacro;
import com.jantvrdik.intellij.latte.intentions.AddCustomPairMacro;
import com.jantvrdik.intellij.latte.intentions.AddCustomUnpairedMacro;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;
import java.util.List;

/**
 * Annotator is mostly used to check semantic rules which can not be easily checked during parsing.
 */
public class LatteAnnotator implements Annotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof LatteMacroClassic) {
			checkClassicMacro(holder, (LatteMacroClassic) element);

		} else if (element instanceof LatteVariableElement) {
			checkVariableElement(holder, (LatteVariableElement) element);

		} else if (element instanceof LatteNetteAttr) {
			checkNetteAttr(holder, (LatteNetteAttr) element);

		} else if (element instanceof LattePhpMethod) {
			checkPhpMethod(holder, (LattePhpMethod) element);

		} else if (element instanceof LatteVariableTypeElement) {
			checkVariableTypeElement(holder, (LatteVariableTypeElement) element);
		}
	}

	private static void checkClassicMacro(@NotNull AnnotationHolder holder, @NotNull LatteMacroClassic element) {
		LatteMacroTag openTag = element.getOpenTag();
		LatteMacroTag closeTag = element.getCloseTag();

		String openTagName = openTag.getMacroName();
		LatteMacro macro = LatteConfiguration.INSTANCE.getMacro(element.getProject(), openTagName);

		PsiElement child = element.getFirstChild().getFirstChild().getNextSibling().getFirstChild();

		if (child instanceof LatteVariableElement) {
			checkVariableElement(holder, (LatteVariableElement) child);
			return;
		}

		if (macro == null || macro.type == LatteMacro.Type.ATTR_ONLY) {
			Annotation annotation = holder.createErrorAnnotation(openTag, "Unknown macro {" + openTagName + "}");
			annotation.registerFix(new AddCustomPairMacro(openTagName));
			annotation.registerFix(new AddCustomUnpairedMacro(openTagName));
		}
		String closeTagName = closeTag != null ? closeTag.getMacroName() : null;
		if (closeTagName != null && !closeTagName.isEmpty() && !closeTagName.equals(openTagName)) {
			holder.createErrorAnnotation(closeTag, "Unexpected {/" + closeTagName + "}, expected {/" + openTagName + "}");
		}
	}

	private static void checkVariableElement(@NotNull AnnotationHolder holder, @NotNull LatteVariableElement element) {
		String variableName = element.getVariableName();

		if (element.isProperty()) {
			Collection<PhpClass> phpClasses = LatteUtil.findPhpClasses(element);
			if (phpClasses != null && phpClasses.size() > 0) {
				boolean isFound = false;
				for (PhpClass phpClass : phpClasses) {
					for (Field field : phpClass.getFields()) {
						if (!field.isConstant() && ("$" + field.getName()).equals(variableName)) {
							checkVisibility(holder, field.getModifier(), element, variableName, "property");
							checkStatic(holder, field.getModifier(), element, variableName, "property");
							isFound = true;
						}
					}
				}

				if (!isFound) {
					holder.createErrorAnnotation(element, resolveUndefinedError("property", variableName));
				}
			} else {
				holder.createWarningAnnotation(element, resolveClassNotFound("property", variableName));
			}

		} else {
			List<PsiPositionedElement> found = LatteUtil.findVariablesInFile(
					element.getProject(),
					element.getContainingFile().getVirtualFile(),
					variableName
			);
			if (element.isDefinition()) {
				if (found.size() > 1) {
					holder.createWarningAnnotation(element, "Multiple definitions for variable '" + variableName + "'");
				}

			} else if (found.size() == 0) {
				holder.createErrorAnnotation(element, resolveUndefinedError("variable", variableName));
			}
		}
	}

	private static void checkVariableTypeElement(@NotNull AnnotationHolder holder, @NotNull LatteVariableTypeElement element) {
		if (LattePhpUtil.getClassesByFQN(element.getProject(), element.getVariableType()).size() == 0) {
			holder.createErrorAnnotation(
					element.getFirstChild() != null ? element.getFirstChild() : element,
					resolveUndefinedError("class", element.getVariableType())
			);
		}
	}

	private static void checkPhpMethod(@NotNull AnnotationHolder holder, @NotNull LattePhpMethod element) {
		boolean isMethod = element.getNextSibling() != null && element.getNextSibling().getText().equals("(");
		String methodName = element.getMethodName();

		Collection<PhpClass> phpClasses = LatteUtil.findPhpClasses(element);
		if (phpClasses != null && phpClasses.size() > 0) {
			boolean isFound = false;
			if (isMethod) {
				for (PhpClass phpClass : phpClasses) {
					for (Method method : phpClass.getMethods()) {
						if (method.getName().equals(methodName)) {
							checkVisibility(holder, method.getModifier(), element, methodName, "method");
							checkStatic(holder, method.getModifier(), element, methodName, "method");
							isFound = true;
						}
					}
				}

			} else {
				if (LattePhpUtil.isNativeClassConstant(methodName)) {
					isFound = true;
				} else {
					for (PhpClass phpClass : phpClasses) {
						for (Field field : phpClass.getFields()) {
							if (field.isConstant() && field.getName().equals(methodName)) {
								checkVisibility(holder, field.getModifier(), element, methodName, "constant");
								boolean isStatic = LattePhpUtil.isStatic(element);
								if (isStatic && !field.getModifier().isStatic()) {
									holder.createWarningAnnotation(element, "Constant " + methodName + " is not static but called statically");
								} else if (!isStatic) {
									holder.createWarningAnnotation(element, "Constant must be called statically");
								}
								isFound = true;
							}
						}
					}
				}
			}

			if (!isFound) {
				boolean isGlobal = LattePhpUtil.isGlobal(element);
				holder.createErrorAnnotation(element, (isMethod ? "Method" : "Constant") + " '" + methodName + "' not found");
			}

		} else {
			holder.createWarningAnnotation(element, resolveClassNotFound(isMethod ? "method" : "constant", methodName));
		}
	}

	private static void checkVisibility(
			@NotNull AnnotationHolder holder,
			@NotNull PhpModifier modifier,
			@NotNull PsiElement element,
			@NotNull String methodName,
			@NotNull String type
	) {
		if (modifier.isPrivate()) {
			holder.createWarningAnnotation(element, resolveVisibilityError("private", type, methodName));
		} else if (modifier.isProtected()) {
			holder.createWarningAnnotation(element, resolveVisibilityError("protected", type, methodName));
		}
	}

	private static void checkStatic(
			@NotNull AnnotationHolder holder,
			@NotNull PhpModifier modifier,
			@NotNull PsiElement element,
			@NotNull String methodName,
			@NotNull String type
	) {
		boolean isStatic = LattePhpUtil.isStatic(element);
		if (isStatic && !modifier.isStatic()) {
			holder.createWarningAnnotation(element, LatteUtil.capitalizeFirstLetter(type) +  " " + methodName + " is not static but called statically");
		} else if (!isStatic && modifier.isStatic()) {
			holder.createWarningAnnotation(element, type +  " " + methodName + " is static but called non statically");
		}
	}

	private static void checkNetteAttr(@NotNull AnnotationHolder holder, @NotNull LatteNetteAttr element) {
		PsiElement attrName = element.getAttrName();
		String macroName = attrName.getText();
		boolean prefixed = false;

		if (macroName.startsWith("n:inner-")) {
			prefixed = true;
			macroName = macroName.substring(8);
		} else if (macroName.startsWith("n:tag-")) {
			prefixed = true;
			macroName = macroName.substring(6);
		} else {
			macroName = macroName.substring(2);
		}

		LatteMacro macro = LatteConfiguration.INSTANCE.getMacro(element.getProject(), macroName);
		if (macro == null || macro.type == LatteMacro.Type.UNPAIRED) {
			Annotation annotation = holder.createErrorAnnotation(attrName, "Unknown attribute macro " + attrName.getText());
			annotation.registerFix(new AddCustomPairMacro(macroName));
			if (!prefixed) annotation.registerFix(new AddCustomAttrOnlyMacro(macroName));

		} else if (prefixed && macro.type != LatteMacro.Type.PAIR && macro.type != LatteMacro.Type.AUTO_EMPTY) {
			holder.createErrorAnnotation(attrName, "Attribute macro n:" + macroName + " can not be used with prefix.");
		}
	}

	private static String resolveVisibilityError(@NotNull String type, @NotNull String name, @NotNull String variableName) {
		return "Used " + type + " " + name + " '" + variableName + "'";
	}

	private static String resolveUndefinedError(@NotNull String type, @NotNull String name) {
		return "Undefined " + type + " '" + name + "'";
	}

	private static String resolveClassNotFound(@NotNull String type, @NotNull String name) {
		return "Class not found for " + type + " '" + name + "'";
	}
}
