package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface CashCardRepository extends CrudRepository<CashCard, Integer>, PagingAndSortingRepository<CashCard, Integer> {
    CashCard findByIdAndOwner(int id, String owner);

    boolean existsByIdAndOwner(int id, String owner);

    Page<CashCard> findByOwner(String owner, PageRequest pageRequest);
}
