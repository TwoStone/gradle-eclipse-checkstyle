package com.github.twostone.gradle.plugins.eclipsecheckstyle

import groovy.xml.MarkupBuilder
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.plugins.ide.eclipse.model.BuildCommand;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.eclipse.model.EclipseProject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EclipseCheckstylePlugin implements Plugin<Project> {

    private final Instantiator instantiator
    private EclipseModel eclipseModel
    private CheckstyleExtension checkstyleExtension
    private EclipsePlugin eclipsePlugin

    @Inject
    EclipseCheckstylePlugin(final Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    public void apply(final Project project) {

        project.pluginManager.withPlugin('org.gradle.eclipse') { p ->
            eclipseModel = project.extensions.findByName('eclipse')
            eclipsePlugin = p
        }

        project.pluginManager.withPlugin('org.gradle.checkstyle') {
            checkstyleExtension = project.extensions.getByName('checkstyle')
        }

        project.afterEvaluate {
            if (this.eclipseModel && this.checkstyleExtension) {
                configureEclipseProject(it)
                configureEclipseCheckstyle(it)
            }
        }
    }

    def configureEclipseCheckstyle(Project project) {
        maybeAddTask(project, 'eclipseCheckstyle', GenerateEclipseCheckstyle) {
            description = 'Generates the Eclipse Checkstyle file'
            inputFile = project.file('.checkstyle')
            outputFile = project.file('.checkstyle')

            eclipseModel = this.eclipseModel
            checkstyleExtension = this.checkstyleExtension
        }
    }

    def configureEclipseProject(final Project project) {
        project.plugins.withType(JavaBasePlugin) {
            final EclipseProject eclipseProject = eclipseModel.project
            int index = eclipseProject.buildCommands.findIndexOf {
                it.name == 'org.eclipse.jdt.core.javabuilder'
            }

            eclipseProject.buildCommands.add(index + 1, new BuildCommand('net.sf.eclipsecs.core.CheckstyleBuilder'))

            eclipseProject.natures.add(
                    eclipseProject.natures.indexOf("org.eclipse.jdt.core.javanature") + 1,
                    "net.sf.eclipsecs.core.CheckstyleNature")
        }
    }

    private void maybeAddTask(Project project, String taskName, Class taskType, Closure action) {
        if (project.tasks.findByName(taskName)) { return }
        def task = project.tasks.create(taskName, taskType)
        project.configure(task, action)
        eclipsePlugin.addWorker(task)
    }

}
