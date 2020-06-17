import java.util.List;

public interface PaymentRepository {

    List<Payment> findAll();
}
