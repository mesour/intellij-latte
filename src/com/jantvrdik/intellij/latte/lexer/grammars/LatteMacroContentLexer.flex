package com.jantvrdik.intellij.latte.lexer;

import com.intellij.psi.tree.IElementType;
import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

%%

%class LatteMacroContentLexer
%extends LatteBaseFlexLexer
%function advance
%type IElementType
%unicode
%ignorecase


STRING = {STRING_SQ} | {STRING_DQ}
STRING_SQ = "'" ("\\" [^] | [^'\\])* "'"
STRING_DQ = "\"" ("\\" [^] | [^\"\\])* "\""
SYMBOL = [_[:letter:]][_0-9[:letter:]]*(-[_0-9[:letter:]]+)* //todo: unicode letters

%%


<YYINITIAL> {

	"$" [a-zA-Z_][a-zA-Z0-9_]* {
		return T_MACRO_ARGS_VAR;
	}

	"\\" [a-zA-Z_][a-zA-Z0-9_\\]* {
		return T_MACRO_ARGS_VAR_TYPE;
	}

	{STRING} {
		return T_MACRO_ARGS_STRING;
	}

	[0-9]+ {
		return T_MACRO_ARGS_NUMBER;
	}

	{SYMBOL} {
		return T_MACRO_ARGS;
	}

	[^] {
		return T_MACRO_ARGS;
	}
}
