/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.remapcheck;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskContainer;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.RemapSourcesJarTask;

public abstract class RemapCheckExtension {
    private final Project project;

    public RemapCheckExtension(Project project) {this.project = project;}

    public void checkRemap(String targetProject, Object targetMapping) {
        final String minecraftVersion = (String) project.property("minecraft_version");
        if (minecraftVersion == null) throw new IllegalStateException("Missing `minecraft_version` property");

        Project target = project.evaluationDependsOn(targetProject);

        LoomGradleExtension loomEx =
            (LoomGradleExtension) project.getExtensions().getByType(LoomGradleExtensionAPI.class);

        DependencyHandler deps = project.getDependencies();
        deps.add("minecraft", "com.mojang:minecraft:" + minecraftVersion);
        deps.add("mappings", targetMapping);

        TaskContainer tasks = project.getTasks();
        RemapJarTask checkRemap = tasks.create("checkRemap", RemapJarTask.class, task -> {
            task.getClasspath().from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY));
            task.dependsOn(target.getTasks().named("remapJar"));

            task.getArchiveClassifier().set("remapCheck");

            task.getInputFile()
                .set(target.getTasks().named("remapJar", RemapJarTask.class).flatMap(RemapJarTask::getArchiveFile));
            task.getSourceNamespace().set("intermediary");
            task.getTargetNamespace().set("named");

            task.getRemapperIsolation().set(true);
        });

        RemapSourcesJarTask checkRemapSource = tasks.create("checkRemapSource", RemapSourcesJarTask.class, task -> {
            task.getClasspath().from(loomEx.getMinecraftJarsCollection(MappingsNamespace.INTERMEDIARY));
            task.dependsOn(target.getTasks().named("remapSourcesJar"));

            task.getArchiveClassifier().set("remapCheckSource");

            task.getInputFile().set(target.getTasks().named("remapSourcesJar", RemapSourcesJarTask.class)
                .flatMap(RemapSourcesJarTask::getArchiveFile));
            task.getSourceNamespace().set("intermediary");
            task.getSourceNamespace().set("named");

            task.getRemapperIsolation().set(true);
        });

        tasks.named("assemble").configure(task -> task.dependsOn(checkRemap, checkRemapSource));
    }
}
