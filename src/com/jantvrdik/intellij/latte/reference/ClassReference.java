package com.jantvrdik.intellij.latte.reference;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ClassReference {

	@NotNull
	private PsiElement latteFile;

	@NotNull
	private String name;

	@NotNull
	private String template;

	@Nullable
	private String parameter;

	public ClassReference(@NotNull PsiElement latteFile, @NotNull String name) {
		this.latteFile = latteFile;
		this.name = name;
	}

	public ClassReference(@NotNull PsiElement latteFile, @NotNull String name, @NotNull String template) {
		this(latteFile, name);
		this.template = template;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@NotNull
	public String getTemplate() {
		return template;
	}

	@Nullable
	public String getParameter() {
		return parameter;
	}

	@Nullable
	public PsiElement getLatteFile() {
		return latteFile;
	}

	@NotNull
	public ClassReference withParameter(@Nullable String parameter) {
		this.parameter = parameter;
		return this;
	}
}