package com.jantvrdik.intellij.latte.stubs.indexes;

import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.reference.ClassReference;
import com.jantvrdik.intellij.latte.util.LatteUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LatteStubIndex extends FileBasedIndexExtension<String, Void> {

	public static final ID<String, Void> KEY = ID.create("com.jantvrdik.intellij.latte.latte_usages");
	private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

	@NotNull
	@Override
	public ID<String, Void> getName() {
		return KEY;
	}

	@NotNull
	@Override
	public DataIndexer<String, Void, FileContent> getIndexer() {
		return inputData -> {
			final Map<String, Void> map = new THashMap<>();

			PsiFile psiFile = inputData.getPsiFile();
			//if(!Symfony2ProjectComponent.isEnabledForIndex(psiFile.getProject())) {
			//	return map;
			//}

			if(!(psiFile instanceof LatteFile)) {
				return map;
			}
/*
            Consumer<ClassReference> myConsumer = (ClassReference templateInclude) ->
            {
                if (templateInclude.getLatteFile() != null) {
                    map.put(templateInclude.getLatteFile().getContainingFile().getOriginalFile().getName(), null);
                }
            };

            LatteUtil.visitTemplateIncludes((LatteFile) psiFile, myConsumer);
*/
			LatteUtil.visitTemplateIncludes((LatteFile) psiFile, templateInclude ->
					map.put(templateInclude.getLatteFile().getContainingFile().getOriginalFile().getName(), null)
			);

			return map;
		};

	}

	@NotNull
	@Override
	public KeyDescriptor<String> getKeyDescriptor() {
		return this.myKeyDescriptor;
	}

	@NotNull
	@Override
	public DataExternalizer<Void> getValueExternalizer() {
		return VoidDataExternalizer.INSTANCE;
	}

	@NotNull
	@Override
	public FileBasedIndex.InputFilter getInputFilter() {
		return file -> file.getFileType() == LatteFileType.INSTANCE;
	}

	@Override
	public boolean dependsOnFileContent() {
		return true;
	}

	@Override
	public int getVersion() {
		return 3;
	}

	/*
	 * Visit all possible Twig include file pattern
	 *
	public static void visitTemplateIncludes(@NotNull LatteFile twigFile, @NotNull Consumer<TemplateInclude> consumer) {
		PsiTreeUtil.collectElements(twigFile, psiElement -> {
			if(psiElement instanceof TwigTagWithFileReference) {
				// {% include %}
				if(psiElement.getNode().getElementType() == TwigElementTypes.INCLUDE_TAG) {
					for (String templateName : getIncludeTagStrings((TwigTagWithFileReference) psiElement)) {
						if(StringUtils.isNotBlank(templateName)) {
							consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.INCLUDE));
						}
					}
				}

				// {% import "foo.html.twig"
				PsiElement importTag = PsiElementUtils.getChildrenOfType(psiElement, TwigPattern.getTagNameParameterPattern(TwigElementTypes.IMPORT_TAG, "import"));
				if(importTag != null) {
					String templateName = importTag.getText();
					if(StringUtils.isNotBlank(templateName)) {
						consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.IMPORT));
					}
				}

				// {% from 'forms.html' import ... %}
				PsiElement fromTag = PsiElementUtils.getChildrenOfType(psiElement, TwigPattern.getTagNameParameterPattern(TwigElementTypes.IMPORT_TAG, "from"));
				if(fromTag != null) {
					String templateName = fromTag.getText();
					if(StringUtils.isNotBlank(templateName)) {
						consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.IMPORT));
					}
				}
			} else if(psiElement instanceof TwigCompositeElement) {
				// {{ include() }}
				// {{ source() }}
				PsiElement includeTag = PsiElementUtils.getChildrenOfType(psiElement, TwigPattern.getPrintBlockOrTagFunctionPattern("include", "source"));
				if(includeTag != null) {
					String templateName = includeTag.getText();
					if(StringUtils.isNotBlank(templateName)) {
						consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.INCLUDE_FUNCTION));
					}
				}

				// {% embed "foo.html.twig"
				PsiElement embedTag = PsiElementUtils.getChildrenOfType(psiElement, TwigPattern.getEmbedPattern());
				if(embedTag != null) {
					String templateName = embedTag.getText();
					if(StringUtils.isNotBlank(templateName)) {
						consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.EMBED));
					}
				}

				if(psiElement.getNode().getElementType() == TwigElementTypes.TAG) {
					PsiElement tagElement = PsiElementUtils.getChildrenOfType(psiElement, PlatformPatterns.psiElement().withElementType(TwigTokenTypes.TAG_NAME));
					if(tagElement != null) {
						String text = tagElement.getText();
						if("form_theme".equals(text)) {
							// {% form_theme form.child 'form/fields_child.html.twig' %}
							PsiElement childrenOfType = PsiElementUtils.getNextSiblingAndSkip(tagElement, TwigTokenTypes.STRING_TEXT,
									TwigTokenTypes.IDENTIFIER, TwigTokenTypes.SINGLE_QUOTE, TwigTokenTypes.DOUBLE_QUOTE, TwigTokenTypes.DOT
							);

							if(childrenOfType != null) {
								String templateName = childrenOfType.getText();
								if(StringUtils.isNotBlank(templateName)) {
									consumer.consume(new TemplateInclude(psiElement, templateName, TemplateInclude.TYPE.FORM_THEME));
								}
							}

							// {% form_theme form.child with ['form/fields_child.html.twig'] %}
							PsiElement withElement = PsiElementUtils.getNextSiblingOfType(tagElement, PlatformPatterns.psiElement().withElementType(TwigTokenTypes.IDENTIFIER).withText("with"));
							if(withElement != null) {
								// find LITERAL "[", "{"
								PsiElement arrayStart = PsiElementUtils.getNextSiblingAndSkip(tagElement, TwigElementTypes.LITERAL,
										TwigTokenTypes.IDENTIFIER, TwigTokenTypes.SINGLE_QUOTE, TwigTokenTypes.DOUBLE_QUOTE, TwigTokenTypes.DOT
								);

								if(arrayStart != null) {
									PsiElement firstChild = arrayStart.getFirstChild();
									if(firstChild != null) {
										visitStringInArray(firstChild, pair ->
												consumer.consume(new TemplateInclude(psiElement, pair.getFirst(), TemplateInclude.TYPE.FORM_THEME))
										);
									}
								}
							}
						}
					}
				}

				for (TwigFileUsage extension : TWIG_FILE_USAGE_EXTENSIONS.getExtensions()) {
					if (extension.isIncludeTemplate(psiElement)) {
						for (String template : extension.getIncludeTemplate(psiElement)) {
							consumer.consume(new TemplateInclude(psiElement, template, TemplateInclude.TYPE.INCLUDE));
						}
					}
				}
			}

			return false;
		});
	}*/

}