package com.ticketsystem.service;

import com.ticketsystem.exception.UserNotFoundException;
import com.ticketsystem.model.Agent;
import com.ticketsystem.model.Customer;
import com.ticketsystem.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;
    private int nextUserId = 1;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Customer registerCustomer(String name, String email) {
        validate(name, email);
        Customer customer = new Customer(nextUserId++, name, email);
        userRepository.saveCustomer(customer);
        return customer;
    }

    public Agent registerAgent(String name, String email) {
        validate(name, email);
        Agent agent = new Agent(nextUserId++, name, email);
        userRepository.saveAgent(agent);
        return agent;
    }

    public Customer getCustomer(int customerId) {
        return userRepository.findCustomerById(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer", customerId));
    }

    public List<Customer> getAllCustomers() {
        return userRepository.findAllCustomers();
    }

    public List<Agent> getAllAgents() {
        return userRepository.findAllAgents();
    }

    private void validate(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
    }
}
