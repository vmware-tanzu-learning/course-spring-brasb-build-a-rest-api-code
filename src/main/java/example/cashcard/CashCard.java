package example.cashcard;

import org.springframework.data.annotation.Id;

record CashCard(@Id Integer id, double amount, String owner) {
}
