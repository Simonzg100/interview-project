package com.shepherdmoney.interviewproject;

import com.shepherdmoney.interviewproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InterviewProjectApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }



}
