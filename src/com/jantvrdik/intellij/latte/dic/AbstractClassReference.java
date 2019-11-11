package com.jantvrdik.intellij.latte.dic;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIndex;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
abstract public class AbstractClassReference extends PsiPolyVariantReferenceBase<PsiElement> {

	protected String className;
	protected boolean usePrivateServices = true;

	public AbstractClassReference(PsiElement psiElement) {
		super(psiElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		//ContainerCollectionResolver.ServiceCollector collector = ContainerCollectionResolver
		//		.ServiceCollector.create(getElement().getProject());

		// Return the PsiElement for the class corresponding to the serviceId
		//String serviceClass = collector.resolve(serviceId);
		String serviceClass = "\\UlovDomov\\Permissions\\Permission";
		if (serviceClass == null) {
			return new ResolveResult[0];
		}

		return PsiElementResolveResult.createResults(PhpIndex.getInstance(getElement().getProject()).getAnyByFQN(serviceClass));
	}
}