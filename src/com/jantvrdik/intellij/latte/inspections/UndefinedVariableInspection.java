package com.jantvrdik.intellij.latte.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import com.jantvrdik.intellij.latte.utils.PsiPositionedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UndefinedVariableInspection extends LocalInspectionTool {

	private static Set<String> macros;


	@NotNull
	@Override
	public String getShortName() {
		return "UndefinedVariable";
	}

	@Nullable
	@Override
	public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
		if (!(file instanceof LatteFile)) {
			return null;
		}

		List<PsiPositionedElement> variables = LatteUtil.findVariablesInFile(manager.getProject(), file.getVirtualFile(), true);

		final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
		for (PsiPositionedElement variable : variables) {
			if (!(variable instanceof LatteVariableElement)) {
				continue;
			}

			if (!((LatteVariableElement) variable).isDefinition()) {
				String variableName = ((LatteVariableElement) variable).getVariableName();
				LatteVariableElement found = findDefinition(variableName, variables);
				if (found == null) {
					//ProblemDescriptor problem = manager.createProblemDescriptor(variable, "Undefined variable '" + variableName + "'", true, ProblemHighlightType.GENERIC_ERROR, isOnTheFly);
					//problems.add(problem);
				}
			}
		}
		return problems.toArray(new ProblemDescriptor[problems.size()]);
	}

	@Nullable
	private static LatteVariableElement findDefinition(String variableName, List<PsiPositionedElement> variables) {
		for (PsiPositionedElement variable : variables) {
			if (!(variable instanceof LatteVariableElement)) {
				continue;
			}

			if (((LatteVariableElement) variable).getVariableName().equals(variableName) && ((LatteVariableElement) variable).isDefinition()) {
				return (LatteVariableElement) variable;
			}
		}
		return null;
	}
}
