package com.jantvrdik.intellij.latte.psi.factories;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.jantvrdik.intellij.latte.LatteFileType;
import com.jantvrdik.intellij.latte.psi.LatteFile;
import com.jantvrdik.intellij.latte.psi.LatteMacroElement;

public class LatteMacroFactory {
    public static LatteMacroElement createMacro(Project project, String name) {
        final LatteFile file = createFile(project, name);
        return (LatteMacroElement) file.getFirstChild();
    }

    public static LatteFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (LatteFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, LatteFileType.INSTANCE, text);
    }
}