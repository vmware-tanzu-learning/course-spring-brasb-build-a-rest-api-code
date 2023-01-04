package example.cashcard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends PagingAndSortingRepository<CashCard, Long>,
        CrudRepository<CashCard, Long> {
    CashCard findByIdAndUsername(Long id, String username);
}
