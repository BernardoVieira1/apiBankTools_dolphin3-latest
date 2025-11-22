package org.example;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

public class Main {

    interface AssistenteBancario {
        String responder(String comando);
    }

    public static void main(String[] args) {

        String modelName = "dolphin3:latest";

        var model = OllamaChatModel.builder()
                .modelName(modelName)
                .baseUrl("http://localhost:11434")
                .temperature(0.0) // Temperatura 0 para maior determinismo nos testes
                .build();

        // Instanciar as ferramentas
        BankToolsA toolsA = new BankToolsA();
        BankToolsB toolsB = new BankToolsB();

        // --- CONFIGURAÇÃO DO TESTE ---
        Object[] toolsConfig = new Object[]{toolsB}; //Aqui vc vai mudar a configuração

        // Defina qual prompt e cenário vai rodar agora
        String prompt = "Withdraw 600 from account AG7340H and 700 from account TG23986Q. If one of the operations is not successful, return the value to the other account and don't execute anything else. If both operations are successful, perform a deposit of the summed value into account WS2754T and perform a payment of 1200 in this same account.";
        String cenario = "SCENARIO3B"; //aqui vc muda o cenário

        System.out.println("=== Iniciando 10 Execuções ===");
        System.out.println("Config: " + modelName + " | Cenário: " + cenario);

        for (int i = 1; i <= 10; i++) {
            System.out.println("\n--- Execução #" + i + " ---");

            // Resetar as tools para a nova execução
            toolsA.reset(cenario);
            toolsB.reset(cenario);

            // Criar o assistente (recriar a cada loop garante memória limpa)
            AssistenteBancario bot = AiServices.builder(AssistenteBancario.class)
                    .chatModel(model)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .tools(toolsConfig)
                    .build();

            try {
                String resposta = bot.responder(prompt);
                System.out.println("Resposta do LLM: " + resposta);
            } catch (Exception e) {
                System.out.println("Erro na execução: " + e.getMessage());
            }
        }
    }
}