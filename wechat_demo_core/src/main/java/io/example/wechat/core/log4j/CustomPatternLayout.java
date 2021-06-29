package io.example.wechat.core.log4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bruce.wan on 2019/9/29.
 */
@Plugin(name = "CustomPatternLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class CustomPatternLayout extends AbstractStringLayout {
    public static final String DEFAULT_CONVERSION_PATTERN = "%m%n";
    public static final String TTCC_CONVERSION_PATTERN = "%r [%t] %p %c %notEmpty{%x }- %m%n";
    public static final String SIMPLE_CONVERSION_PATTERN = "%d [%t] %p %c - %m%n";
    public static final String KEY = "Converter";
    private final String conversionPattern;
    private final PatternSelector patternSelector;
    private final Serializer eventSerializer;

    private CustomPatternLayout(Configuration config, CustomRegexReplaces replace, String eventPattern, PatternSelector patternSelector, Charset charset, boolean alwaysWriteExceptions, boolean disableAnsi, boolean noConsoleNoAnsi, String headerPattern, String footerPattern) {
        super(config, charset, newSerializerBuilder().setConfiguration(config).setReplace(replace).setPatternSelector(patternSelector).setAlwaysWriteExceptions(alwaysWriteExceptions).setDisableAnsi(disableAnsi).setNoConsoleNoAnsi(noConsoleNoAnsi).setPattern(headerPattern).build(), newSerializerBuilder().setConfiguration(config).setReplace(replace).setPatternSelector(patternSelector).setAlwaysWriteExceptions(alwaysWriteExceptions).setDisableAnsi(disableAnsi).setNoConsoleNoAnsi(noConsoleNoAnsi).setPattern(footerPattern).build());
        this.conversionPattern = eventPattern;
        this.patternSelector = patternSelector;
        this.eventSerializer = newSerializerBuilder().setConfiguration(config).setReplace(replace).setPatternSelector(patternSelector).setAlwaysWriteExceptions(alwaysWriteExceptions).setDisableAnsi(disableAnsi).setNoConsoleNoAnsi(noConsoleNoAnsi).setPattern(eventPattern).setDefaultPattern("%m%n").build();
    }

    public static SerializerBuilder newSerializerBuilder() {
        return new SerializerBuilder();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static Serializer createSerializer(Configuration configuration, CustomRegexReplaces replace, String pattern, String defaultPattern, PatternSelector patternSelector, boolean alwaysWriteExceptions, boolean noConsoleNoAnsi) {
        SerializerBuilder builder = newSerializerBuilder();
        builder.setAlwaysWriteExceptions(alwaysWriteExceptions);
        builder.setConfiguration(configuration);
        builder.setDefaultPattern(defaultPattern);
        builder.setNoConsoleNoAnsi(noConsoleNoAnsi);
        builder.setPattern(pattern);
        builder.setPatternSelector(patternSelector);
        builder.setReplace(replace);
        return builder.build();
    }

    public static PatternParser createPatternParser(Configuration config) {
        if (config == null) {
            return new PatternParser(config, "Converter", LogEventPatternConverter.class);
        } else {
            PatternParser parser = (PatternParser) config.getComponent("Converter");
            if (parser == null) {
                parser = new PatternParser(config, "Converter", LogEventPatternConverter.class);
                config.addComponent("Converter", parser);
                parser = (PatternParser) config.getComponent("Converter");
            }

            return parser;
        }
    }

    /**
     * @deprecated
     */
    @PluginFactory
    @Deprecated
    public static CustomPatternLayout createLayout(@PluginAttribute(value = "pattern", defaultString = "%m%n") String pattern, @PluginElement("PatternSelector") PatternSelector patternSelector, @PluginConfiguration Configuration config, @PluginElement("Replace") CustomRegexReplaces replace, @PluginAttribute("charset") Charset charset, @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) boolean alwaysWriteExceptions, @PluginAttribute("noConsoleNoAnsi") boolean noConsoleNoAnsi, @PluginAttribute("header") String headerPattern, @PluginAttribute("footer") String footerPattern) {
        return newBuilder().withPattern(pattern).withPatternSelector(patternSelector).withConfiguration(config).withRegexReplacement(replace).withCharset(charset).withAlwaysWriteExceptions(alwaysWriteExceptions).withNoConsoleNoAnsi(noConsoleNoAnsi).withHeader(headerPattern).withFooter(footerPattern).build();
    }

    public static CustomPatternLayout createDefaultLayout() {
        return newBuilder().build();
    }

    public static CustomPatternLayout createDefaultLayout(Configuration configuration) {
        return newBuilder().withConfiguration(configuration).build();
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private static String maskData(String source) {
        source = source.replaceAll("\\\\", "");
        source = maskDocumentURI(source);
        source = maskNickName(source);
        source = maskContentUrl(source);
        source = maskAccessToken(source);
        return source;
    }

    private static String maskAccessToken(String message) {
        Pattern pattern = Pattern.compile("access_token=(?<accessToken>[^&\r\n]*)");
        Matcher matcher = pattern.matcher(message);
        for (boolean find = matcher.find(); find; find = matcher.find()) {
            String accessToken = matcher.group("accessToken");
            if (StringUtils.isNotBlank(accessToken)) {
                message = message.replace(accessToken, "*****");
            }
        }
        return message;
    }

    private static String maskDocumentURI(String message) {
        Pattern pattern = Pattern.compile("\\<ns1\\:DocumentURI.*\\>.*\\<\\/ns1\\:DocumentURI\\>");
        Matcher matcher = pattern.matcher(message);

        for (boolean find = matcher.find(); find; find = matcher.find()) {
            String hostRegex = "[https|http]\\:\\/\\/[^\\/]*\\/";
            String str = matcher.group();
            String[] subStr = str.split(hostRegex);
            if (subStr.length >= 2) {
                Matcher hostMatcher = Pattern.compile(hostRegex).matcher(str);
                hostMatcher.find();
                String host = hostMatcher.group();
                String maskStr = subStr[0] + host + "*****" + str.substring(str.lastIndexOf("<"), str.length());
                message = message.replace(str, maskStr);
            }
        }
        return message;
    }

    private static String maskContentUrl(String message) {
        Pattern pattern = Pattern.compile("content_url[\"]\\:\"http[^\"]*\"");
        Matcher matcher = pattern.matcher(message);
        for (boolean find = matcher.find(); find; find = matcher.find()) {
            String hostRegex = "[https|http]\\:\\/\\/[^\\/]*\\/";
            String str = matcher.group();
            String[] subStr = str.split(hostRegex);
            if (subStr.length >= 2) {
                Matcher ipMatcher = Pattern.compile(hostRegex).matcher(str);
                ipMatcher.find();
                String ip = ipMatcher.group();
                String maskStr = subStr[0] + ip + "*****" + str.substring(str.lastIndexOf("\""));
                message = message.replace(str, maskStr);
            }
        }
        return message;
    }

    private static String maskNickName(String message) {
        Pattern pattern = Pattern.compile("nickName[\"]\\:.*\\,");
        Matcher matcher = pattern.matcher(message);

        for (boolean find = matcher.find(); find; find = matcher.find()) {
            String str = matcher.group();
            String maskStr = str.replaceAll("\\:.*,", ":****,");
            message = message.replace(str, maskStr);
        }
        return message;
    }

    public String getConversionPattern() {
        return this.conversionPattern;
    }

    public Map<String, String> getContentFormat() {
        Map<String, String> result = new HashMap<>();
        result.put("structured", "false");
        result.put("formatType", "conversion");
        result.put("format", this.conversionPattern);
        return result;
    }

    public String toSerializable(LogEvent event) {
        return this.eventSerializer.toSerializable(event);
    }

    public void encode(LogEvent event, ByteBufferDestination destination) {
        if (!(this.eventSerializer instanceof Serializer2)) {
            super.encode(event, destination);
        } else {
            StringBuilder text = this.toText((Serializer2) this.eventSerializer, event, getStringBuilder());
            Encoder<StringBuilder> encoder = this.getStringBuilderEncoder();
            encoder.encode(text, destination);
            trimToMaxSize(text);
        }
    }

    private StringBuilder toText(Serializer2 serializer, LogEvent event, StringBuilder destination) {
        return serializer.toSerializable(event, destination);
    }

    public boolean requiresLocation() {
        return true;
    }

    public String toString() {
        return this.patternSelector == null ? this.conversionPattern : this.patternSelector.toString();
    }

    public Serializer getEventSerializer() {
        return this.eventSerializer;
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<CustomPatternLayout> {
        @PluginBuilderAttribute
        private String pattern;
        @PluginElement("PatternSelector")
        private PatternSelector patternSelector;
        @PluginConfiguration
        private Configuration configuration;
        @PluginElement("Replace")
        private CustomRegexReplaces regexReplacement;
        @PluginBuilderAttribute
        private Charset charset;
        @PluginBuilderAttribute
        private boolean alwaysWriteExceptions;
        @PluginBuilderAttribute
        private boolean disableAnsi;
        @PluginBuilderAttribute
        private boolean noConsoleNoAnsi;
        @PluginBuilderAttribute
        private String header;
        @PluginBuilderAttribute
        private String footer;

        private Builder() {
            this.pattern = "%m%n";
            this.charset = Charset.defaultCharset();
            this.alwaysWriteExceptions = true;
            this.disableAnsi = !this.useAnsiEscapeCodes();
        }

        private boolean useAnsiEscapeCodes() {
            PropertiesUtil propertiesUtil = PropertiesUtil.getProperties();
            boolean isPlatformSupportsAnsi = !propertiesUtil.isOsWindows();
            boolean isJansiRequested = !propertiesUtil.getBooleanProperty("log4j.skipJansi", true);
            return isPlatformSupportsAnsi || isJansiRequested;
        }

        public Builder withPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withPatternSelector(PatternSelector patternSelector) {
            this.patternSelector = patternSelector;
            return this;
        }

        public Builder withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder withRegexReplacement(CustomRegexReplaces regexReplacement) {
            this.regexReplacement = regexReplacement;
            return this;
        }

        public Builder withCharset(Charset charset) {
            if (charset != null) {
                this.charset = charset;
            }

            return this;
        }

        public Builder withAlwaysWriteExceptions(boolean alwaysWriteExceptions) {
            this.alwaysWriteExceptions = alwaysWriteExceptions;
            return this;
        }

        public Builder withDisableAnsi(boolean disableAnsi) {
            this.disableAnsi = disableAnsi;
            return this;
        }

        public Builder withNoConsoleNoAnsi(boolean noConsoleNoAnsi) {
            this.noConsoleNoAnsi = noConsoleNoAnsi;
            return this;
        }

        public Builder withHeader(String header) {
            this.header = header;
            return this;
        }

        public Builder withFooter(String footer) {
            this.footer = footer;
            return this;
        }

        public CustomPatternLayout build() {
            if (this.configuration == null) {
                this.configuration = new DefaultConfiguration();
            }

            return new CustomPatternLayout(this.configuration, this.regexReplacement, this.pattern, this.patternSelector, this.charset, this.alwaysWriteExceptions, this.disableAnsi, this.noConsoleNoAnsi, this.header, this.footer);
        }
    }

    private static class PatternSelectorSerializer implements Serializer, Serializer2 {
        private final PatternSelector patternSelector;
        private final CustomRegexReplaces replace;

        private PatternSelectorSerializer(PatternSelector patternSelector, CustomRegexReplaces replace) {
            this.patternSelector = patternSelector;
            this.replace = replace;
        }

        public String toSerializable(LogEvent event) {
            StringBuilder sb = AbstractStringLayout.getStringBuilder();

            String var3;
            try {
                var3 = this.toSerializable(event, sb).toString();
            } finally {
                AbstractStringLayout.trimToMaxSize(sb);
            }

            return var3;
        }

        public StringBuilder toSerializable(LogEvent event, StringBuilder buffer) {
            PatternFormatter[] formatters = this.patternSelector.getFormatters(event);
            int len = formatters.length;

            for (int i = 0; i < len; ++i) {
                formatters[i].format(event, buffer);
            }

            if (this.replace != null) {
                String str = buffer.toString();
                str = this.replace.format(str);
                buffer.setLength(0);
                buffer.append(str);
            }

            return buffer;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append("[patternSelector=");
            builder.append(this.patternSelector);
            builder.append(", replace=");
            builder.append(this.replace);
            builder.append("]");
            return builder.toString();
        }
    }

    public static class SerializerBuilder implements org.apache.logging.log4j.core.util.Builder<Serializer> {
        private Configuration configuration;
        private CustomRegexReplaces replace;
        private String pattern;
        private String defaultPattern;
        private PatternSelector patternSelector;
        private boolean alwaysWriteExceptions;
        private boolean disableAnsi;
        private boolean noConsoleNoAnsi;

        public SerializerBuilder() {
        }

        public Serializer build() {
            if (Strings.isEmpty(this.pattern) && Strings.isEmpty(this.defaultPattern)) {
                return null;
            } else if (this.patternSelector == null) {
                try {
                    PatternParser parser = CustomPatternLayout.createPatternParser(this.configuration);
                    List<PatternFormatter>
                            list = parser.parse(this.pattern == null ? this.defaultPattern : this.pattern, this.alwaysWriteExceptions, this.disableAnsi, this.noConsoleNoAnsi);
                    PatternFormatter[] formatters = (PatternFormatter[]) list.toArray(new PatternFormatter[0]);
                    return new PatternSerializer(formatters, this.replace);
                } catch (RuntimeException var4) {
                    throw new IllegalArgumentException("Cannot parse pattern '" + this.pattern + "'", var4);
                }
            } else {
                return new PatternSelectorSerializer(this.patternSelector, this.replace);
            }
        }

        public SerializerBuilder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public SerializerBuilder setReplace(CustomRegexReplaces replace) {
            this.replace = replace;
            return this;
        }

        public SerializerBuilder setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public SerializerBuilder setDefaultPattern(String defaultPattern) {
            this.defaultPattern = defaultPattern;
            return this;
        }

        public SerializerBuilder setPatternSelector(PatternSelector patternSelector) {
            this.patternSelector = patternSelector;
            return this;
        }

        public SerializerBuilder setAlwaysWriteExceptions(boolean alwaysWriteExceptions) {
            this.alwaysWriteExceptions = alwaysWriteExceptions;
            return this;
        }

        public SerializerBuilder setDisableAnsi(boolean disableAnsi) {
            this.disableAnsi = disableAnsi;
            return this;
        }

        public SerializerBuilder setNoConsoleNoAnsi(boolean noConsoleNoAnsi) {
            this.noConsoleNoAnsi = noConsoleNoAnsi;
            return this;
        }
    }

    private static class PatternSerializer implements Serializer, Serializer2 {
        private final PatternFormatter[] formatters;
        private final CustomRegexReplaces replace;

        private PatternSerializer(PatternFormatter[] formatters, CustomRegexReplaces replace) {
            this.formatters = formatters;
            this.replace = replace;
        }

        public String toSerializable(LogEvent event) {
            StringBuilder sb = AbstractStringLayout.getStringBuilder();

            String var3;
            try {
                var3 = this.toSerializable(event, sb).toString();
            } finally {
                AbstractStringLayout.trimToMaxSize(sb);
            }

            return var3;
        }

        public StringBuilder toSerializable(LogEvent event, StringBuilder buffer) {
            int len = this.formatters.length;

            for (int i = 0; i < len; ++i) {
                this.formatters[i].format(event, buffer);
            }

            // 对数据进行脱敏处理
            String strCustomize = maskData(buffer.toString());
            buffer.setLength(0);
            buffer.append(strCustomize);

            if (this.replace != null) {
                String str = buffer.toString();
                str = this.replace.format(str);
                buffer.setLength(0);
                buffer.append(str);
            }

            return buffer;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append("[formatters=");
            builder.append(Arrays.toString(this.formatters));
            builder.append(", replace=");
            builder.append(this.replace);
            builder.append("]");
            return builder.toString();
        }
    }
}
