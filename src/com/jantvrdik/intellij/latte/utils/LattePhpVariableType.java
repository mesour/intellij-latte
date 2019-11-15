package com.jantvrdik.intellij.latte.utils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LattePhpVariableType {

	private final String type;
	private final String name;
	private final boolean nullable;

	public LattePhpVariableType(String type, boolean nullable) {
		this(null, type, nullable);
	}

	public LattePhpVariableType(String name, String type) {
		this(name, type, false);
	}

	public LattePhpVariableType(String name, String type, boolean nullable) {
		this.name = name == null ? null : (name.startsWith("$") ? name.substring(1) : name);
		this.type = type;
		this.nullable = nullable;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isClassOrInterfaceType() {
		return type != null && type.startsWith("\\");
	}

	@Nullable
	public Collection<PhpClass> getPhpClasses(Project project) {
		if (!isClassOrInterfaceType()) {
			return null;
		}

		return LattePhpUtil.getClassesByFQN(project, getType());
	}

	@Nullable
	public PhpClass getFirstPhpClass(Project project) {
		Collection<PhpClass> phpClasses = getPhpClasses(project);
		if (phpClasses == null) {
			return null;
		}
		return phpClasses.stream().findFirst().isPresent() ? phpClasses.stream().findFirst().get() : null;
	}

	@Override
	public String toString() {
		return "LattePhpVariableType(" + (nullable ? "@Nullable" : "@NotNull") + " " + type + ", name=" + name + ")";
	}
}