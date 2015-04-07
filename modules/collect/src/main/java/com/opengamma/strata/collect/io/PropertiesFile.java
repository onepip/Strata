/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.collect.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.io.CharSource;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A properties file.
 * <p>
 * Represents a properties file together with the ability to parse it from a {@link CharSource}.
 * This is similar to {@link Properties} but with a simpler format and without extending {@link Map}.
 * Duplicate keys are allowed and handled.
 * <p>
 * The properties file format used here is deliberately simple.
 * There is only one element - key-value pairs.
 * <p>
 * The key is separated from the value using the '=' symbol.
 * Duplicate keys are allowed.
 * For example 'key = value'.
 * <p>
 * Keys and values are trimmed.
 * Blank lines are ignored.
 * Whole line comments begin with hash '#' or semicolon ';'.
 * No escape format is available.
 * Lookup is case sensitive.
 * <p>
 * This example explains the format:
 * <pre>
 *  # line comment
 *  key = value
 *  month = January
 * </pre>
 * <p>
 * The aim of this class is to parse the basic format.
 * Interpolation of variables is not supported.
 */
public final class PropertiesFile {

  /**
   * The key-value pairs.
   */
  private final PropertySet keyValueMap;

  //-------------------------------------------------------------------------
  /**
   * Parses the specified source as a properties file.
   * <p>
   * This parses the specified character source expecting a properties file format.
   * The resulting instance can be queried for each key and value.
   * 
   * @param source  the properties file resource, not null
   * @return the properties file, not null
   */
  public static PropertiesFile of(CharSource source) {
    ArgChecker.notNull(source, "source");
    try {
      PropertySet keyValues = parse(source);
      return new PropertiesFile(keyValues);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  // parses the properties file format
  private static PropertySet parse(CharSource source) throws IOException {
    ImmutableList<String> lines = source.readLines();
    Multimap<String, String> parsed = ArrayListMultimap.create();
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      int equalsPosition = line.indexOf('=');
      if (equalsPosition < 0) {
        throw new IllegalArgumentException("Invalid properties file, expected key=value property, line " + lineNum);
      }
      String key = line.substring(0, equalsPosition).trim();
      String value = line.substring(equalsPosition + 1).trim();
      if (key.length() == 0) {
        throw new IllegalArgumentException("Invalid properties file, empty key, line " + lineNum);
      }
      parsed.put(key, value);
    }
    return PropertySet.of(parsed);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   * 
   * @param keyValueMap  the values
   */
  private PropertiesFile(PropertySet keyValueMap) {
    this.keyValueMap = keyValueMap;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets all the key-value properties of this file.
   * <p>
   * The map of key-value properties is exposed by this method.
   * 
   * @return the key-value properties
   */
  public PropertySet getProperties() {
    return keyValueMap;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this file equals another.
   * <p>
   * The comparison checks the content.
   * 
   * @param obj  the other section, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PropertiesFile) {
      return keyValueMap.equals(((PropertiesFile) obj).keyValueMap);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the file.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return keyValueMap.hashCode();
  }

  /**
   * Returns a string describing the file.
   * 
   * @return the descriptive string
   */
  @Override
  public String toString() {
    return keyValueMap.toString();
  }

}