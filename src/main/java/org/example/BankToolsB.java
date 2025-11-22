package org.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class BankToolsB {
    private String currentScenario = "";
    private int withdrawCount = 0;

    public void reset(String scenario) {
        this.currentScenario = scenario;
        this.withdrawCount = 0;
    }

    @Tool("Execute an operation in an account with a given value and return if the operation was successfull or not")
    public boolean executeOperation(
            @P("WITHDRAW if the operation is a withdraw, DEPOSIT if the operation is a deposit, TAX to charge the value of a tax from an account, RETURN to return the value of a failed operation, PAYMENT to perform a payment") OperationType type,
            @P("account number") String accountNumber,
            @P("value to be used in the operation") double value) {

        System.out.println("[LOG ToolsB] executeOperation: " + type + " Conta: " + accountNumber);

        if (type == OperationType.WITHDRAW) {
            withdrawCount++;
            if ("SCENARIO2B".equals(currentScenario) && withdrawCount > 3) return false;
            if ("SCENARIO3B".equals(currentScenario) && withdrawCount == 2) return false;
        }

        if (type == OperationType.DEPOSIT) {
            if ("SCENARIO1B".equals(currentScenario)) return false;
        }

        return true;
    }
}
