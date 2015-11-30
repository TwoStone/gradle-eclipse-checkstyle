package com.github.twostone.gradle.plugins.eclipsecheckstyle

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.eclipse.model.EclipseModel

public class GenerateEclipseCheckstyle extends DefaultTask{

    @InputFile
    @Optional
    File inputFile

    @OutputFile
    File outputFile

    @TaskAction
    def doAction() {
        CheckstyleExtension extension = project.extensions.getByName('checkstyle')

        def writer = outputFile.withWriter {
            def xml = new MarkupBuilder(it)
            def configLocation = extension.configFile.toPath().resolve(project.projectDir.toPath()).toString()
            def configName = "${project.name} - Gradle Eclipse Checkstyle"

            xml.'fileset-config'('file-format-version':'1.2.0', 'simple-config':true, 'sync-formatter': false) {
                'local-check-config'(
                        name: configName,
                        location: configLocation,
                        type: 'project',
                        description: 'Gradle Eclipse Checkstyle') {
                    'additional-data'(
                            name: 'protect-config-file',
                            value: "false")
                }
                'fileset'(
                        name: 'all',
                        enabled: true,
                        'check-config-name': configName,
                        local: true) {
                    'file-match-pattern' (
                            'match-pattern': '.',
                            'include-pattern': true
                    )
                }
            }
        }
    }
}
