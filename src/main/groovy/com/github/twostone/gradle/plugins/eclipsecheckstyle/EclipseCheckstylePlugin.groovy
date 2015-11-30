package com.github.twostone.gradle.plugins.eclipsecheckstyle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.BuildCommand
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.eclipse.model.EclipseProject

import javax.inject.Inject

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
        if (project.tasks.findByName('eclipseCheckstyle')) {
            return
        }

        project.afterEvaluate {
            if (!['org.gradle.eclipse', 'org.gradle.checkstyle'].every {
                project.pluginManager.hasPlugin(it)
            }) {
                configureEclipseProject(project)
                createEclipseCheckstyleTask(project)
            }
        }
    }

    def createEclipseCheckstyleTask(Project project) {
        EclipsePlugin eclipsePlugin = project.pluginManager.findPlugin('org.gradle.eclipse')
        if (project.tasks.findByName('eclipseCheckstyle')) {
            return
        }
        GenerateEclipseCheckstyle task = project.tasks.create('eclipseCheckstyle', GenerateEclipseCheckstyle) {
            description = 'Generates the Eclipse Checkstyle file'
            inputFile = project.file('.checkstyle')
            outputFile = project.file('.checkstyle')
        }

        project.tasks.getByName('eclipse').dependsOn(task)
    }

    def configureEclipseProject(Project p) {
        if (p.pluginManager.hasPlugin('org.gradle.java')) {
            EclipseModel model = project.extensions.findByName('eclipse')
            EclipseProject project = model.project

            addBuildCommand(project)
            addNature(project)
        }
    }

    def addNature(EclipseProject eclipseProject) {
        eclipseProject.natures.add(
            eclipseProject.natures.indexOf("org.eclipse.jdt.core.javanature") + 1,
            "net.sf.eclipsecs.core.CheckstyleNature"
        )
    }

    def addBuildCommand(EclipseProject eclipseProject) {
        eclipseProject.buildCommands.add(
            eclipseProject.findIndexOf { name == 'org.eclipse.jdt.core.javabuilder' } + 1,
            new BuildCommand('net.sf.eclipsecs.core.CheckstyleBuilder'))
    }

    def hasExtensions(Project project, String... extensions) {
        return extensions.every {
            project.extensions.findByName(it) != null
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
}
