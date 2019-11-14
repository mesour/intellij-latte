package com.jantvrdik.intellij.latte.lexer;

import com.intellij.psi.tree.IElementType;
import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

%%

%class LattePhpLexer
%extends LatteBaseFlexLexer
%function advance
%type IElementType
%unicode
%ignorecase

STRING = {STRING_SQ} | {STRING_DQ}
STRING_SQ = "'" ("\\" [^] | [^'\\])* "'"
STRING_DQ = "\"" ("\\" [^] | [^\"\\])* "\""
WHITE_SPACE=[ \t\r\n]+
VAR_STRING=[a-zA-Z_][a-zA-Z0-9_]*

%%

<YYINITIAL> {

	"$" {VAR_STRING} {
        return T_MACRO_ARGS_VAR;
    }

    "\\" [a-zA-Z_][a-zA-Z0-9_\\]* {
        return T_MACRO_ARGS_VAR_TYPE;
    }

    [0-9]+ {
        return T_MACRO_ARGS_STRING;
    }

    "::" {
        return T_PHP_DOUBLE_COLON;
    }

    ("<=>" | "<>" | "<=" | ">=" | "<" | ">" | "==" | "===" | "\!=" | "\==") {
        return T_PHP_OPERATOR;
    }

    {STRING} {
        return T_MACRO_ARGS_STRING;
    }

    {VAR_STRING} {
        return T_PHP_METHOD;
    }

    {WHITE_SPACE} {
        return T_WHITESPACE;
    }

    [^] {
        return T_MACRO_ARGS;
    }
}
