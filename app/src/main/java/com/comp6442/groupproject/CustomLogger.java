package com.comp6442.groupproject;

import timber.log.Timber;

/**
 * Extension of Timber DebugTree to include method name and line number of invocation.
 */
public class CustomLogger extends Timber.DebugTree {
  @Override
  protected String createStackElementTag(StackTraceElement element) {
    return String.format("%s.%s:%s",
            super.createStackElementTag(element),
            element.getMethodName(),
            element.getLineNumber());
  }
}