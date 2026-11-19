package com.ticketsystem.service;

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
        Customer customer = new Customer(nextUserId++, name, email);
        userRepository.saveCustomer(customer);
        return customer;
    }

    public Agent registerAgent(String name, String email) {
        Agent agent = new Agent(nextUserId++, name, email);
        userRepository.saveAgent(agent);
        return agent;
    }

    public List<Customer> getAllCustomers() {
        return userRepository.findAllCustomers();
    }

    public List<Agent> getAllAgents() {
        return userRepository.findAllAgents();
    }
}
