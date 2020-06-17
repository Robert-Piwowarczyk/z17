import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentService paymentService;
    @Test DateTimeProvider dateTimeProvider;

    @
    void init() {
        MockitoAnnotations.initMocks(this);
        paymentService = new PaymentService(new FakePaymentRepository(), dateTimeProvider);
        when(dateTimeProvider.zonedDateTimeNow()).thenReturn(ZonedDateTime.of(2019, 4, 12, 10, 0, 0, 0, ZoneId.systemDefault()));
        when(dateTimeProvider.yearMonthNow()).thenReturn(YearMonth.of(2019, 4));
    }

    @Test
    void shouldReturnPaymentsSortedByDateDesc() {
        List<Payment> payments = paymentService.findPaymentsSortedByDateDesc();

        // then
        assertThat(payments.size()).isEqualTo(6);
        assertThat(payments.get(0).getPaymentDate()).isEqualTo(ZonedDateTime.of(2019, 4, 10, 10, 0, 0, 0, ZoneId.systemDefault()));
        assertThat(payments.get(5).getPaymentDate()).isEqualTo(ZonedDateTime.of(2010, 10, 10, 10, 0, 0, 0, ZoneId.systemDefault()));
    }

    @Test
    void shouldReturnPaymentForCurrentMonth() {
        // paymenty z aktualnego miesiąca

        // when
        List<Payment> paymentsForCurrentMonth = paymentService.findPaymentsForCurrentMonth();

        // then
        assertThat(paymentsForCurrentMonth.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnPaymentForPreviousMonth() {
        // paymenty z poprzedniego miesiąca
        // when
        List<Payment> paymentsForPrevious = paymentService.findPaymentsForGivenMonth(YearMonth.of(2019, 3));

        // then
        assertThat(paymentsForPrevious.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnPaymentForLast30Days() {
        // paymenty z ostatnich 30 dni
        // when
        List<Payment> payments = paymentService.findPaymentsForGivenLastDays(30);

        // then
        assertThat(payments.size()).isEqualTo(3);
    }

    @Test
    void shouldReturnPaymentForLast15Days() {
        // paymenty z ostatnich 15 dni
        // when
        List<Payment> payments = paymentService.findPaymentsForGivenLastDays(15);

        // then
        assertThat(payments.size()).isEqualTo(2);
    }

    @Test
    void shouldFindPaymentsWithOnePaymentItem() {
        // paymenty które mają tylko 1 payment item
        Set<Payment> payments = paymentService.findPaymentsWithOnePaymentItem();

        assertThat(payments.size()).isEqualTo(3);
        for (Payment payment : payments) {
            assertThat(payment.getPaymentItems().size()).isEqualTo(1);
        }

    }

    @Test
    void shouldFindProductsSoldInCurrentMonth() {
        // nazwy sprzedanych produktów w tym miesiącu
        Set<String> products = paymentService.findProductsSoldInCurrentMonth();

        // then
        assertThat(products.size()).isEqualTo(3);
        assertThat(products).contains("Buty do biegania");
        assertThat(products).contains("Piłka koszykowa");
        assertThat(products).contains("Kij do baseball");
    }

    @Test
    void shouldCountTotalIncomeInApril2019() {
        // suma sprzedaży w kwietniu 2019
        BigDecimal total = paymentService.sumTotalForGivenMonth(YearMonth.of(2019, 4));

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(390));
    }

    @Test
    void shouldCountTotalDiscountsInApril2019() {
        // suma rabatów w kwietniu 2019
        BigDecimal total = paymentService.sumDiscountForGivenMonth(YearMonth.of(2019, 4));

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(40));
    }

    @Test
    void shouldFindPaymentItemsForEdek() {
        // lista zakupów użytkownika o emailu "edek@gmail.com"
        List<PaymentItem> items = paymentService.getPaymentsForUserWithEmail("edek@gmail.com");

        assertThat(items.size()).isEqualTo(4);
    }

    @Test
    void shouldFindPaymentItemsForKasia() {
        // lista zakupów użytkownika o emailu "kasia@gmail.com"
        List<PaymentItem> items = paymentService.getPaymentsForUserWithEmail("kasia@gmail.com");

        assertThat(items.size()).isEqualTo(1);
    }

    @Test
    void shouldFindPaymentItemsForAdam() {
        // lista zakupów użytkownika o emailu "adam@gmail.com"
        List<PaymentItem> items = paymentService.getPaymentsForUserWithEmail("adam@gmail.com");

        assertThat(items.size()).isEqualTo(5);
    }

    @Test
    void shouldFindWithTotalValueOver150() {
        // paymenty których suma przekracza 180zł
        Set<Payment> payments = paymentService.findPaymentsWithValueOver(180);

        // then
        assertThat(payments.size()).isEqualTo(4);
    }


    @Test
    void shouldFindWithTotalValueOver300() {
        // paymenty których suma przekracza 300zł
        Set<Payment> payments = paymentService.findPaymentsWithValueOver(300);

        // then
        assertThat(payments.size()).isEqualTo(2);
    }

    public static class FakePaymentRepository implements PaymentRepository {

        public List<Payment> findAll() {
            List<Payment> payments = new ArrayList<>();

            User adam = new User("adam@gmail.com");
            User kasia = new User("kasia@gmail.com");
            User edek = new User("edek@gmail.com");

            List<PaymentItem> paymentItems1 = Arrays.asList(
                    new PaymentItem("Piłka koszykowa", BigDecimal.valueOf(100), BigDecimal.valueOf(100)),
                    new PaymentItem("Kij do baseball", BigDecimal.valueOf(100), BigDecimal.valueOf(90)),
                    new PaymentItem("Buty sportowe", BigDecimal.valueOf(200), BigDecimal.valueOf(200))
            );
            Payment payment1 = new Payment(adam, ZonedDateTime.of(2010, 10, 10, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems1);
            payments.add(payment1);

            List<PaymentItem> paymentItems2 = Arrays.asList(
                    new PaymentItem("Piłka koszykowa", BigDecimal.valueOf(100), BigDecimal.valueOf(100)),
                    new PaymentItem("Kij do baseball", BigDecimal.valueOf(100), BigDecimal.valueOf(90))
            );
            Payment payment2 = new Payment(adam, ZonedDateTime.of(2019, 4, 10, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems2);
            payments.add(payment2);

            List<PaymentItem> paymentItems3 = Collections.singletonList(
                    new PaymentItem("Buty do biegania", BigDecimal.valueOf(230), BigDecimal.valueOf(200))
            );
            Payment payment3 = new Payment(kasia, ZonedDateTime.of(2019, 4, 9, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems3);
            payments.add(payment3);

            List<PaymentItem> paymentItems4 = Collections.singletonList(
                    new PaymentItem("Pompka rowerowa", BigDecimal.valueOf(50), BigDecimal.valueOf(50))
            );
            Payment payment4 = new Payment(edek, ZonedDateTime.of(2019, 3, 9, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems4);
            payments.add(payment4);

            List<PaymentItem> paymentItems5 = Arrays.asList(
                    new PaymentItem("Siodełko rowerowe", BigDecimal.valueOf(119), BigDecimal.valueOf(119)),
                    new PaymentItem("Lampka rowerowa", BigDecimal.valueOf(50), BigDecimal.valueOf(50))
            );
            Payment payment5 = new Payment(edek, ZonedDateTime.of(2018, 4, 9, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems5);
            payments.add(payment5);

            List<PaymentItem> paymentItems6 = Collections.singletonList(
                    new PaymentItem("Rower", BigDecimal.valueOf(4000), BigDecimal.valueOf(4000))
            );
            Payment payment6 = new Payment(edek, ZonedDateTime.of(2019, 3, 22, 10, 0, 0, 0, ZoneId.systemDefault()), paymentItems6);
            payments.add(payment6);

            return payments;
        }
    }
}
