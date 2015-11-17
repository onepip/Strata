/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.examples.finance;

import static com.opengamma.strata.basics.date.BusinessDayConventions.MODIFIED_FOLLOWING;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.PayReceive;
import com.opengamma.strata.basics.Trade;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.index.IborIndices;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.PeriodicSchedule;
import com.opengamma.strata.calc.CalculationEngine;
import com.opengamma.strata.calc.CalculationRules;
import com.opengamma.strata.calc.Column;
import com.opengamma.strata.calc.config.Measure;
import com.opengamma.strata.calc.marketdata.MarketEnvironment;
import com.opengamma.strata.calc.marketdata.scenario.PerturbationMapping;
import com.opengamma.strata.calc.marketdata.scenario.ScenarioDefinition;
import com.opengamma.strata.calc.runner.Results;
import com.opengamma.strata.calc.runner.function.result.ScenarioResult;
import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.collect.id.StandardId;
import com.opengamma.strata.examples.engine.ExampleEngine;
import com.opengamma.strata.examples.marketdata.ExampleMarketDataBuilder;
import com.opengamma.strata.function.StandardComponents;
import com.opengamma.strata.function.marketdata.curve.CurvePointShifts;
import com.opengamma.strata.function.marketdata.curve.CurvePointShiftsBuilder;
import com.opengamma.strata.function.marketdata.scenario.curve.AnyDiscountCurveFilter;
import com.opengamma.strata.function.marketdata.scenario.curve.CurveRateIndexFilter;
import com.opengamma.strata.market.ShiftType;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.curve.CurveParameterMetadata;
import com.opengamma.strata.market.curve.NodalCurve;
import com.opengamma.strata.market.id.DiscountCurveId;
import com.opengamma.strata.market.id.RateCurveId;
import com.opengamma.strata.market.id.RateIndexCurveId;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.rate.swap.IborRateCalculation;
import com.opengamma.strata.product.rate.swap.NotionalSchedule;
import com.opengamma.strata.product.rate.swap.PaymentSchedule;
import com.opengamma.strata.product.rate.swap.RateCalculationSwapLeg;
import com.opengamma.strata.product.rate.swap.Swap;
import com.opengamma.strata.product.rate.swap.SwapLeg;
import com.opengamma.strata.product.rate.swap.SwapTrade;

/**
 * Example to illustrate using the engine to run a set of historical scenarios on a single swap
 * to produce a P&L series. This P&L series could then be used to calculate historical VaR.
 * <p>
 * In this example we are provided with market data containing:
 * <li>a complete snapshot to value the swap on the valuation date (curves only as the swap is forward-starting)
 * <li>a series of historical curves for every date leading up to the valuation date
 * <p>
 * The differences between the zero rates in consecutive historical curves (dates d-1 and d)
 * are used to generate a scenario, later attributed to date d, containing these relative curve
 * shifts. The swap is then valued on the valuation date, applying each scenario to the base
 * snapshot from the valuation date, to produce a PV series. A P&L series is then generated from
 * this.
 * <p>
 * Instead of generating the perturbations on-the-fly from real data as in this example, the
 * scenario could be pre-generated and stored, or generated in any other way.
 */
public class HistoricalScenarioExample {

  private static final String MARKET_DATA_RESOURCE_ROOT = "example-historicalscenario-marketdata";

