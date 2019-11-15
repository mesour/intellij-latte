package com.jantvrdik.intellij.latte.lexer;

import com.intellij.lexer.LayeredLexer;
import com.jantvrdik.intellij.latte.psi.LatteTypes;

/**
 * Main Latte lexer which combines "top lexer" and "macro lexer".
 *
 * LatteMacroLexerAdapter is used to further process token T_MACRO_CLASSIC.
 */
public class LatteLexer extends LayeredLexer {
	public LatteLexer() {
		super(new LatteTopLexerAdapter());
		LayeredLexer macroLexer = new LayeredLexer(new LatteMacroLexerAdapter());
		macroLexer.registerLayer(createContentAdapter(), LatteTypes.T_MACRO_CONTENT);
		macroLexer.registerLayer(new LattePhpLexerAdapter(), LatteTypes.T_PHP_CONTENT);

		registerLayer(macroLexer, LatteTypes.T_MACRO_CLASSIC);
		registerLayer(createContentAdapter(), LatteTypes.T_MACRO_CONTENT);
		registerLayer(new LattePhpLexerAdapter(), LatteTypes.T_PHP_CONTENT);
		registerLayer(new LattePhpAnnotationLexerAdapter(), LatteTypes.T_MACRO_ANNOTATION);
	}

	private LayeredLexer createContentAdapter() {
		LayeredLexer macroLexer = new LayeredLexer(new LatteMacroContentLexerAdapter());
		macroLexer.registerLayer(new LattePhpLexerAdapter(), LatteTypes.T_PHP_CONTENT);
		return macroLexer;
	}
}
