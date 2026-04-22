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

package dev.daymor.ultimanexus.jvm.gradle.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PmdRulesetMergerTest {

    private val baseRuleset = """
        <?xml version="1.0"?>
        <ruleset name="Base">
            <rule ref="category/java/bestpractices.xml"/>
        </ruleset>
    """.trimIndent()

    private val fragment = """
        <?xml version="1.0"?>
        <ruleset name="Generated">
            <rule ref="category/java/errorprone.xml/MissingStaticMethodInNonInstantiatableClass">
                <properties>
                    <property name="violationSuppressXPath"
                              value="//ClassDeclaration[@SimpleName='SpringRestApi']"/>
                </properties>
            </rule>
        </ruleset>
    """.trimIndent()

    @Test
    fun `should return base unchanged when no fragments provided`() {
        val merged = PmdRulesetMerger.merge(baseRuleset, emptyList())
        assertThat(merged).isEqualTo(baseRuleset)
    }

    @Test
    fun `should splice fragment rules into the base ruleset`() {
        val merged = PmdRulesetMerger.merge(baseRuleset, listOf(fragment))
        assertThat(merged)
            .contains("bestpractices.xml")
            .contains("MissingStaticMethodInNonInstantiatableClass")
            .contains("SpringRestApi")
    }

    @Test
    fun `should end with single ruleset close tag`() {
        val merged = PmdRulesetMerger.merge(baseRuleset, listOf(fragment))
        assertThat(merged.split("</ruleset>")).hasSize(2)
        assertThat(merged.trim()).endsWith("</ruleset>")
    }

    @Test
    fun `should produce empty ruleset when base is null`() {
        val merged = PmdRulesetMerger.merge(null, listOf(fragment))
        assertThat(merged).contains("<ruleset").contains("MissingStaticMethodInNonInstantiatableClass")
    }

    @Test
    fun `should merge multiple fragments targeting the same rule into a single rule entry`() {
        val secondFragment = """
            <?xml version="1.0"?>
            <ruleset name="Generated">
                <rule ref="category/java/errorprone.xml/MissingStaticMethodInNonInstantiatableClass">
                    <properties>
                        <property name="violationSuppressXPath"
                                  value="//ClassDeclaration[@SimpleName='Tier1Showcase'] or //ClassDeclaration[@SimpleName='AdvancedShowcase']"/>
                    </properties>
                </rule>
            </ruleset>
        """.trimIndent()

        val merged = PmdRulesetMerger.merge(baseRuleset, listOf(fragment, secondFragment))

        val ruleRefOccurrences = merged.split(
            "<rule ref=\"category/java/errorprone.xml/MissingStaticMethodInNonInstantiatableClass\">"
        ).size - 1
        assertThat(ruleRefOccurrences).isEqualTo(1)
        assertThat(merged).contains("SpringRestApi")
        assertThat(merged).contains("Tier1Showcase")
        assertThat(merged).contains("AdvancedShowcase")
    }
}
