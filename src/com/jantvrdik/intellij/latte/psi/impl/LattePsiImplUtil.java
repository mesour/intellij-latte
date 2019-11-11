package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.psi.factories.LatteVariableFactory;
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

	public static String getName(PsiNameIdentifierOwner element) {
		return element.getFirstChild().getText();
	}

	public static PsiElement setName(PsiNameIdentifierOwner element, String newName) {
		ASTNode keyNode = element.getNode();
		if (keyNode != null) {

			PsiNameIdentifierOwner property = LatteVariableFactory.createVariable(element.getProject(), newName);
			ASTNode newKeyNode = property.getFirstChild().getNode();
			element.getNode().replaceChild(keyNode, newKeyNode);
		}
		return element;
	}

	public static PsiElement getNameIdentifier(PsiNameIdentifierOwner element) {
		ASTNode keyNode = element.getNode().findChildByType(T_MACRO_ARGS_VAR);
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	public static String getVariableName(LatteVariableElement element) {
		return element.getFirstChild().getText();
	}

}
