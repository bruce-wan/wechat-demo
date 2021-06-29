package io.example.wechat.core.log4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.pattern.RegexReplacement;
import org.apache.logging.log4j.status.StatusLogger;

import java.util.Arrays;

/**
 * Created by bruce.wan on 2019/9/29.
 */
@Plugin(name = "replaces", category = "Core", printObject = true)
public final class CustomRegexReplaces
{
  private static final Logger LOGGER = StatusLogger.getLogger();
  private final RegexReplacement[] regexReplacements;

  private CustomRegexReplaces(RegexReplacement[] regexReplacements) {
    this.regexReplacements = regexReplacements;
  }

  public String format(String msg) {
    for(RegexReplacement replace : regexReplacements)
    {
      msg = replace.format(msg);
    }
    return msg;
  }

  @PluginFactory
  public static CustomRegexReplaces createRegexReplacement(@PluginElement("replaces") final RegexReplacement[] regexReplacements) {
    if (ArrayUtils.isEmpty(regexReplacements)) {
      LOGGER.error("no replaces is defined");
      return null;
    }
    return new CustomRegexReplaces(regexReplacements);
  }

  @Override
  public String toString()
  {
    return "CustomRegexReplaces{" + "regexReplacements=" + Arrays.toString(regexReplacements) + '}';
  }
}
