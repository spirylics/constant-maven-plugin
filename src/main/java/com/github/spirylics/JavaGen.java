package com.github.spirylics;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

@Mojo(name = "java", defaultPhase = GENERATE_SOURCES)
public class JavaGen extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    File directory;

    @Parameter(readonly = true, required = true)
    String name;

    @Parameter(defaultValue = "${project.properties}", readonly = true, required = true)
    Properties properties;

    final List<Function<String, Object>> typeFns = Arrays.asList(
            v -> {
                if ("true".equals(v) || "false".equals(v)) return new Boolean(v);
                else throw new IllegalArgumentException("not a boolean");
            },
            v -> new Short(v),
            v -> new Integer(v),
            v -> new Long(v),
            v -> v);

    final Map<Class<?>, Function<Object, String>> toStringMap = new ImmutableMap.Builder<Class<?>, Function<Object, String>>()
            .put(Long.class, o -> o + "L")
            .put(String.class, o -> "\"" + o + "\"")
            .build();

    public void execute() throws MojoExecutionException {
        Path constantPath = getConstantFile().toPath();
        List<String> lines = new ArrayList<>();
        lines.add(String.format("package %s;", getPackage()));
        lines.add(String.format("public interface %s {", getSimpleClassName()));
        properties.entrySet().stream().forEach(e -> lines.add(getConstantDeclaration(e)));
        lines.add("}");
        try {
            Files.deleteIfExists(constantPath);
            Files.createDirectories(constantPath.getParent());
            Files.write(constantPath, lines, Charset.forName(properties.getProperty("project.build.sourceEncoding")), APPEND, CREATE);
        } catch (IOException e) {
            throw new MojoExecutionException("generate constants FAILED", e);
        }
    }

    File getConstantFile() {
        return new File(directory.getPath() + "/" + name.replace(".", "/") + ".java");
    }

    String getPackage() {
        return name.replace("." + getSimpleClassName(), "");
    }

    String getSimpleClassName() {
        return name.substring(name.lastIndexOf('.') + 1, name.length());
    }

    String getConstantDeclaration(Map.Entry<Object, Object> entry) {
        Object realValue = getRealValue(String.valueOf(entry.getValue()));
        return String.format("%s %s = %s;",
                toStringType(realValue),
                String.valueOf(entry.getKey()).replace(".", "_"),
                toStringValue(realValue));
    }

    String toStringType(Object o) {
        return Primitives.isWrapperType(o.getClass()) ? Primitives.unwrap(o.getClass()).toString() : o.getClass().getName();
    }

    String toStringValue(Object o) {
        Function<Object, String> fn = toStringMap.get(o.getClass());
        if (fn == null) {
            return String.valueOf(o);
        }
        return fn.apply(o);
    }

    Object getRealValue(String value) {
        for (Function<String, Object> typeFn : typeFns) {
            try {
                return typeFn.apply(value);
            } catch (Exception e) {
            }
        }
        return null;
    }
}
