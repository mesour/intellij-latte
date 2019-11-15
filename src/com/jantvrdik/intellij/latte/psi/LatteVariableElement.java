package com.jantvrdik.intellij.latte.psi;

public interface LatteVariableElement extends LattePhpBaseElement {

    public abstract String getVariableName();

    public abstract boolean isProperty();

    public abstract boolean isAnnotation();

    public abstract boolean isDefinition();

}