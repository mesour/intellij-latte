package com.jantvrdik.intellij.latte.reference.handlers;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jantvrdik.intellij.latte.psi.LattePhpMethod;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GoToClassHandler implements GotoDeclarationHandler {

	@Nullable
	@Override
	public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement element, int offset, Editor editor) {
		if (element == null || element.getParent() == null) {
			return new PsiElement[0];
		}


		if (element.getNode().getElementType() == LatteTypes.T_MACRO_ARGS_VAR_TYPE) {
			Collection<PhpClass> classes = LattePhpUtil.getClassesByFQN(element);
			return classes.toArray(new PsiElement[classes.size()]);

		} else if (element.getNode().getElementType() == LatteTypes.T_MACRO_ARGS_VAR) {
			PsiElement parent = element.getParent();
			Collection<PhpClass> phpClasses = LatteUtil.findPhpClasses(element);
			if (phpClasses == null || phpClasses.size() ==  0 || !(parent instanceof LatteVariableElement) || !((LatteVariableElement) parent).isProperty()) {
				return new PsiElement[0];
			}

			List<Field> fields = new ArrayList<>();
			for (PhpClass phpClass : phpClasses) {
				for (Field field : phpClass.getFields()) {
					if (!field.isConstant() && ("$" + field.getName()).equals(((LatteVariableElement) parent).getVariableName())) {
						fields.add(field);
					}
				}
			}
			return fields.toArray(new PsiElement[fields.size()]);

		} else if (element.getNode().getElementType() == LatteTypes.T_PHP_METHOD) {
			PsiElement parent = element.getParent();
			Collection<PhpClass> phpClasses = LatteUtil.findPhpClasses(element);
			if (phpClasses == null || phpClasses.size() ==  0 || !(parent instanceof LattePhpMethod)) {
				return new PsiElement[0];
			}

			boolean isMethod = parent.getNextSibling() != null && parent.getNextSibling().getText().equals("(");
			String methodName = ((LattePhpMethod) parent).getMethodName();


			if (isMethod) {
				List<Method> methods = new ArrayList<>();
				for (PhpClass phpClass : phpClasses) {
					for (Method method : phpClass.getMethods()) {
						if (method.getName().equals(methodName)) {
							methods.add(method);
						}
					}
				}
				return methods.toArray(new PsiElement[methods.size()]);

			} else {
				List<Field> fields = new ArrayList<>();
				for (PhpClass phpClass : phpClasses) {
					for (Field field : phpClass.getFields()) {
						if (field.isConstant() && field.getName().equals(methodName)) {
							fields.add(field);
						}
					}
				}
				return fields.toArray(new PsiElement[fields.size()]);
			}
		}
		return new PsiElement[0];
	}

	@Nullable
	@Override
	public String getActionText(DataContext context) {
		return null;
	}


}