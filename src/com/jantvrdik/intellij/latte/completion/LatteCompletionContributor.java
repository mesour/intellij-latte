package com.jantvrdik.intellij.latte.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.ui.PsiClassPanel;
import com.jantvrdik.intellij.latte.LatteLanguage;
import com.jantvrdik.intellij.latte.config.LatteConfiguration;
import com.jantvrdik.intellij.latte.config.LatteMacro;
import com.jantvrdik.intellij.latte.psi.LatteTypes;
import com.jantvrdik.intellij.latte.util.PhpStaticFactoryTypeProvider;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.completion.*;
import com.jetbrains.php.completion.PhpCompletionSorting;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpUseImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

	/** cached lookup elements for standard attribute macros */
	private List<LookupElement> testMacrosCompletions;

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

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(LatteTypes.T_MACRO_VAR_TYPE).withLanguage(LatteLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
				Project project = parameters.getOriginalFile().getProject();

				Map<String, LatteMacro> customMacros = LatteConfiguration.INSTANCE.getCustomMacros(project);
				result.addAllElements(testMacrosCompletions);
				result.addAllElements(testCompletions(customMacros));
			}
		});

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(LatteTypes.T_MACRO_VAR_TYPE), new ClassCompletionProvider());
       	//extend(CompletionType.BASIC, StandardPatterns.instanceOf(PsiElement.class), new ClassCompletionProvider());

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
				new CompletionProvider<CompletionParameters>() {
					@Override
					public void addCompletions(@NotNull final CompletionParameters parameters, final ProcessingContext context, @NotNull final CompletionResultSet _result) {
						Project project = parameters.getOriginalFile().getProject();

						PsiFile x = PhpStaticFactoryTypeProvider.getMetaFile(project);

						//if(parameters == null || ! MagicentoProjectComponent.isEnabled(parameters.getOriginalFile().getProject())) {
						//	return;
						//}

						PsiElement currentElement = parameters.getPosition();

						if (currentElement != null) {
							List<LookupElement> elements = null;
							final String prefix = _result.getPrefixMatcher().getPrefix();

							String filePath = parameters.getOriginalFile().getVirtualFile().getPath();
							elements = getAutocompleteForClassName(filePath);

							// autocomplete classname
							if (isClassnameAutocomplete(currentElement)) {
								//String filePath = parameters.getOriginalFile().getVirtualFile().getPath();
								elements = getAutocompleteForClassName(filePath);
							}

							if (elements != null && elements.size() > 0) {
								_result.addAllElements(elements);
								// we are using the prioritized lookup inside every get* method because this general solutions is failing for example for getAutocompleteForFactory, I don't know why
//                            int count = 10000;
//                            for(LookupElement element : elements){
//                                count++;
//                                _result.addElement(PrioritizedLookupElement.withPriority(element, count));
//                                //_result.addElement(element);
//                            }
							}
						}
					}
				});
	}

	static boolean isIssetOrUnset(PsiElement position) {
		if (PhpPsiUtil.isOfType(position, PhpTokenTypes.IDENTIFIER)) {
			position = position.getParent();
		}

		if (position instanceof ConstantReference) {
			position = position.getParent();
		}

		return position instanceof PhpIsset || position instanceof PhpUnset;
	}

	private static boolean isList(PsiElement position) {
		MultiassignmentExpression list = (MultiassignmentExpression)PhpPsiUtil.getParentByCondition(position, true, MultiassignmentExpression.INSTANCEOF, GroupStatement.INSTANCEOF);
		return list != null && !PsiTreeUtil.isAncestor(list.getValue(), position, true);
	}


	static boolean isGlobal(PsiElement position) {
		Variable variable = (Variable) PhpPsiUtil.getParentByCondition(position, false, Variable.INSTANCEOF, GroupStatement.INSTANCEOF);
		if (variable != null && variable.getParent() instanceof Global && PhpPsiUtil.findNextSiblingOfAnyType(variable.getFirstChild(), new IElementType[]{PhpTokenTypes.chLBRACE, PhpTokenTypes.chRBRACE}) == null) {
			return true;
		} else {
			Statement statement = (Statement)PhpPsiUtil.getParentByCondition(position, false, Statement.INSTANCEOF, GroupStatement.INSTANCEOF);
			if (statement != null) {
				PsiElement sibling = statement.getPrevSibling();
				if (sibling instanceof Global && !PhpPsiUtil.isOfType(sibling.getLastChild(), PhpTokenTypes.opSEMICOLON)) {
					return true;
				}
			}

			return false;
		}
	}


	protected List<LookupElement> getAutocompleteForClassName(String filePath) {
		List<LookupElement> elements = new ArrayList<LookupElement>();
		if (filePath != null) {
			//String className = Magicento.getClassNameFromFilePath(filePath);
			String className = "test";
			if (className != null) {

				LookupElement element = LookupElementBuilder.create(LookupElementBuilder.create(className));

				elements.add(PrioritizedLookupElement.withPriority(element, 1000));
			}
		}
		return elements;
	}
