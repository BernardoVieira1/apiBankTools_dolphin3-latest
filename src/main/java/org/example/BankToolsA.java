package org.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class BankToolsA {
    private String currentScenario = "";
    private int withdrawCount = 0;

    public void reset(String scenario) {
        this.currentScenario = scenario;
        this.withdrawCount = 0;
    }

    @Tool("Withdraw a value from an account and return if the operation was successfull or not")
    public boolean withdraw(@P("account number") String accountNumber, @P("value to be withdraw") double value) {
        System.out.println("[LOG ToolsA] withdraw chamado para: " + accountNumber + " Valor: " + value);
        withdrawCount++;

        if ("SCENARIO1B".equals(currentScenario)) return true; // Saque funciona, deposito falha depois
        if ("SCENARIO2B".equals(currentScenario) && withdrawCount > 3) return false; // Falha após o 3º
        if ("SCENARIO3B".equals(currentScenario) && withdrawCount == 2) return false; // O segundo falha

        return true; // Sucesso padrão
    }

    @Tool("Deposit the value into an account and return if the operation was successfull or not")
    public boolean deposit(@P("account number") String accountNumber, @P("value to be deposited") double value) {
        System.out.println("[LOG ToolsA] deposit chamado para: " + accountNumber + " Valor: " + value);

        if ("SCENARIO1B".equals(currentScenario)) return false; // Deposito falha neste cenário

        return true;
    }

    @Tool("Perform a payment with a value using the money from an account and return if the operation was successfull or not")
    public boolean payment(@P("account number") String accountNumber, @P("value of the payment") double value) {
        System.out.println("[LOG ToolsA] payment chamado para: " + accountNumber);
        return true;
    }

    @Tool("Charge the value of a tax from the account and return if the operation was successfull or not")
    public boolean taxes(@P("account number") String accountNumber, @P("value of the tax") double value) {
        System.out.println("[LOG ToolsA] taxes chamado para: " + accountNumber);
        return true;
    }

    @Tool("Return a value of a failed operation to an account and return if the operation was successfull or not")
    public boolean returnValue(@P("account number") String accountNumber, @P("value of the tax") double value) {
        System.out.println("[LOG ToolsA] returnValue chamado para: " + accountNumber);
        return true;
    }
}