  public static void main(String[] args) {
    // the trades for which to calculate a P&L series
    List<Trade> trades = ImmutableList.of(createTrade());

    // the columns, specifying the measures to be calculated
    List<Column> columns = ImmutableList.of(
        Column.of(Measure.PRESENT_VALUE));

    // use the built-in example historical scenario market data
    ExampleMarketDataBuilder marketDataBuilder = ExampleMarketDataBuilder.ofResource(MARKET_DATA_RESOURCE_ROOT);

    // the complete set of rules for calculating measures
    CalculationRules rules = CalculationRules.builder()
        .pricingRules(StandardComponents.pricingRules())
        .marketDataRules(marketDataBuilder.rules())
        .build();

    // load the historical calibrated curves from which we will build our scenarios
    // these curves are provided in the example data environment
    SortedMap<LocalDate, Map<RateCurveId, Curve>> historicalCurves = marketDataBuilder.loadAllRatesCurves();

    // sorted list of dates for the available series of curves
    // the entries in the P&L vector we produce will correspond to these dates
    List<LocalDate> scenarioDates = new ArrayList<>(historicalCurves.keySet());

    // build the historical scenarios
    ScenarioDefinition historicalScenarios = buildHistoricalScenarios(historicalCurves, scenarioDates);

    // build a market data snapshot for the valuation date
    // this is the base snapshot which will be perturbed by the scenarios
    LocalDate valuationDate = LocalDate.of(2015, 4, 23);
    MarketEnvironment snapshot = marketDataBuilder.buildSnapshot(valuationDate);

    // create the engine and calculate the results under each scenario
    CalculationEngine engine = ExampleEngine.create();
    Results results = engine.calculate(trades, columns, rules, snapshot, historicalScenarios);

    // the results contain the one measure requested (Present Value) for each scenario
    ScenarioResult<?> scenarioValuations = (ScenarioResult<?>) results.get(0, 0).getValue();
    outputPnl(scenarioDates, scenarioValuations);
  }

  private static ScenarioDefinition buildHistoricalScenarios(
      Map<LocalDate, Map<RateCurveId, Curve>> historicalCurves,
      List<LocalDate> scenarioDates) {

    // create identifiers for the curves we want the scenarios to affect
    // these are the curves which are required to price the swap, for which we also have historical data
    CurveGroupName curveGroup = CurveGroupName.of("Default");
    DiscountCurveId discountCurveId = DiscountCurveId.of(Currency.USD, curveGroup);
    RateIndexCurveId libor3mCurveId = RateIndexCurveId.of(IborIndices.USD_LIBOR_3M, curveGroup);
    RateIndexCurveId libor6mCurveId = RateIndexCurveId.of(IborIndices.USD_LIBOR_6M, curveGroup);

    // create mappings which will cause the point shift perturbations generated above
    // to be applied to the correct curves
    PerturbationMapping<Curve> discountCurveMappings = PerturbationMapping.of(
        Curve.class,
        AnyDiscountCurveFilter.INSTANCE,
        buildShifts(discountCurveId, historicalCurves, scenarioDates));

    PerturbationMapping<Curve> libor3mMappings = PerturbationMapping.of(
        Curve.class,
        CurveRateIndexFilter.of(IborIndices.USD_LIBOR_3M),
        buildShifts(libor3mCurveId, historicalCurves, scenarioDates));

    PerturbationMapping<Curve> libor6mMappings = PerturbationMapping.of(
        Curve.class,
        CurveRateIndexFilter.of(IborIndices.USD_LIBOR_6M),
        buildShifts(libor6mCurveId, historicalCurves, scenarioDates));

    // create a scenario definition from these mappings
    return ScenarioDefinition.ofMappings(
        discountCurveMappings,
        libor3mMappings,
        libor6mMappings);
  }

