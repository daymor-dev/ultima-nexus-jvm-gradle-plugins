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

object PmdRulesetMerger {

    private const val ROOT_OPEN = "<ruleset"

    private const val ROOT_CLOSE = "</ruleset>"

    private const val EMPTY_BASE = """<?xml version="1.0"?>
<ruleset name="Generated"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
</ruleset>"""

    private val ruleRefPattern = Regex("""<rule\s+ref="([^"]+)">""")

    private val xpathValuePattern = Regex(
        """<property\s+name="violationSuppressXPath"\s*\r?\n?\s*value="([^"]+)"\s*/>"""
    )

    fun merge(baseXml: String?, fragments: List<String>): String {
        val base = baseXml?.takeIf { it.isNotBlank() } ?: EMPTY_BASE
        val baseOpenEnd = findBodyStart(base)
        val baseCloseStart = base.lastIndexOf(ROOT_CLOSE)
        if (baseOpenEnd <= 0 || baseCloseStart < 0) {
            return base
        }
        val head = base.substring(0, baseOpenEnd)
        val body = base.substring(baseOpenEnd, baseCloseStart)
        val tail = base.substring(baseCloseStart)

        val bodiesByRule = linkedMapOf<String, MutableSet<String>>()
        val extrasWithoutRuleRef = StringBuilder()
        for (fragment in fragments) {
            extractFragmentBody(fragment)?.let { fragmentBody ->
                splitIntoElements(fragmentBody).forEach { element ->
                    mergeElement(element, bodiesByRule, extrasWithoutRuleRef)
                }
            }
        }

        val merged = buildString {
            for ((rule, xpaths) in bodiesByRule) {
                append("\n    <rule ref=\"").append(rule).append("\">\n")
                append("        <properties>\n")
                append("            <property name=\"violationSuppressXPath\"\n")
                append("                      value=\"").append(xpaths.joinToString(" or ")).append("\"/>\n")
                append("        </properties>\n")
                append("    </rule>\n")
            }
            append(extrasWithoutRuleRef)
        }

        return buildString(head.length + body.length + merged.length + tail.length) {
            append(head)
            append(body)
            append(merged)
            append(tail)
        }
    }

    private fun findBodyStart(xml: String): Int {
        val openStart = xml.indexOf(ROOT_OPEN)
        if (openStart < 0) {
            return -1
        }
        val openEnd = xml.indexOf('>', openStart)
        if (openEnd < 0) {
            return -1
        }
        return openEnd + 1
    }

    private fun extractFragmentBody(fragment: String): String? {
        val openEnd = findBodyStart(fragment)
        val closeStart = fragment.lastIndexOf(ROOT_CLOSE)
        if (openEnd <= 0 || closeStart <= openEnd) {
            return null
        }
        return fragment.substring(openEnd, closeStart)
    }

    private fun splitIntoElements(body: String): List<String> {
        val results = mutableListOf<String>()
        var searchFrom = 0
        while (searchFrom < body.length) {
            val ruleStart = body.indexOf("<rule ", searchFrom)
            if (ruleStart < 0) {
                break
            }
            val ruleEnd = body.indexOf("</rule>", ruleStart)
            if (ruleEnd < 0) {
                break
            }
            val endInclusive = ruleEnd + "</rule>".length
            results += body.substring(ruleStart, endInclusive)
            searchFrom = endInclusive
        }
        return results
    }

    private fun mergeElement(
        element: String,
        bodiesByRule: MutableMap<String, MutableSet<String>>,
        extrasWithoutRuleRef: StringBuilder
    ) {
        val ruleRefMatch = ruleRefPattern.find(element)
        if (ruleRefMatch == null) {
            extrasWithoutRuleRef.append(element).append('\n')
            return
        }
        val ruleRef = ruleRefMatch.groupValues[1]
        val xpathMatch = xpathValuePattern.find(element)
        if (xpathMatch == null) {
            extrasWithoutRuleRef.append(element).append('\n')
            return
        }
        val combined = xpathMatch.groupValues[1]
        val bucket = bodiesByRule.getOrPut(ruleRef) { linkedSetOf() }
        combined.split(" or ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { bucket.add(it) }
    }
}
