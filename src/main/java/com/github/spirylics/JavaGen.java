package com.github.spirylics;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

@Mojo(name = "java", defaultPhase = GENERATE_SOURCES)
public class JavaGen extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    File directory;

    @Parameter(readonly = true, required = true)
    String name;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    public void execute() throws MojoExecutionException {
        Path constantPath = getConstantFile().toPath();
        List<String> lines = new ArrayList<>();
        lines.add(String.format("package %s;", getPackage()));
        lines.add(String.format("public interface %s {", getSimpleClassName()));

        lines.add("}");
        try {
            Files.deleteIfExists(constantPath);
            Files.createDirectories(constantPath.getParent());
            Files.write(constantPath, lines, UTF_8, APPEND, CREATE);
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
}
