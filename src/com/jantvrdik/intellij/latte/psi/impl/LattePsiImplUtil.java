package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.jantvrdik.intellij.latte.psi.LatteMacroContent;
import com.jantvrdik.intellij.latte.psi.LatteMacroTag;
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
	@NotNull
	public static String getAnnotationMacroName(LatteMacroTag element) {
		return "@var";
	}

}
