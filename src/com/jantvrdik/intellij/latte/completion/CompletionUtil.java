package com.jantvrdik.intellij.latte.completion;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jantvrdik.intellij.latte.psi.LatteTypes;

public class CompletionUtil {

	public static boolean isIncompleteKey(PsiElement el) {
		PsiElement parent = el.getParent();
		ASTNode node = el.getNode();
		if (node.getElementType() == LatteTypes.T_MACRO_VAR_TYPE || LatteTypes.T_MACRO_VAR_TYPE == parent.getNode().getElementType()) {
			return true;
		}
/*		//first scalar in file
		if (el.getParent() instanceof NeonScalar && el.getParent().getParent() instanceof NeonFile) {
			return true;
		}
		//error element
		if (el.getParent() instanceof NeonArray
				&& el.getPrevSibling() instanceof PsiErrorElement
				&& ((PsiErrorElement) el.getPrevSibling()).getErrorDescription().equals(NeonParser.EXPECTED_ARRAY_ITEM)) {
			return true;
		}
		//new key after new line
		if (el.getParent() instanceof NeonScalar
				&& (el.getParent().getParent() instanceof NeonKeyValPair | el.getParent().getParent().getNode().getElementType() == NeonElementTypes.ITEM)
				&& el.getParent().getPrevSibling().getNode().getElementType() == NeonTokenTypes.NEON_INDENT) {
			return true;
		}
*/
		return false;
	}

}