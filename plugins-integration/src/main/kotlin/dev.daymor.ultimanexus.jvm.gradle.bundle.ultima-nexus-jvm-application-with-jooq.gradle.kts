/*
 * Copyright (C) 2025 Malcolm Rozé.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-application-with-jooq
 *
 * Compatibility aggregator that applies the Ultima Nexus JVM Vaadin + htmx frontend
 * bundles together with the jOOQ code-generation plugin from a SINGLE convention-plugin
 * classloader.
 *
 * WHY THIS EXISTS — it works around a Vaadin flow-gradle-plugin bug (vaadin/flow#17665):
 * VaadinPrepareFrontendTask reads service inputs that its plugin only computes in
 * `project.afterEvaluate`. When the Vaadin plugin and the jOOQ plugin are contributed by
 * TWO different published convention-plugin modules, applying them perturbs the
 * afterEvaluate ordering so the Vaadin task is realised before its inputs are set, failing
 * the build with "property 'svc'/'tokenService'/'inputProperties' doesn't have a configured
 * value". Applying both from ONE module (one classloader) keeps the ordering intact.
 *
 * This is the ONLY place the frontend and persistence plugins are deliberately coupled. The
 * `plugins-frontend` and `plugins-persistence` modules stay independent and standalone;
 * remove this whole `plugins-integration` module once the upstream Vaadin bug is fixed.
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-frontend-vaadin")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-frontend-htmx")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.jooq-codegen")
}
