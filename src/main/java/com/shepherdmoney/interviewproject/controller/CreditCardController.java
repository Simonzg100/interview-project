package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<?> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        // 1. validate the parameters
        if (payload == null) {
            return ResponseEntity.badRequest().body("Payload is null");
        }
        Integer userId = payload.getUserId();
        String cardIssuanceBank = payload.getCardIssuanceBank();
        String cardNumber = payload.getCardNumber();

        if (Integer.toString(userId).equals("null") || cardIssuanceBank == null || cardNumber == null) {
            return ResponseEntity.badRequest().body("Invalid input");
        }
        // 2. check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // 3. add the credit card to the user
        User user = userOptional.get();
        CreditCard creditCard = new CreditCard();
        creditCard.setIssuanceBank(cardIssuanceBank);
        creditCard.setNumber(cardNumber);
        creditCard.setUser(user);

        CreditCard savedCreditCard = creditCardRepository.save(creditCard);
        return ResponseEntity.ok(savedCreditCard.getId());
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null

        List<CreditCard> creditCards = creditCardRepository.findByUserId(userId);
        if (creditCards == null) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }

        List<CreditCardView> creditCardViews = new ArrayList<>();
        for (CreditCard creditCard : creditCards) {
            CreditCardView creditCardView = new CreditCardView(creditCard.getNumber(), creditCard.getIssuanceBank());
            creditCardViews.add(creditCardView);
        }
        return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);
        if (creditCard.isEmpty()) {
            return ResponseEntity.badRequest().body(-1);
        }
        int userId = creditCard.get().getUser().getId();

        return ResponseEntity.ok(userId);
    }


    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<?> postMethodName(@RequestBody UpdateBalancePayload[] payloads) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.


        if (payloads.length == 0) {
            // Check if the input payloads are empty or null
            return ResponseEntity.badRequest().body("Empty or null payload");
        }
        System.out.println(Arrays.toString(payloads));
        // Loop through each transaction data
        for (UpdateBalancePayload payload : payloads) {
            String cardNumber = payload.getCreditCardNumber();
            LocalDate date = payload.getBalanceDate();
            double amount = payload.getBalanceAmount();

            Optional<CreditCard> cardOptional = creditCardRepository.findByNumber(cardNumber);
            // If no credit card is found, return a 400 Bad Request
            if (cardOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Credit card not found" );
            }

            CreditCard creditCard = cardOptional.get();
            List<BalanceHistory> histories = creditCard.getBalanceHistories();
            Map<LocalDate, BalanceHistory> balanceHistoryMap = new HashMap<>();
            for (BalanceHistory bh : histories) {
                balanceHistoryMap.put(bh.getDate(), bh);
            }

            // Find or create a balance record for the specified date
            BalanceHistory history = balanceHistoryMap.get(date);
            if (history == null) {
                history = new BalanceHistory();
                history.setDate(date);
                history.setCreditCard(creditCard);
                histories.add(history);
            }

            // Update the balance
            history.setBalance(history.getBalance() + amount);

            // If the amount change is not zero, update all subsequent balances after the specified date
            if (amount != 0) {
                for (BalanceHistory bh : histories) {
                    if (bh.getDate().isAfter(date)) {
                        bh.setBalance(bh.getBalance() + amount);
                    }
                }
            }

            creditCardRepository.save(creditCard);
        }

        return ResponseEntity.ok("Balance updated successfully");
    }


}
