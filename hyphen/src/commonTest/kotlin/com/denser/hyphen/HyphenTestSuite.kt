package com.denser.hyphen

import org.junit.runner.RunWith
import org.junit.runners.Suite

import com.denser.hyphen.markdown.MarkdownProcessorTest
import com.denser.hyphen.markdown.MarkdownSerializerTest
import com.denser.hyphen.state.BlockStyleManagerTest
import com.denser.hyphen.state.EditorHistoryManagerTest
import com.denser.hyphen.state.HyphenTextStateTest
import com.denser.hyphen.state.SpanManagerTest
import com.denser.hyphen.ui.EditorExtensionsTest

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MarkdownProcessorTest::class,
    MarkdownSerializerTest::class,
    EditorHistoryManagerTest::class,
    HyphenTextStateTest::class,
    SpanManagerTest::class,
    BlockStyleManagerTest::class,
    EditorExtensionsTest::class
)
class HyphenTestSuite {}