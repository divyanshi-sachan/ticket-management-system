package com.ticketsystem.repository;

import com.ticketsystem.model.Agent;
import com.ticketsystem.model.Customer;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void saveCustomer(Customer customer);

    void saveAgent(Agent agent);

    Optional<Customer> findCustomerById(int id);

    Optional<Agent> findAgentById(int id);

    List<Customer> findAllCustomers();

    List<Agent> findAllAgents();
}