  private static CurvePointShifts buildShifts(
      RateCurveId curveId,
      Map<LocalDate, Map<RateCurveId, Curve>> historicalCurves,
      List<LocalDate> scenarioDates) {

    CurvePointShiftsBuilder builder = CurvePointShifts.builder(ShiftType.ABSOLUTE);

    for (int scenarioIndex = 1; scenarioIndex < scenarioDates.size(); scenarioIndex++) {
      LocalDate previousDate = scenarioDates.get(scenarioIndex - 1);
      LocalDate scenarioDate = scenarioDates.get(scenarioIndex);
      Map<RateCurveId, Curve> previousCurves = historicalCurves.get(previousDate);
      Map<RateCurveId, Curve> curves = historicalCurves.get(scenarioDate);

      // get the curve from this scenario date and the previous scenario date
      NodalCurve curve = (NodalCurve) curves.get(curveId);
      NodalCurve previousCurve = (NodalCurve) previousCurves.get(curveId);

      // obtain the curve node metadata - this is used to identify a node to apply a perturbation to
      List<CurveParameterMetadata> curveNodeMetadata = curve.getMetadata().getParameterMetadata().get();

      // build up the shifts to apply to each node
      // these are calculated as the actual change in the zero rate at that node between the two scenario dates
      for (int curveNodeIdx = 0; curveNodeIdx < curve.getParameterCount(); curveNodeIdx++) {
        double zeroRate = curve.getYValues().get(curveNodeIdx);
        double previousZeroRate = previousCurve.getYValues().get(curveNodeIdx);
        double shift = (zeroRate - previousZeroRate);
        builder.addShift(scenarioIndex, curveNodeMetadata.get(curveNodeIdx).getIdentifier(), shift);
      }
    }
    return builder.build();
  }

  private static void outputPnl(List<LocalDate> scenarioDates, ScenarioResult<?> scenarioValuations) {
    NumberFormat numberFormat = new DecimalFormat("0.00");
    double basePv = ((CurrencyAmount) scenarioValuations.get(0)).getAmount();
    System.out.println("Base PV (USD): " + numberFormat.format(basePv));
    System.out.println();
    System.out.println("P&L series (USD):");
    for (int i = 1; i < scenarioValuations.size(); i++) {
      double scenarioPv = ((CurrencyAmount) scenarioValuations.get(i)).getAmount();
      double pnl = scenarioPv - basePv;
      LocalDate scenarioDate = scenarioDates.get(i);
      System.out.println(Messages.format("{} = {}", scenarioDate, numberFormat.format(pnl)));
    }
  }

  //-------------------------------------------------------------------------
  // create a libor 3m vs libor 6m swap
  private static Trade createTrade() {
    NotionalSchedule notional = NotionalSchedule.of(Currency.USD, 1_000_000);

    SwapLeg payLeg = RateCalculationSwapLeg.builder()
        .payReceive(PayReceive.PAY)
        .accrualSchedule(PeriodicSchedule.builder()
            .startDate(LocalDate.of(2015, 9, 11))
            .endDate(LocalDate.of(2021, 9, 11))
            .frequency(Frequency.P3M)
            .businessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, HolidayCalendars.USNY))
            .build())
        .paymentSchedule(PaymentSchedule.builder()
            .paymentFrequency(Frequency.P3M)
            .paymentDateOffset(DaysAdjustment.NONE)
            .build())
        .notionalSchedule(notional)
        .calculation(IborRateCalculation.of(IborIndices.USD_LIBOR_3M))
        .build();

    SwapLeg receiveLeg = RateCalculationSwapLeg.builder()
        .payReceive(PayReceive.RECEIVE)
        .accrualSchedule(PeriodicSchedule.builder()
            .startDate(LocalDate.of(2015, 9, 11))
            .endDate(LocalDate.of(2021, 9, 11))
            .frequency(Frequency.P6M)
            .businessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, HolidayCalendars.USNY))
            .build())
        .paymentSchedule(PaymentSchedule.builder()
            .paymentFrequency(Frequency.P6M)
            .paymentDateOffset(DaysAdjustment.NONE)
            .build())
        .notionalSchedule(notional)
        .calculation(IborRateCalculation.of(IborIndices.USD_LIBOR_6M))
        .build();

    return SwapTrade.builder()
        .product(Swap.of(payLeg, receiveLeg))
        .tradeInfo(TradeInfo.builder()
            .id(StandardId.of("example", "1"))
            .attributes(ImmutableMap.of("description", "Libor 3m vs Libor 6m"))
            .counterparty(StandardId.of("example", "A"))
            .settlementDate(LocalDate.of(2015, 9, 11))
            .build())
        .build();
  }

}
