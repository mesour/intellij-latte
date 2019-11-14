package com.jantvrdik.intellij.latte.psi.factories;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LattePhpMethod;

public class LattePhpMethodFactory {
    public static LattePhpMethod createPhpMethod(Project project, String name) {
        final LatteFile file = createFile(project, name);
        return (LattePhpMethod) file.getFirstChild().getFirstChild();
    }

    public static LatteFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (LatteFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, LatteFileType.INSTANCE, text);
    }
}