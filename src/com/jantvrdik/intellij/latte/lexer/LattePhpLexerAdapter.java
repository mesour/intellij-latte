package com.jantvrdik.intellij.latte.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.jantvrdik.intellij.latte.psi.LatteTypes;

public class LattePhpLexerAdapter extends MergingLexerAdapter {

	public LattePhpLexerAdapter() {
		super(
				new FlexAdapter(new LattePhpLexer(null)),
				TokenSet.create(LatteTypes.T_PHP_CONTENT)
		);
	}
}
