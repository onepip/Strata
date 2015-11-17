/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve.definition;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.light.LightMetaBean;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.market.MarketData;
import com.opengamma.strata.basics.market.ObservableKey;
import com.opengamma.strata.market.curve.DatedCurveParameterMetadata;
import com.opengamma.strata.market.curve.SimpleCurveNodeMetadata;
import com.opengamma.strata.market.value.ValueType;

/**
 * Dummy curve node.
 * Based on a FRA.
 */
@BeanDefinition(style = "light")
public final class DummyFraCurveNode
    implements CurveNode, ImmutableBean, Serializable {

  @PropertyDefinition(validate = "notNull")
  private final Period periodToStart;
  @PropertyDefinition(validate = "notNull")
  private final Period periodToEnd;
  @PropertyDefinition(validate = "notNull")
  private final ObservableKey rateKey;
  @PropertyDefinition
  private final double spread;

  //-------------------------------------------------------------------------
  public static DummyFraCurveNode of(Period periodToStart, IborIndex index, ObservableKey rateKey) {
    return new DummyFraCurveNode(periodToStart, periodToStart.plus(index.getTenor().getPeriod()), rateKey, 0);
  }

  public static DummyFraCurveNode of(Period periodToStart, IborIndex index, ObservableKey rateKey, double spread) {
    return new DummyFraCurveNode(periodToStart, periodToStart.plus(index.getTenor().getPeriod()), rateKey, spread);
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<ObservableKey> requirements() {
    return ImmutableSet.of(rateKey);
  }

  @Override
  public DatedCurveParameterMetadata metadata(LocalDate valuationDate) {
    return SimpleCurveNodeMetadata.of(
        HolidayCalendars.SAT_SUN.nextOrSame(valuationDate.plus(periodToEnd)), periodToEnd.toString());
  }

  @Override
  public DummyFraTrade trade(LocalDate valuationDate, MarketData marketData) {
    double fixedRate = marketData.getValue(rateKey) + spread;
    return DummyFraTrade.of(valuationDate, fixedRate);
  }

  @Override
  public double initialGuess(LocalDate valuationDate, MarketData marketData, ValueType valueType) {
    if (ValueType.ZERO_RATE.equals(valueType)) {
      return marketData.getValue(rateKey);
    }
    return 0d;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DummyFraCurveNode}.
   */
  private static MetaBean META_BEAN = LightMetaBean.of(DummyFraCurveNode.class);

  /**
   * The meta-bean for {@code DummyFraCurveNode}.
   * @return the meta-bean, not null
   */
  public static MetaBean meta() {
    return META_BEAN;
  }

  static {
    JodaBeanUtils.registerMetaBean(META_BEAN);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private DummyFraCurveNode(
      Period periodToStart,
      Period periodToEnd,
      ObservableKey rateKey,
      double spread) {
    JodaBeanUtils.notNull(periodToStart, "periodToStart");
    JodaBeanUtils.notNull(periodToEnd, "periodToEnd");
    JodaBeanUtils.notNull(rateKey, "rateKey");
    this.periodToStart = periodToStart;
    this.periodToEnd = periodToEnd;
    this.rateKey = rateKey;
    this.spread = spread;
  }

  @Override
  public MetaBean metaBean() {
    return META_BEAN;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the periodToStart.
   * @return the value of the property, not null
   */
  public Period getPeriodToStart() {
    return periodToStart;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the periodToEnd.
   * @return the value of the property, not null
   */
  public Period getPeriodToEnd() {
    return periodToEnd;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rateKey.
   * @return the value of the property, not null
   */
  public ObservableKey getRateKey() {
    return rateKey;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the spread.
   * @return the value of the property
   */
  public double getSpread() {
    return spread;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DummyFraCurveNode other = (DummyFraCurveNode) obj;
      return JodaBeanUtils.equal(periodToStart, other.periodToStart) &&
          JodaBeanUtils.equal(periodToEnd, other.periodToEnd) &&
          JodaBeanUtils.equal(rateKey, other.rateKey) &&
          JodaBeanUtils.equal(spread, other.spread);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(periodToStart);
    hash = hash * 31 + JodaBeanUtils.hashCode(periodToEnd);
    hash = hash * 31 + JodaBeanUtils.hashCode(rateKey);
    hash = hash * 31 + JodaBeanUtils.hashCode(spread);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("DummyFraCurveNode{");
    buf.append("periodToStart").append('=').append(periodToStart).append(',').append(' ');
    buf.append("periodToEnd").append('=').append(periodToEnd).append(',').append(' ');
    buf.append("rateKey").append('=').append(rateKey).append(',').append(' ');
    buf.append("spread").append('=').append(JodaBeanUtils.toString(spread));
    buf.append('}');
    return buf.toString();
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
