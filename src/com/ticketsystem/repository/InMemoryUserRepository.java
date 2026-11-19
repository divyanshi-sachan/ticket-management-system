package com.ticketsystem.repository;

import com.ticketsystem.model.Agent;
import com.ticketsystem.model.Customer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, Customer> customers = new LinkedHashMap<>();
    private final Map<Integer, Agent> agents = new LinkedHashMap<>();

    @Override
    public void saveCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    @Override
    public void saveAgent(Agent agent) {
        agents.put(agent.getId(), agent);
    }

    @Override
    public Optional<Customer> findCustomerById(int id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Agent> findAgentById(int id) {
        return Optional.ofNullable(agents.get(id));
    }

    @Override
    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public List<Agent> findAllAgents() {
        return new ArrayList<>(agents.values());
    }
}
