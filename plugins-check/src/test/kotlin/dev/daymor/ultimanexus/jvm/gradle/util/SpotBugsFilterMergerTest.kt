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

class SpotBugsFilterMergerTest {

    private val baseFilter = """
        <?xml version="1.0" encoding="UTF-8"?>
        <FindBugsFilter>
            <Match>
                <Class name="com.example.Base"/>
            </Match>
        </FindBugsFilter>
    """.trimIndent()

    private val fragmentOne = """
        <?xml version="1.0" encoding="UTF-8"?>
        <FindBugsFilter>
            <Match>
                <Bug pattern="EI_EXPOSE_REP"/>
                <Class name="com.example.Alpha"/>
            </Match>
        </FindBugsFilter>
    """.trimIndent()

    private val fragmentTwo = """
        <?xml version="1.0" encoding="UTF-8"?>
        <FindBugsFilter>
            <Match>
                <Bug pattern="EI_EXPOSE_REP2"/>
                <Class name="com.example.Beta"/>
            </Match>
        </FindBugsFilter>
    """.trimIndent()

    @Test
    fun `should return base unchanged when no fragments provided`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(baseFilter, emptyList())

        // Assert
        assertThat(merged).isEqualTo(baseFilter)
    }

    @Test
    fun `should splice fragment bodies into the base filter`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(baseFilter, listOf(fragmentOne, fragmentTwo))

        // Assert
        assertThat(merged)
            .contains("com.example.Base")
            .contains("com.example.Alpha")
            .contains("com.example.Beta")
            .contains("EI_EXPOSE_REP")
            .contains("EI_EXPOSE_REP2")
    }

    @Test
    fun `should preserve base xml prolog`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(baseFilter, listOf(fragmentOne))

        // Assert
        assertThat(merged).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    @Test
    fun `should end with single FindBugsFilter close tag`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(baseFilter, listOf(fragmentOne, fragmentTwo))

        // Assert — exactly one closing tag, at the end
        assertThat(merged.split("</FindBugsFilter>")).hasSize(2)
        assertThat(merged.trim()).endsWith("</FindBugsFilter>")
    }

    @Test
    fun `should produce empty filter when base is null and no fragments`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(null, emptyList())

        // Assert
        assertThat(merged).contains("<FindBugsFilter>").contains("</FindBugsFilter>")
    }

    @Test
    fun `should apply fragments onto a null base`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge(null, listOf(fragmentOne))

        // Assert
        assertThat(merged).contains("com.example.Alpha").contains("EI_EXPOSE_REP")
    }

    @Test
    fun `should skip malformed fragments without aborting the merge`() {
        // Arrange
        val malformed = "<not-a-filter/>"

        // Act
        val merged = SpotBugsFilterMerger.merge(baseFilter, listOf(malformed, fragmentOne))

        // Assert — malformed skipped, well-formed fragment still spliced
        assertThat(merged).contains("com.example.Alpha").doesNotContain("not-a-filter")
    }

    @Test
    fun `should return base unchanged when base has no root element`() {
        // Arrange
        val brokenBase = "<broken>no root</broken>"

        // Act
        val merged = SpotBugsFilterMerger.merge(brokenBase, listOf(fragmentOne))

        // Assert
        assertThat(merged).isEqualTo(brokenBase)
    }

    @Test
    fun `should treat blank base as null and produce empty filter with fragments`() {
        // Arrange, Act
        val merged = SpotBugsFilterMerger.merge("   ", listOf(fragmentOne))

        // Assert
        assertThat(merged).contains("com.example.Alpha")
    }
}
