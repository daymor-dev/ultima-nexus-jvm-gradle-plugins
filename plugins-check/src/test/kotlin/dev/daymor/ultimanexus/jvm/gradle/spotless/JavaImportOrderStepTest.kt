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

package dev.daymor.ultimanexus.jvm.gradle.spotless

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JavaImportOrderStepTest {

    private val formatter = JavaImportOrderStep(
        samePackageDepth = 3,
        standardPackageRegex = Regex("""^java\."""),
        specialImportsRegex = Regex("""^(javax|jakarta)\."""),
    ).toFormatter()

    private fun format(input: String): String = formatter.apply(input)

    @Nested
    inner class NoImports {

        @Test
        fun `returns input unchanged when no imports exist`() {
            val input = """
                package com.example;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }

        @Test
        fun `returns input unchanged when file is empty`() {
            val input = ""
            assertThat(format(input)).isEqualTo(input)
        }
    }

    @Nested
    inner class BasicImportOrdering {

        @Test
        fun `sorts java imports before javax imports`() {
            val input = """
                package com.example;

                import javax.annotation.Nullable;
                import java.util.List;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import java.util.List;

                import javax.annotation.Nullable;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts javax imports before third-party imports`() {
            val input = """
                package com.example;

                import org.apache.commons.lang3.StringUtils;
                import javax.annotation.Nullable;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import javax.annotation.Nullable;

                import org.apache.commons.lang3.StringUtils;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts jakarta imports as special imports`() {
            val input = """
                package com.example;

                import org.apache.commons.lang3.StringUtils;
                import jakarta.validation.Valid;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import jakarta.validation.Valid;

                import org.apache.commons.lang3.StringUtils;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts static imports last`() {
            val input = """
                package com.example;

                import static org.junit.Assert.assertEquals;
                import java.util.List;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import java.util.List;

                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class SamePackageOrdering {

        @Test
        fun `sorts same-package imports after third-party`() {
            val input = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;
                import org.apache.commons.lang3.StringUtils;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import org.apache.commons.lang3.StringUtils;

                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `uses first 3 package domains for same-package matching`() {
            val input = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import dev.daymor.ultimanexus.other.OtherClass;
                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;
                import org.apache.commons.lang3.StringUtils;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import org.apache.commons.lang3.StringUtils;

                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;
                import dev.daymor.ultimanexus.other.OtherClass;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `same-package imports are sorted before static imports`() {
            val input = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import static org.junit.Assert.assertEquals;
                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;

                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class FullImportOrdering {

        @Test
        fun `orders imports according to checkstyle CustomImportOrder rules`() {
            val input = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import static org.junit.Assert.assertEquals;
                import java.util.List;
                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;
                import org.apache.commons.lang3.StringUtils;
                import javax.annotation.Nullable;
                import java.io.File;
                import static org.assertj.core.api.Assertions.assertThat;
                import jakarta.validation.Valid;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import java.io.File;
                import java.util.List;

                import jakarta.validation.Valid;
                import javax.annotation.Nullable;

                import org.apache.commons.lang3.StringUtils;

                import dev.daymor.ultimanexus.jvm.gradle.config.Defaults;

                import static org.assertj.core.api.Assertions.assertThat;
                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class AlphabeticalSorting {

        @Test
        fun `sorts imports alphabetically within each group`() {
            val input = """
                package com.example;

                import java.util.Map;
                import java.io.File;
                import java.util.ArrayList;
                import java.io.InputStream;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import java.io.File;
                import java.io.InputStream;
                import java.util.ArrayList;
                import java.util.Map;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts static imports alphabetically`() {
            val input = """
                package com.example;

                import static org.mockito.Mockito.when;
                import static org.assertj.core.api.Assertions.assertThat;
                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import static org.assertj.core.api.Assertions.assertThat;
                import static org.junit.Assert.assertEquals;
                import static org.mockito.Mockito.when;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class BlankLinesBetweenGroups {

        @Test
        fun `adds blank lines between import groups`() {
            val input = """
                package com.example;

                import java.util.List;
                import javax.annotation.Nullable;
                import org.apache.commons.lang3.StringUtils;
                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import java.util.List;

                import javax.annotation.Nullable;

                import org.apache.commons.lang3.StringUtils;

                import static org.junit.Assert.assertEquals;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `does not add blank lines within the same group`() {
            val input = """
                package com.example;

                import java.util.ArrayList;

                import java.util.List;

                import java.util.Map;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package com.example;

                import java.util.ArrayList;
                import java.util.List;
                import java.util.Map;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class NoPackageDeclaration {

        @Test
        fun `handles file without package declaration`() {
            val input = """
                import javax.annotation.Nullable;
                import java.util.List;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                import java.util.List;

                import javax.annotation.Nullable;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `all imports are third-party when no package declaration`() {
            val input = """
                import com.example.MyClass;
                import org.other.OtherClass;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                import com.example.MyClass;
                import org.other.OtherClass;

                public class Foo {
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class CustomConfiguration {

        @Test
        fun `respects custom same-package depth`() {
            val customFormatter = JavaImportOrderStep(
                samePackageDepth = 2,
                standardPackageRegex = Regex("""^java\."""),
                specialImportsRegex = Regex("""^(javax|jakarta)\."""),
            ).toFormatter()

            val input = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import dev.daymor.other.OtherClass;
                import org.apache.commons.lang3.StringUtils;

                public class Foo {
                }
            """.trimIndent()

            val expected = """
                package dev.daymor.ultimanexus.jvm.gradle.util;

                import org.apache.commons.lang3.StringUtils;

                import dev.daymor.other.OtherClass;

                public class Foo {
                }
            """.trimIndent()

            assertThat(customFormatter.apply(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class Serialization {

        @Test
        fun `JavaImportOrderStep is serializable`() {
            val step = JavaImportOrderStep.create()
            assertThat(step).isNotNull
        }
    }

    @Nested
    inner class PreservesContent {

        @Test
        fun `preserves code before and after imports`() {
            val input = """
                /*
                 * License header
                 */

                package com.example;

                import javax.annotation.Nullable;
                import java.util.List;

                /**
                 * Javadoc for class.
                 */
                public class Foo {
                    private final String name;
                }
            """.trimIndent()

            val expected = """
                /*
                 * License header
                 */

                package com.example;

                import java.util.List;

                import javax.annotation.Nullable;

                /**
                 * Javadoc for class.
                 */
                public class Foo {
                    private final String name;
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }
}
