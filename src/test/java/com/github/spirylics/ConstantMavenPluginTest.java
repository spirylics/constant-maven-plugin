package com.github.spirylics;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConstantMavenPluginTest {
    File baseDir = new File(getClass().getResource("java").getFile());

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void testJava() throws Exception {
        JavaGen mojo = (JavaGen) rule.lookupConfiguredMojo(baseDir, "java");
        mojo.execute();

        File constantFile = mojo.getConstantFile();
        assertTrue(constantFile.getPath() + " not generated", constantFile.exists());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals("Constants class not compilable", 0, compiler.run(null, null, null, constantFile.getAbsolutePath()));

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{mojo.directory.toURI().toURL()});
        Class<?> constantClass = Class.forName(mojo.name, true, classLoader);
        assertEquals(5, constantClass.getFields().length);
        assertTrue(constantClass.getField("constant_boolean").getBoolean(null));
        assertEquals(1, constantClass.getField("constant_short").getShort(null));
        assertEquals(1111111, constantClass.getField("constant_int").getInt(null));
        assertEquals(1111111111111111111L, constantClass.getField("constant_long").getLong(null));
        assertEquals("foo", constantClass.getField("constant_string").get(null));
    }

}
