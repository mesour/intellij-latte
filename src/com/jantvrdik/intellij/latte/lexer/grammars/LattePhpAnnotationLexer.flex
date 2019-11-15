package com.jantvrdik.intellij.latte.lexer;

import com.intellij.psi.tree.IElementType;
import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

%%

%class LattePhpAnnotationLexer
%extends LatteBaseFlexLexer
%function advance
%type IElementType
%unicode
%ignorecase

WHITE_SPACE=[ \t\r\n]+
VAR_STRING=[a-zA-Z_][a-zA-Z0-9_]*

%%

<YYINITIAL> {

    "{" {
        return T_ANNOTATION_OPEN_TAG_OPEN;
    }

    "}" {
        return T_ANNOTATION_OPEN_TAG_CLOSE;
    }

    "=" {
        return T_ANNOTATION_OPERATOR;
    }

    "**" {
        return T_ANNOTATION_TAG_OPEN;
    }

    "*" {
        return T_ANNOTATION_TAG_CLOSE;
    }

	"$" {VAR_STRING} {
        return T_MACRO_ARGS_VAR;
    }

    "\\" [a-zA-Z_][a-zA-Z0-9_\\]* {
        return T_MACRO_ARGS_VAR_TYPE;
    }

    {WHITE_SPACE} {
        return T_WHITESPACE;
    }

	[^] {
		return T_MACRO_CONTENT;
	}
}
