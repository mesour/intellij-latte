package com.jantvrdik.intellij.latte.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jantvrdik.intellij.latte.LatteLanguage;
import com.jantvrdik.intellij.latte.config.LatteConfiguration;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;
import com.jantvrdik.intellij.latte.utils.*;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides basic code completion for names of both classic and attribute macros.
 */
public class LatteCompletionContributor extends CompletionContributor {

	/** cached lookup elements for standard classic macros */
	private List<LookupElement> classicMacrosCompletions;

	/** cached lookup elements for standard attribute macros */
	private List<LookupElement> attrMacrosCompletions;

	/** insert handler for attribute macros */
	private InsertHandler<LookupElement> attrMacroInsertHandler = new AttrMacroInsertHandler<LookupElement>();

	public LatteCompletionContributor() {
		initStandardMacrosCompletions();

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(LatteTypes.T_MACRO_NAME).withLanguage(LatteLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
				Project project = parameters.getOriginalFile().getProject();
				Map<String, LatteMacro> customMacros = LatteConfiguration.INSTANCE.getCustomMacros(project);
				result.addAllElements(classicMacrosCompletions);
				result.addAllElements(getClassicMacroCompletions(customMacros));
			}
		});

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(LatteTypes.T_HTML_TAG_NATTR_NAME).withLanguage(LatteLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
				Project project = parameters.getOriginalFile().getProject();
				Map<String, LatteMacro> customMacros = LatteConfiguration.INSTANCE.getCustomMacros(project);
				result.addAllElements(attrMacrosCompletions);
				result.addAllElements(getAttrMacroCompletions(customMacros));
			}
		});

		extend(
				CompletionType.BASIC,
				PlatformPatterns.psiElement(LatteTypes.T_PHP_METHOD).withLanguage(LatteLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
				result.addAllElements(getMethodCompletions(parameters.getPosition()));
			}
		});

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(LatteTypes.T_MACRO_ARGS_VAR).withLanguage(LatteLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
				PsiElement prev = LatteElementFinderUtil.findPrevWithSkippedWhitespaces(parameters.getPosition());
				List<LookupElement> elements;
				if (prev != null && prev.getNode().getElementType() == LatteTypes.T_PHP_DOUBLE_COLON) {
					elements = getMethodCompletions(parameters.getPosition());
				} else {
					elements = getArgsVarCompletions(parameters.getPosition(), parameters.getOriginalFile().getVirtualFile());
				}
				result.addAllElements(elements);
			}
		});

		extend(
				CompletionType.BASIC,
				PlatformPatterns.psiElement()
					.andOr(
							PlatformPatterns.or(
								PlatformPatterns.psiElement().withElementType(LatteTypes.T_MACRO_ARGS_VAR_TYPE),
								PlatformPatterns.psiElement().withElementType(LatteTypes.T_MACRO_ARGS)
							)
						),
				new ClassCompletionProvider()
		);
	}

	/**
	 * Prepares lists of lookup elements for both classic and attribute standard macros.
	 */
	private void initStandardMacrosCompletions() {
		Map<String, LatteMacro> macros = LatteConfiguration.INSTANCE.getStandardMacros();
		classicMacrosCompletions = getClassicMacroCompletions(macros);
		attrMacrosCompletions = getAttrMacroCompletions(macros);
	}

	private List<LookupElement> getArgsVarCompletions(@NotNull PsiElement psiElement, @NotNull VirtualFile virtualFile) {
		List<LookupElement> lookupElements = new ArrayList<LookupElement>();
		for (PsiPositionedElement element : LatteUtil.findVariablesInFileBeforeElement(psiElement, virtualFile)) {
			if (!(element.getElement() instanceof LatteVariableElement)) {
				continue;
			}
			lookupElements.add(LookupElementBuilder.create(((LatteVariableElement) element.getElement()).getVariableName()));
		}
		return lookupElements;
	}

	private List<LookupElement> getMethodCompletions(@NotNull PsiElement psiElement) {
		boolean isStatic = LattePhpUtil.isStatic(psiElement);

		List<LookupElement> lookupElements = new ArrayList<LookupElement>();
		LattePhpVariableType variableType = LatteElementFinderUtil.findMethodReturnType(psiElement);
		if (variableType == null || !variableType.isClassOrInterfaceType()) {
			return lookupElements;
		}

		Collection<PhpClass> phpClasses = variableType.getPhpClasses(psiElement.getProject());
		if (phpClasses == null) {
			return lookupElements;
		}

		for (PhpClass phpClass : phpClasses) {
			for (Method method : phpClass.getMethods()) {
				PhpModifier modifier = method.getModifier();
				if (modifier.isPublic() && canShowCompletionElement(isStatic, modifier)) {
					lookupElements.add(LookupElementBuilder.createWithSmartPointer(method.getName() + "(", method));
				}
			}

			for (Field field : phpClass.getFields()) {
				PhpModifier modifier = field.getModifier();
				if (modifier.isPublic() && (field.isConstant() || canShowCompletionElement(isStatic, modifier))) {
					if (field.isConstant()) {
						lookupElements.add(LookupElementBuilder.create(field.getName()));
					} else {
						lookupElements.add(LookupElementBuilder.create("$" + field.getName()));
					}
				}
			}

			if (isStatic) {
				for (String nativeConstant : LattePhpUtil.getNativeClassConstants()) {
					lookupElements.add(LookupElementBuilder.create(nativeConstant));
				}
			}
		}
		return lookupElements;
	}

	private boolean canShowCompletionElement(boolean isStatic, @NotNull PhpModifier modifier) {
		return (isStatic && modifier.isStatic()) || (!isStatic && !modifier.isStatic());
	}

	/**
	 * Builds list of lookup elements for code completion of classic macros.
	 */
	private List<LookupElement> getClassicMacroCompletions(Map<String, LatteMacro> macros) {
		List<LookupElement> lookupElements = new ArrayList<LookupElement>(macros.size());
		for (LatteMacro macro : macros.values()) {
			if (macro.type != LatteMacro.Type.ATTR_ONLY) {
				lookupElements.add(LookupElementBuilder.create(macro.name));
			}
		}
		return lookupElements;
	}

	/**
	 * Builds list of lookup elements for code completion of attribute macros.
	 */
	private List<LookupElement> getAttrMacroCompletions(Map<String, LatteMacro> macros) {
		List<LookupElement> lookupElements = new ArrayList<LookupElement>(macros.size());
		for (LatteMacro macro : macros.values()) {
			if (macro.type != LatteMacro.Type.UNPAIRED) {
				lookupElements.add(LookupElementBuilder.create("n:" + macro.name).withInsertHandler(attrMacroInsertHandler));
				if (macro.type == LatteMacro.Type.PAIR || macro.type == LatteMacro.Type.AUTO_EMPTY) {
					lookupElements.add(LookupElementBuilder.create("n:tag-" + macro.name).withInsertHandler(attrMacroInsertHandler));
					lookupElements.add(LookupElementBuilder.create("n:inner-" + macro.name).withInsertHandler(attrMacroInsertHandler));
				}
			}
		}
		return lookupElements;
	}

	/**
	 * Inserts ="" after attribute macro and moves caret inside those quotes.
	 */
	private class AttrMacroInsertHandler<T extends LookupElement> implements InsertHandler<T> {
		@Override
		public void handleInsert(InsertionContext context, LookupElement item) {
			Editor editor = context.getEditor();
			Document document = editor.getDocument();
			CaretModel caretModel = editor.getCaretModel();
			int offset = caretModel.getOffset();

			document.insertString(offset, "=\"\"");
			caretModel.moveToOffset(offset + 2);
		}
	}
}
