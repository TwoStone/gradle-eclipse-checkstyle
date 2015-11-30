import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import static org.junit.Assert.assertThat

/**
 * Created by nw on 30.11.2015.
 */
class EclipseCheckstylePluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    private File buildFile
    private List<File> pluginClasspath

    @Before
    public void setup() {
        this.buildFile = testProjectDir.newFile('build.gradle')

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    @Test
    public void testFileCreation() {
        def classpathString = pluginClasspath
                .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")

        buildFile << """
            buildscript {
                dependencies {
                    classpath files($classpathString)
                }
            }

            apply plugin: 'java'
            apply plugin: 'eclipse'
            apply plugin: 'checkstyle'
            apply plugin: 'com.github.twostone.eclipse-checkstyle'
        """

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('eclipse')
                .withDebug(true)
                .withGradleVersion("2.7")
                .build();

        println result

    }

}
