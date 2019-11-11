package com.jantvrdik.intellij.latte.psi.factories;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LatteVariableElement;

public class LatteVariableFactory {
    public static LatteVariableElement createVariable(Project project, String name) {
        final LatteFile file = createFile(project, name);
        return (LatteVariableElement) file.getFirstChild().getFirstChild();
    }

    public static LatteFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (LatteFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, LatteFileType.INSTANCE, text);
    }
}