/*
	protected static class PhpVariableCompletionProvider extends CompletionProvider<CompletionParameters> {
		protected PhpVariableCompletionProvider() {
		}

		protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
			if (parameters == null) {
				$$$reportNull$$$0(0);
			}

			if (context == null) {
				$$$reportNull$$$0(1);
			}

			if (result == null) {
				$$$reportNull$$$0(2);
			}

			result = PhpCompletionContributor.patchResultIfNeeded(result);
			PsiElement position = parameters.getPosition();
			boolean global = PhpCompletionContributor.isGlobal(position);
			boolean allGlobals = parameters.getInvocationCount() > 1 || global;
			PhpUseList useList = PhpUseImpl.getUseList(position);
			if (useList != null) {
				allGlobals = false;
				position = useList.getParent().getParent();
			} else if (!allGlobals) {
				result.addLookupAdvertisement(PhpBundle.message("completion.press.again.to.see.global.variants", new Object[]{PhpCompletionContributor.getCompletionActionShortcut()}));
			}

			if (!PhpCompletionContributor.isNamespace(position.getPrevSibling()) && !PhpCompletionContributor.isForeachKeyOrValue(position)) {
				boolean smart = parameters.getCompletionType() == CompletionType.SMART;
				boolean allSuperGlobals = !smart && !global && useList == null && (allGlobals || !PhpCompletionContributor.isList(position) && !PhpCompletionContributor.isIssetOrUnset(position));
				CompletionResultSetWrapper wrappedResult = PhpCompletionSorting.wrapResult(parameters, context, result);
				PhpVariantsUtil.getVariableVariants(position, allGlobals, allSuperGlobals).stream().filter((e) -> {
					return useList == null || !"this".equals(e.getLookupString());
				}).forEach(wrappedResult::addElement);
			}
		}
	}*/


	/**
	 * checks if user is trying to autocomplete the name of the class ("class ...")
	 *
	 * @param currentElement
	 * @return
	 */
	protected boolean isClassnameAutocomplete(PsiElement currentElement) {
		PsiElement prevSibling = currentElement.getPrevSibling();
		while (prevSibling != null && prevSibling instanceof PsiWhiteSpace) {
			prevSibling = prevSibling.getPrevSibling();
		}
		if (prevSibling != null && prevSibling.getText().equals("class")) {
			return true;
		}
		return false;
	}

	/**
	 * Prepares lists of lookup elements for both classic and attribute standard macros.
	 */
	private void initStandardMacrosCompletions() {
		Map<String, LatteMacro> macros = LatteConfiguration.INSTANCE.getStandardMacros();
		classicMacrosCompletions = getClassicMacroCompletions(macros);
		attrMacrosCompletions = getAttrMacroCompletions(macros);
		testMacrosCompletions = testCompletions(macros);
	}

	/**
	 * Builds list of lookup elements for code completion of classic macros.
	 */
	private List<LookupElement> testCompletions(Map<String, LatteMacro> macros) {
		List<LookupElement> lookupElements = new ArrayList<LookupElement>(macros.size());
		for (LatteMacro macro : macros.values()) {
			if (macro.type != LatteMacro.Type.ATTR_ONLY) {
				lookupElements.add(LookupElementBuilder.create(macro.name));
			}
		}
		return lookupElements;
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
