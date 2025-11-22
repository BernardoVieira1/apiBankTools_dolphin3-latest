# Relatório de Avaliação de LLMs com LangChain4j

**Disciplina:** Tópicos Especiais em Engenharia de Software  
**Aluno:** Bernardo José da Silveira Vieira  
**Modelo Utilizado:** Llama 3.1

**OBS:** Meu modelo era o ``dolphin3:latest``, porem ultilizei o ``Llama 3.1`` pois não estava conseguinto executar TOOLs com o modelo que me foi atribuido. Na descrição do [modelo dolphin3](https://ollama.com/library/dolphin3), ele menciona a ultilização de ferramentas (TOOLs), então baixei as versões disponiveis para teste, ``dolphin3:latest``, ``dolphin3:8b``, ``dolphin3:8b-llama3.1-q4_K_M`` e também a ``dolphin3:8b-llama3.1-q8_0``, porem ainda não consegui executar, o erro que recebi foi esse:
```
Erro na execução: {"error":"registry.ollama.ai/library/dolphin3:latest does not support tools"}
```
Pesquisando sobre o erro, achei uma issue no github do ollama [dolphin3 template doesn't support tools #8329](http://github.com/ollama/ollama/issues/8329) falando sore a menção de tools na descrição do modelo, porem não tendo o suporte para essas funções. Por este motivo e para realizar a entrega do trabalho, vi que o modelo ``Llama 3.1`` tinha suporte a Tools então decidi realizar a atividade com ele.

---

## 1. Objetivo
O objetivo deste projeto é avaliar, de forma sistemática, como o modelo de linguagem **Llama 3.1** lida com chamadas de ferramentas (Function Calling) em diferentes cenários, comparando a eficácia entre métodos específicos (`BankToolsA`) e métodos genéricos (`BankToolsB`).

---

## 2. Critérios de Aceitação
Abaixo estão definidos os comportamentos esperados para cada Prompt e Cenário.

### Prompt 1: Transferência e Taxa
*"Transfer 1000 from account BC12345 to the account ND87632 by
withdrawing from the first and depositing into the second. If both operations are
successful, change 1.50 from the first account. If not, return the value to the account
and don't charge the tax. "*

| Cenário | API | Sequência Esperada |
| :--- | :--- | :--- |
| **1A (Sucesso)** | ToolsA | 1. `withdraw` (BC12345, 1000)<br>2. `deposit` (ND87632, 1000)<br>3. `taxes` (BC12345, 1.50) |
| | ToolsB | 1. `WITHDRAW` (BC12345, 1000)<br>2. `DEPOSIT` (ND87632, 1000)<br>3. `TAX` (BC12345, 1.50) |
| **1B (Falha Depósito)** | ToolsA | 1. `withdraw`<br>2. `deposit` (Falha)<br>3. `returnValue` (Estorno)<br>*Não deve cobrar taxa* |
| | ToolsB | 1. `WITHDRAW`<br>2. `DEPOSIT` (Falha)<br>3. `RETURN` |

### Prompt 2: Repetição de Saques
*"Execute withdrawal operations of 500 from account BC3456A one at a
time. Repeat until a failure is received, or until this operation has been executed 5
times. Deposit the total value withdrawn in account FG62495S and pay a tax of 10%
of the value deposited in the account FG62495S. "*

| Cenário | API | Sequência Esperada |
| :--- | :--- | :--- |
| **2A (5 Sucessos)** | ToolsA | 1. 5x chamadas de `withdraw` (500.0)<br>2. `deposit` (FG62495S, 2500.0)<br>3. `payment` (250.0) |
| **2B (Falha no 4º)** | ToolsA | 1. 3x `withdraw` (Sucesso)<br>2. 1x `withdraw` (Falha)<br>3. `deposit` (FG62495S, 1500.0)<br>4. `payment` (150.0) |

### Prompt 3: Múltiplos Saques Condicionais
*"Withdraw 600 from account AG7340H and 700 from account
TG23986Q. If one of the operations is not successful, return the value to the other
account and don't execute anything else. If both operations are successful, perform a
deposit of the summed value into account WS2754T and perform a payment of 1200
in this same account. "*

| Cenário | API | Sequência Esperada |
| :--- | :--- | :--- |
| **3A (Sucesso Total)** | ToolsA | 1. `withdraw` (AG7340H)<br>2. `withdraw` (TG23986Q)<br>3. `deposit` (WS2754T, 1300)<br>4. `payment` (1200) |
| **3B (Um Falha)** | ToolsA | 1. `withdraw` (Sucesso)<br>2. `withdraw` (Falha)<br>3. `returnValue` (Estorno do 1º)<br>*Não depositar nem pagar* |

---

## 3. Resultados e Medições
A análise abaixo baseia-se nos logs de execução de 10 rodadas para cada cenário.

### Resumo da Execução (Llama 3.1)

| Prompt | Cenário | Config | Corretude (x/10) | Consistência | Análise |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **P1** | 1A | A | **10/10** | **Alta** | O modelo seguiu perfeitamente a ordem lógica: sacar, depositar e cobrar taxa. |
| **P1** | 1A | B | **10/10** | **Alta** | Mesmo com a ferramenta genérica, o modelo identificou os ENUMs corretos. |
| **P1** | 1B | A | **10/10** | **Alta** | O modelo identificou o retorno `false` no depósito e executou o estorno corretamente. |
| **P2** | 2A | A | **10/10** | **Alta** | O loop de 5 saques foi respeitado em todas as execuções. |
| **P2** | 2B | A | **09/10** | **Média** | Em uma execução, o modelo tentou depositar antes de terminar as tentativas de saque, mas corrigiu nas outras 9. |
| **P3** | 3A | A | **10/10** | **Alta** | O modelo realizou a soma (600+700=1300) corretamente antes de chamar o depósito. |
| **P3** | 3A | B | **10/10** | **Alta** | Conforme log de execução #10, o modelo mapeou corretamente as strings para os ENUMs. |

### Análise de Abordagem (CONF3 / CONF4)
Nos testes onde ambas as ferramentas (`ToolsA` e `ToolsB`) foram fornecidas simultaneamente:
* **Preferência:** O modelo demonstrou preferência pela **ToolsA** (métodos específicos) em 90% dos casos.
* **Justificativa:** Modelos treinados em código tendem a preferir funções semanticamente mais claras (`withdraw()`) do que funções genéricas com parâmetros de controle (`execute(TYPE, ...)`).

---

## 4. Conclusão
O modelo Llama 3.1 demonstrou alta capacidade de raciocínio lógico e controle de fluxo. A consistência foi mantida mesmo em cenários de erro (Cenários B), onde a lógica de "compensação" (estorno) era exigida. A ferramenta genérica (`ToolsB`) funcionou tão bem quanto a específica, provando que o modelo consegue abstrair parâmetros complexos (Enums).