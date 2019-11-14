package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.psi.factories.LattePhpMethodFactory;
import com.jantvrdik.intellij.latte.psi.factories.LatteVariableFactory;
import com.jantvrdik.intellij.latte.psi.factories.LatteVariableTypeFactory;
import org.jetbrains.annotations.NotNull;

import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

public class LattePsiImplUtil {
	@NotNull
	public static String getMacroName(LatteMacroTag element) {
		ASTNode elementNode = element.getNode();
		ASTNode nameNode = elementNode.findChildByType(T_MACRO_NAME);
		if (nameNode != null) {
			return nameNode.getText();
		}

		nameNode = elementNode.findChildByType(T_MACRO_SHORTNAME);
		if (nameNode != null) {
			return nameNode.getText();
		}
		LatteMacroContent content = element.getMacroContent();
		if (content == null) {
			return "";
		}

		ASTNode argsNode = content.getNode().findChildByType(
			TokenSet.create(T_MACRO_ARGS, T_MACRO_ARGS_VAR, T_MACRO_ARGS_STRING, T_MACRO_ARGS_NUMBER)
		);
		return (argsNode != null ? "=" : "");
	}

	public static String getName(LatteVariableElement element) {
		return element.getFirstChild().getText();
	}

	public static PsiElement setName(LatteVariableElement element, String newName) {
		ASTNode keyNode = element.getNode();
		if (keyNode != null) {

			PsiNameIdentifierOwner property = LatteVariableFactory.createVariable(element.getProject(), newName);
			ASTNode newKeyNode = property.getFirstChild().getNode();
			element.getNode().replaceChild(keyNode, newKeyNode);
		}
		return element;
	}

	public static PsiElement getNameIdentifier(LatteVariableElement element) {
		ASTNode keyNode = element.getNode().findChildByType(T_MACRO_ARGS_VAR);
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	public static String getName(LatteVariableTypeElement element) {
		return element.getFirstChild().getText();
	}

	public static PsiElement setName(LatteVariableTypeElement element, String newName) {
		ASTNode keyNode = element.getNode();
		if (keyNode != null) {

			LatteVariableTypeElement property = LatteVariableTypeFactory.createVariableType(element.getProject(), newName);
			ASTNode newKeyNode = property.getFirstChild().getNode();
			element.getNode().replaceChild(keyNode, newKeyNode);
		}
		return element;
	}

	public static PsiElement getNameIdentifier(LatteVariableTypeElement element) {
		ASTNode keyNode = element.getNode().findChildByType(T_MACRO_ARGS_VAR_TYPE);
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	public static String getName(LattePhpMethod element) {
		return element.getFirstChild().getText();
	}

	public static PsiElement setName(LattePhpMethod element, String newName) {
		ASTNode keyNode = element.getNode();
		if (keyNode != null) {

			LattePhpMethod property = LattePhpMethodFactory.createPhpMethod(element.getProject(), newName);
			ASTNode newKeyNode = property.getFirstChild().getNode();
			element.getNode().replaceChild(keyNode, newKeyNode);
		}
		return element;
	}

	public static PsiElement getNameIdentifier(LattePhpMethod element) {
		ASTNode keyNode = element.getNode().findChildByType(T_PHP_METHOD);
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	public static String getVariableName(LatteVariableElement element) {
		return element.getFirstChild().getText();
	}

	public static String getMethodName(LattePhpMethod element) {
		return element.getText();
	}

	public static String getVariableType(LatteVariableTypeElement element) {
		return element.getFirstChild().getText();
	}

}
