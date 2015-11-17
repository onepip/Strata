/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve.definition;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.BuySell;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.market.MarketData;
import com.opengamma.strata.basics.market.ObservableKey;
import com.opengamma.strata.market.curve.DatedCurveParameterMetadata;
import com.opengamma.strata.market.curve.TenorCurveNodeMetadata;
import com.opengamma.strata.market.value.ValueType;
import com.opengamma.strata.product.fx.FxSwapTrade;
import com.opengamma.strata.product.fx.type.FxSwapTemplate;

/**
 * A curve node whose instrument is an FX Swap.
 */
@BeanDefinition
public final class FxSwapCurveNode
    implements CurveNode, ImmutableBean, Serializable {

  /**
   * The template for the FX Swap associated with this node.
   */
  @PropertyDefinition(validate = "notNull")
  private final FxSwapTemplate template;
  /**
   * The key identifying the market data value which provides the FX near (spot) date rate.
   */
  @PropertyDefinition(validate = "notNull")
  private final ObservableKey fxNearKey;
  /**
   * The key identifying the market data value which provides the FX forward points.
   */
  @PropertyDefinition(validate = "notNull")
  private final ObservableKey fxPtsKey;

  //-------------------------------------------------------------------------
  /**
   * Returns a curve node for an FX Swap using the specified instrument template and keys.
   *
   * @param template  the template used for building the instrument for the node
   * @param fxNearKey  the key identifying the FX rate for the near date used when building the instrument for the node
   * @param fxPtsKey  the key identifying the FX points between the near date and the far date
   * @return a node whose instrument is built from the template using a market rate
   */
  public static FxSwapCurveNode of(FxSwapTemplate template, ObservableKey fxNearKey, ObservableKey fxPtsKey) {
    return FxSwapCurveNode.builder()
        .template(template)
        .fxNearKey(fxNearKey)
        .fxPtsKey(fxPtsKey)
        .build();
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<ObservableKey> requirements() {
    return ImmutableSet.of(fxNearKey, fxPtsKey);
  }

  @Override
  public DatedCurveParameterMetadata metadata(LocalDate valuationDate) {
    FxSwapTrade trade = template.toTrade(valuationDate, BuySell.BUY, 1, 1, 0);
    LocalDate farDate = trade.getProduct().getFarLeg().getPaymentDate();
    return TenorCurveNodeMetadata.of(farDate, Tenor.of(template.getPeriodToFar()));
  }

  @Override
  public FxSwapTrade trade(LocalDate valuationDate, MarketData marketData) {
    double fxNearRate = marketData.getValue(fxNearKey);
    double fxPts = marketData.getValue(fxPtsKey);
    return template.toTrade(valuationDate, BuySell.BUY, 1d, fxNearRate, fxPts);
  }

  @Override
  public double initialGuess(LocalDate valuationDate, MarketData marketData, ValueType valueType) {
    if (ValueType.DISCOUNT_FACTOR.equals(valueType)) {
      return 1d;
    }
    return 0d;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxSwapCurveNode}.
   * @return the meta-bean, not null
   */
  public static FxSwapCurveNode.Meta meta() {
    return FxSwapCurveNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxSwapCurveNode.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FxSwapCurveNode.Builder builder() {
    return new FxSwapCurveNode.Builder();
  }

  private FxSwapCurveNode(
      FxSwapTemplate template,
      ObservableKey fxNearKey,
      ObservableKey fxPtsKey) {
    JodaBeanUtils.notNull(template, "template");
    JodaBeanUtils.notNull(fxNearKey, "fxNearKey");
    JodaBeanUtils.notNull(fxPtsKey, "fxPtsKey");
    this.template = template;
    this.fxNearKey = fxNearKey;
    this.fxPtsKey = fxPtsKey;
  }

  @Override
  public FxSwapCurveNode.Meta metaBean() {
    return FxSwapCurveNode.Meta.INSTANCE;
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
   * Gets the template for the FX Swap associated with this node.
   * @return the value of the property, not null
   */
  public FxSwapTemplate getTemplate() {
    return template;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the key identifying the market data value which provides the FX near (spot) date rate.
   * @return the value of the property, not null
   */
  public ObservableKey getFxNearKey() {
    return fxNearKey;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the key identifying the market data value which provides the FX forward points.
   * @return the value of the property, not null
   */
  public ObservableKey getFxPtsKey() {
    return fxPtsKey;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxSwapCurveNode other = (FxSwapCurveNode) obj;
      return JodaBeanUtils.equal(template, other.template) &&
          JodaBeanUtils.equal(fxNearKey, other.fxNearKey) &&
          JodaBeanUtils.equal(fxPtsKey, other.fxPtsKey);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(template);
    hash = hash * 31 + JodaBeanUtils.hashCode(fxNearKey);
    hash = hash * 31 + JodaBeanUtils.hashCode(fxPtsKey);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FxSwapCurveNode{");
    buf.append("template").append('=').append(template).append(',').append(' ');
    buf.append("fxNearKey").append('=').append(fxNearKey).append(',').append(' ');
    buf.append("fxPtsKey").append('=').append(JodaBeanUtils.toString(fxPtsKey));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxSwapCurveNode}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code template} property.
     */
    private final MetaProperty<FxSwapTemplate> template = DirectMetaProperty.ofImmutable(
        this, "template", FxSwapCurveNode.class, FxSwapTemplate.class);
    /**
     * The meta-property for the {@code fxNearKey} property.
     */
    private final MetaProperty<ObservableKey> fxNearKey = DirectMetaProperty.ofImmutable(
        this, "fxNearKey", FxSwapCurveNode.class, ObservableKey.class);
    /**
     * The meta-property for the {@code fxPtsKey} property.
     */
    private final MetaProperty<ObservableKey> fxPtsKey = DirectMetaProperty.ofImmutable(
        this, "fxPtsKey", FxSwapCurveNode.class, ObservableKey.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "template",
        "fxNearKey",
        "fxPtsKey");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1321546630:  // template
          return template;
        case -1797478427:  // fxNearKey
          return fxNearKey;
        case -1094751134:  // fxPtsKey
          return fxPtsKey;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FxSwapCurveNode.Builder builder() {
      return new FxSwapCurveNode.Builder();
    }

    @Override
    public Class<? extends FxSwapCurveNode> beanType() {
      return FxSwapCurveNode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code template} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FxSwapTemplate> template() {
      return template;
    }

    /**
     * The meta-property for the {@code fxNearKey} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ObservableKey> fxNearKey() {
      return fxNearKey;
    }

    /**
     * The meta-property for the {@code fxPtsKey} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ObservableKey> fxPtsKey() {
      return fxPtsKey;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1321546630:  // template
          return ((FxSwapCurveNode) bean).getTemplate();
        case -1797478427:  // fxNearKey
          return ((FxSwapCurveNode) bean).getFxNearKey();
        case -1094751134:  // fxPtsKey
          return ((FxSwapCurveNode) bean).getFxPtsKey();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FxSwapCurveNode}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<FxSwapCurveNode> {

    private FxSwapTemplate template;
    private ObservableKey fxNearKey;
    private ObservableKey fxPtsKey;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FxSwapCurveNode beanToCopy) {
      this.template = beanToCopy.getTemplate();
      this.fxNearKey = beanToCopy.getFxNearKey();
      this.fxPtsKey = beanToCopy.getFxPtsKey();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1321546630:  // template
          return template;
        case -1797478427:  // fxNearKey
          return fxNearKey;
        case -1094751134:  // fxPtsKey
          return fxPtsKey;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1321546630:  // template
          this.template = (FxSwapTemplate) newValue;
          break;
        case -1797478427:  // fxNearKey
          this.fxNearKey = (ObservableKey) newValue;
          break;
        case -1094751134:  // fxPtsKey
          this.fxPtsKey = (ObservableKey) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FxSwapCurveNode build() {
      return new FxSwapCurveNode(
          template,
          fxNearKey,
          fxPtsKey);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the template for the FX Swap associated with this node.
     * @param template  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder template(FxSwapTemplate template) {
      JodaBeanUtils.notNull(template, "template");
      this.template = template;
      return this;
    }

    /**
     * Sets the key identifying the market data value which provides the FX near (spot) date rate.
     * @param fxNearKey  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fxNearKey(ObservableKey fxNearKey) {
      JodaBeanUtils.notNull(fxNearKey, "fxNearKey");
      this.fxNearKey = fxNearKey;
      return this;
    }

    /**
     * Sets the key identifying the market data value which provides the FX forward points.
     * @param fxPtsKey  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fxPtsKey(ObservableKey fxPtsKey) {
      JodaBeanUtils.notNull(fxPtsKey, "fxPtsKey");
      this.fxPtsKey = fxPtsKey;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("FxSwapCurveNode.Builder{");
      buf.append("template").append('=').append(JodaBeanUtils.toString(template)).append(',').append(' ');
      buf.append("fxNearKey").append('=').append(JodaBeanUtils.toString(fxNearKey)).append(',').append(' ');
      buf.append("fxPtsKey").append('=').append(JodaBeanUtils.toString(fxPtsKey));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
