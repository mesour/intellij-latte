package com.jantvrdik.intellij.latte.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.jantvrdik.intellij.latte.LatteLanguage;
import com.jantvrdik.intellij.latte.config.LatteConfiguration;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.psi.LatteMacroTag;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import org.jetbrains.annotations.NotNull;

public class MacroInsertHandler implements InsertHandler<LookupElement> {

	private static final MacroInsertHandler instance = new MacroInsertHandler();

	public MacroInsertHandler() {
		super();
	}

	public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
		PsiElement element = context.getFile().findElementAt(context.getStartOffset());
		if (element != null && element.getLanguage() == LatteLanguage.INSTANCE) {
			PsiElement parent = element.getParent();

			boolean resolvePairMacro = false;
			boolean lastError = parent.getLastChild().getNode().getElementType() == TokenType.ERROR_ELEMENT;
			String macroName = null;
			LatteMacro macro = null;
			if (lastError && element.getNode().getElementType() == LatteTypes.T_MACRO_NAME) {
				macroName = element.getText();
				macro = LatteConfiguration.INSTANCE.getMacro(element.getProject(), macroName);
				if (macro != null && macro.type == LatteMacro.Type.PAIR) {
					resolvePairMacro = true;
				}

			} else if (parent instanceof LatteMacroTag) {
				macroName = ((LatteMacroTag) parent).getMacroName();
				macro = LatteConfiguration.INSTANCE.getMacro(element.getProject(), macroName);
			}

			if (macroName != null) {
				Editor editor = context.getEditor();
				CaretModel caretModel = editor.getCaretModel();
				String text = editor.getDocument().getText();

				int spaceInserted = 0;
				int offset = caretModel.getOffset();
				if (macro != null && macro.hasParameters && !LatteUtil.isStringAtCaret(editor, " ")) {
					EditorModificationUtil.insertStringAtCaret(editor, " ");
					spaceInserted = 1;
				}

				if (resolvePairMacro) {
					String endTag = "{/" + macroName + "}";

					int lastBraceOffset = text.indexOf("}", offset);
					int endOfLineOffset = text.indexOf("\n", offset);
					int endTagOffset = text.indexOf(endTag, offset);

					if (lastBraceOffset == -1 || lastBraceOffset > endOfLineOffset) {
						caretModel.moveToOffset(endOfLineOffset + spaceInserted);
						EditorModificationUtil.insertStringAtCaret(editor, "}");
						lastBraceOffset = endOfLineOffset;
						endOfLineOffset++;
					}

					if (endTagOffset == -1 || endTagOffset > endOfLineOffset) {
						caretModel.moveToOffset(lastBraceOffset + spaceInserted + 1);
						EditorModificationUtil.insertStringAtCaret(editor, endTag);
					}
				}
				caretModel.moveToOffset(offset + 1);
				PsiDocumentManager.getInstance(context.getProject()).commitDocument(editor.getDocument());
			}
		}
	}

	public static MacroInsertHandler getInstance() {
		return instance;
	}
}