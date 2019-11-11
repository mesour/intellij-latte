package com.jantvrdik.intellij.latte.dic;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ClassReference extends AbstractClassReference {

	public ClassReference(@NotNull StringLiteralExpression element) {
		super(element);
		this.className = element.getContents();
	}

	public ClassReference(@NotNull StringLiteralExpression element, boolean usePrivateServices) {
		this(element);
		this.usePrivateServices = usePrivateServices;
	}

}