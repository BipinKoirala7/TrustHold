package com.trusthold.mail.templates;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailTemplateRenderer {
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");
  private final Map<String, String> cache = new ConcurrentHashMap<>();

  /**
   * Loads templates/email/{templateName}.html and replaces every {{key}} with the
   * HTML-escaped value from the map. Throws if a placeholder has no value.
   */
  public String render(String templateName, Map<String, String> values) {
    String template = cache.computeIfAbsent(templateName, this::load);

    Matcher matcher = PLACEHOLDER.matcher(template);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String value = values.get(key);
      if (value == null) {
        throw new IllegalArgumentException("Missing value for email placeholder: " + key);
      }
      matcher.appendReplacement(result, Matcher.quoteReplacement(HtmlUtils.htmlEscape(value)));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private String load(String templateName) {
    try (InputStream in = new ClassPathResource("templates/email/" + templateName + ".html").getInputStream()) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not load email template: " + templateName, e);
    }
  }
}
