import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.math.BigDecimal;
import java.time.YearMonth;


import static java.time.temporal.ChronoUnit.DAYS;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    List<Payment> findPaymentsSortedByDateDesc() {
        return paymentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> {
                    YearMonth currentYearMonth = dateTimeProvider.yearMonthNow();
                    return pay.getPaymentDate().getMonth().equals(currentYearMonth.getMonth())
                            && pay.getPaymentDate().getYear() == currentYearMonth.getYear();
                })
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> pay.getPaymentDate().getMonth().equals(yearMonth.getMonth())
                        && pay.getPaymentDate().getYear() == yearMonth.getYear())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenLastDays(int days) {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> daysDifference(pay.getPaymentDate(),
                        dateTimeProvider.zonedDateTimeNow()) <= days)
                .collect(Collectors.toList());
    }

    private long daysDifference(ZonedDateTime date1, ZonedDateTime date2) {
        return DAYS.between(date1.toLocalDate(), date2.toLocalDate());
    }

    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> pay.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    Set<String> findProductsSoldInCurrentMonth() {
        return findPaymentsForGivenMonth(dateTimeProvider.yearMonthNow())
                .stream()
                .flatMap(pay -> pay.getPaymentItems().stream())
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth)
                .stream()
                .flatMap(pay -> pay.getPaymentItems().stream())
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return findPaymentsForGivenMonth(yearMonth)
                .stream()
                .flatMap(pay -> pay.getPaymentItems().stream())
                .map(pi -> pi.getRegularPrice().subtract(pi.getFinalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> pay.getUser().getEmail().equals(userEmail))
                .flatMap(pay -> pay.getPaymentItems().stream())
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll()
                .stream()
                .filter(pay -> pay.getPaymentItems()
                        .stream()
                        .map(PaymentItem::getFinalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .compareTo(BigDecimal.valueOf(value)) > 0)
                .collect(Collectors.toSet());
    }
}