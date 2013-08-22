package com.mediasmiths.std;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.mediasmiths.std.config.ConfigProviderTests;
import com.mediasmiths.std.threading.SettableFutureTest;

// specify a runner class: Suite.class
@RunWith(Suite.class)
// specify an array of test classes
@Suite.SuiteClasses({ SettableFutureTest.class, ConfigProviderTests.class })
public class MediasmithsStdTestSuite {

}
