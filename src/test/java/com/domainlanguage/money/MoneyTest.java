/**
 * Copyright (c) 2005 Domain Language, Inc. (http://domainlanguage.com) This
 * free software is distributed under the "MIT" licence. See file licence.txt.
 * For more information, see http://timeandmoney.sourceforge.net.
 */
package com.domainlanguage.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class MoneyTest {
    private static Currency USD = Currency.getInstance("USD");
    private static Currency JPY = Currency.getInstance("JPY");
    private static Currency EUR = Currency.getInstance("EUR");

    private Money d15 = Money.valueOf(new BigDecimal("15.0"), USD);
    private Money d2_51 = Money.valueOf(new BigDecimal("2.51"), USD);
    private Money y50 = Money.valueOf(new BigDecimal("50"), JPY);
    private Money e2_51 = Money.valueOf(new BigDecimal("2.51"), EUR);
    private Money d100 = Money.valueOf(new BigDecimal("100.0"), USD);

//    public void testSerialization() {
//        SerializationTester.assertCanBeSerialized(d15);
//    }

    @Test
    public void testCreationFromDouble() {
        assertThat(Money.valueOf(15.0, USD)).isEqualTo(d15);
        assertThat(Money.valueOf(2.51, USD)).isEqualTo(d2_51);
        assertThat(Money.valueOf(50.1, JPY)).isEqualTo(y50);
        assertThat(Money.valueOf(100, USD)).isEqualTo(d100);
    }

    @Test
    public void testYen() {
        assertThat(y50.toString()).isEqualTo("¥ 50");
        Money y80 = Money.valueOf(new BigDecimal("80"), JPY);
        Money y30 = Money.valueOf(30, JPY);
        assertThat(y50.plus(y30)).isEqualTo(y80);
        assertThat(y50.times(1.6)).isEqualTo(y80);
    }

    @Test
    public void testConstructor() throws Exception {
        Money d69_99 = new Money(new BigDecimal("69.99"), USD);
        assertThat(new BigDecimal("69.99")).isEqualTo(d69_99.getAmount());
        assertThat(d69_99.getCurrency()).isEqualTo(USD);
        try {
            new Money(new BigDecimal("69.999"), USD);
            fail("Money constructor shall never round, and shall not accept a value whose scale doesn't fit the Currency.");
        } catch (IllegalArgumentException correctResponse) {
        }
    }

    @Test
    public void testDivide() {
        assertThat(Money.dollars(33.33)).isEqualTo(d100.dividedBy(3));
        assertThat(Money.dollars(16.67)).isEqualTo(d100.dividedBy(6));
    }

    @Test
    public void testMultiply() {
        assertThat(Money.dollars(150)).isEqualTo(d15.times(10));
        assertThat(Money.dollars(1.5)).isEqualTo(d15.times(0.1));
        assertThat(Money.dollars(70)).isEqualTo(d100.times(0.7));
    }

    @Test
    public void testMultiplyRounding() {
        assertThat(Money.dollars(66.67)).isEqualTo(d100.times(0.66666667));
        assertThat(Money.dollars(66.66)).isEqualTo(d100.times(0.66666667, RoundingMode.DOWN));
    }

    @Test
    public void testMultiplicationWithExplicitRounding() {
        assertThat(Money.dollars(66.67)).isEqualTo(d100.times(new BigDecimal("0.666666"), RoundingMode.HALF_EVEN));
        assertThat(Money.dollars(66.66)).isEqualTo(d100.times(new BigDecimal("0.666666"), RoundingMode.DOWN));
        assertThat(Money.dollars(-66.66)).isEqualTo(d100.negated().times(new BigDecimal("0.666666"), RoundingMode.DOWN));
    }

    @Test
    public void testMinimumIncrement() {
        assertThat(Money.valueOf(0.01, USD)).isEqualTo(d100.minimumIncrement());
        assertThat(Money.valueOf(1, JPY)).isEqualTo(y50.minimumIncrement());
    }

    @Test
    public void testAdditionOfDifferentCurrencies() {
        try {
            d15.plus(e2_51);
            fail("added different currencies");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testDivisionByMoney() {
        assertThat(new BigDecimal(2.50)).isEqualTo(Money.dollars(5.00).dividedBy(Money.dollars(2.00)).decimalValue(1, RoundingMode.UNNECESSARY));
        assertThat(new BigDecimal(1.25)).isEqualTo(Money.dollars(5.00).dividedBy(Money.dollars(4.00)).decimalValue(2, RoundingMode.UNNECESSARY));
        assertThat(new BigDecimal(5)).isEqualTo(Money.dollars(5.00).dividedBy(Money.dollars(1.00)).decimalValue(0, RoundingMode.UNNECESSARY));
        try {
            Money.dollars(5.00).dividedBy(Money.dollars(2.00)).decimalValue(0, RoundingMode.UNNECESSARY);
            fail("dividedBy(Money) does not allow rounding.");
        } catch (ArithmeticException correctBehavior) {
        }
        try {
            Money.dollars(10.00).dividedBy(Money.dollars(3.00)).decimalValue(5, RoundingMode.UNNECESSARY);
            fail("dividedBy(Money) does not allow rounding.");
        } catch (ArithmeticException correctBehavior) {
        }
    }

    @Test
    public void testCloseNumbersNotEqual() {
        Money d2_51a = Money.dollars(2.515);
        Money d2_51b = Money.dollars(2.5149);
        assertThat(d2_51a.equals(d2_51b)).isFalse();
    }

    @Test
    public void testCompare() {
        assertThat(d15).isGreaterThan(d2_51);
        assertThat(d2_51).isLessThan(d15);
        assertThat(d15.isGreaterThan(d15)).isFalse();
        assertThat(d15.isLessThan(d15)).isFalse();
        try {
            d15.isGreaterThan(e2_51);
            fail();
        } catch (Exception correctBehavior) {
        }
    }

    @Test
    public void testDifferentCurrencyNotEqual() {
        assertThat(d2_51).isNotEqualTo(e2_51);
    }

    @Test
    public void testEquals() {
        Money d2_51a = Money.dollars(2.51);
        assertThat(d2_51a).isEqualTo(d2_51);
    }

    @Test
    public void testEqualsNull() {
        Money d2_51a = Money.dollars(2.51);
        Object objectNull = null;
        assertThat(d2_51a.equals(objectNull)).isFalse();

        //This next test seems just like the previous, but it's not
        //The Java Compiler early binds message sends and
        //it will bind the next call to equals(Money) and
        //the previous will bind to equals(Object)
        //I renamed the original equals(Money) to
        //equalsMoney(Money) to prevent wrong binding.
        Money moneyNull = null;
        assertThat(d2_51a.equals(moneyNull)).isFalse();
    }

    @Test
    public void testHash() {
        Money d2_51a = Money.dollars(2.51);
        assertThat(d2_51a.hashCode()).isEqualTo(d2_51.hashCode());
    }

    @Test
    public void testNegation() {
        assertThat(Money.dollars(-15)).isEqualTo(d15.negated());
        assertThat(e2_51.negated().negated()).isEqualTo(e2_51);
    }

    @Test

    public void testPositiveNegative() {
        assertThat(d15.isPositive()).isTrue();
        assertThat(Money.dollars(-10).isNegative()).isTrue();
        assertThat(Money.dollars(0).isPositive()).isFalse();
        assertThat(Money.dollars(0).isNegative()).isFalse();
        assertThat(Money.dollars(0).isZero()).isTrue();
    }

    @Test
    public void testPrint() {
        assertThat(d15.toString(Locale.US)).isEqualTo("$ 15.00");
        assertThat(d15.toString(Locale.UK)).isEqualTo("US$ 15.00");
    }
    // TODO: Formatted printing of Money
    //	public void testLocalPrinting() {
    //		assertThat(d15.localString()).isEqualTo("$15.00");
    //		assertEquals("2,51 DM", m2_51.localString());
    //	}

    @Test
    public void testRound() {
        Money dRounded = Money.dollars(1.2350);
        assertThat(Money.dollars(1.24)).isEqualTo(dRounded);
    }

    @Test
    public void testSubtraction() {
        assertThat(Money.dollars(12.49)).isEqualTo(d15.minus(d2_51));
    }

//    public void testApplyRatio() {
//        Ratio oneThird = Ratio.of(1, 3);
//        Money result = Money.dollars(100).applying(oneThird, 1, RoundingMode.UP);
//        assertThat(Money.dollars(33.40)).isEqualTo(result);
//    }

    public void testIncremented() {
        assertThat(Money.dollars(2.52)).isEqualTo(d2_51.incremented());
        assertThat(Money.valueOf(51, JPY)).isEqualTo(y50.incremented());
    }

    public void testFractionalPennies() {
//        CurrencyPolicy(USD, 0.0025);
//        Smallest unit.unit Any Money based on this CurrencyPolicy must be some multiple of the
//        smallest unit. "Scale" is insufficient, because the limit is not always a number of demial places.
//        Money someFee = Money.dollars(0.0025);
//        Money wholeMoney = someFee.times(4);
//        assertThat(Money.dollars(0.01)).isEqualTo(wholeMoney);

    }

}
