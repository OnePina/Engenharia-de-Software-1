package Logica;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        Autenticacao autenticacao;

        if (!Autenticacao.existePinCadastrado()) {
            System.out.println("Nenhum PIN cadastrado.");
            System.out.print("Crie um novo PIN: ");
            String novoPin = input.nextLine();

            System.out.print("Confirme o novo PIN: ");
            String confirmarPin = input.nextLine();

            if (!novoPin.equals(confirmarPin)) {
                System.out.println("Os PINs digitados não coincidem. O sistema será encerrado.");
                input.close();
                return;
            }

            autenticacao = Autenticacao.criarNovoPin(novoPin);
            System.out.println("PIN cadastrado com sucesso!");

        } else {
            autenticacao = Autenticacao.carregarPin();

            if (autenticacao == null) {
                System.out.println("Erro ao carregar PIN. Sistema encerrado.");
                input.close();
                return;
            }

            System.out.print("Digite o PIN para acessar o sistema: ");
            String pinDigitado = input.nextLine();

            if (!autenticacao.validarPin(pinDigitado)) {
                System.out.println("PIN incorreto. Acesso negado.");
                input.close();
                return;
            }
        }

        ControleFinanceiro controle = new ControleFinanceiro(input);
        
        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\nCONTROLE FINANCEIRO PESSOAL");
            System.out.println("1. Novo Lançamento");
            System.out.println("2. Editar Lançamento");
            System.out.println("3. Excluir Lançamento");
            System.out.println("4. Gerar Relatório");
            System.out.println("5. Listar Todos os Lançamentos");
            System.out.println("6. Ver Saldo das Contas");
            System.out.println("7. Cadastrar Conta");
            System.out.println("8. Ver Faturas das Contas");
            System.out.println("9. Pagar Fatura");
            System.out.println("10. Alterar PIN");
            System.out.println("0. Sair");
            System.out.print("Escolha a opção: ");
            
            opcao = input.nextInt();
            input.nextLine(); 
            
            switch (opcao) {
                case 1: {
                    System.out.println("\nINSERIR LANÇAMENTO");

                    if (controle.getListaContas().isEmpty()) {
                        System.out.println("Nenhuma conta cadastrada. Cadastre uma conta antes de criar lançamentos.");
                        break;
                    }

                    System.out.println("\nContas disponíveis:");

                    for (Conta conta : controle.getListaContas()) {
                        System.out.println(conta.getIdConta() + " - " + conta.getNomeConta());
                    }

                    System.out.print("Escolha o ID da conta: ");
                    int idConta = input.nextInt();
                    input.nextLine();

                    Conta contaEscolhida = controle.buscarContaPorId(idConta);

                    if (contaEscolhida == null) {
                        System.out.println("Conta inválida. Operação cancelada.");
                        break;
                    }

                    int idLancamento = 1;
                    if (!controle.getListaLancamentos().isEmpty()) {
                        idLancamento = controle.getListaLancamentos()
                                .get(controle.getListaLancamentos().size() - 1)
                                .getIdLancamento() + 1;
                    }

                    System.out.println("ID do Lançamento: " + idLancamento + " (Gerado automaticamente)");

                    System.out.print("Descrição: ");
                    String descricao = input.nextLine();

                    System.out.print("Valor: R$ ");
                    String valorString = input.nextLine().replace(",", ".");

                    double valor;
                    try {
                        valor = Double.parseDouble(valorString);
                    } catch (NumberFormatException e) {
                        System.out.println("Erro: Valor digitado é inválido. A operação foi cancelada.");
                        break;
                    }

                    System.out.print("É Receita (1) ou Despesa (2)? ");
                    int tipoOpcao = input.nextInt();
                    input.nextLine();

                    String tipo;

                    if (tipoOpcao == 1) {
                        tipo = "receita";
                    } else if (tipoOpcao == 2) {
                        tipo = "despesa";
                    } else {
                        System.out.println("Tipo inválido. Operação cancelada.");
                        break;
                    }

                    String formaPagamento = "N/A";
                    int quantidadeParcelas = 0;
                    double valorParcela = 0.0;
                    boolean compraParcelada = false;

                    if (tipo.equalsIgnoreCase("despesa")) {
                        System.out.print("Forma de pagamento - Débito (1) ou Crédito (2): ");
                        int formaOpcao = input.nextInt();
                        input.nextLine();

                        if (formaOpcao == 1) {
                            formaPagamento = "debito";
                            quantidadeParcelas = 1;
                            valorParcela = valor;

                        } else if (formaOpcao == 2) {
                            formaPagamento = "credito";

                            System.out.print("A compra foi parcelada? (s/n): ");
                            String respostaParcelada = input.nextLine();

                            if (respostaParcelada.equalsIgnoreCase("s")) {
                                compraParcelada = true;

                                System.out.print("Quantidade de parcelas: ");
                                quantidadeParcelas = input.nextInt();
                                input.nextLine();

                                if (quantidadeParcelas <= 1) {
                                    System.out.println("Para parcelar, a quantidade de parcelas deve ser maior que 1.");
                                    break;
                                }

                                valorParcela = valor / quantidadeParcelas;
                                System.out.printf("Valor de cada parcela: R$ %.2f\n", valorParcela);

                            } else {
                                compraParcelada = false;
                                quantidadeParcelas = 1;
                                valorParcela = valor;
                            }

                        } else {
                            System.out.println("Forma de pagamento inválida. Operação cancelada.");
                            break;
                        }
                    }

                    System.out.print("Data (DD/MM/AAAA): ");
                    String dataString = input.nextLine();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate data;

                    try {
                        data = LocalDate.parse(dataString, formatter);
                    } catch (Exception e) {
                        System.out.println("Data inválida. Operação cancelada.");
                        break;
                    }

                    System.out.println("\nCategorias disponíveis:");
                    List<Categoria> listaCat = controle.getListaCategorias();

                    for (int i = 0; i < listaCat.size(); i++) {
                        System.out.println((i + 1) + ". " + listaCat.get(i).getNomeCategoria());
                    }

                    System.out.print("Escolha o número da Categoria: ");
                    int escolhaCat = input.nextInt();
                    input.nextLine();

                    if (escolhaCat < 1 || escolhaCat > listaCat.size()) {
                        System.out.println("Categoria inválida. Operação cancelada.");
                        break;
                    }

                    Categoria categoriaEscolhida = listaCat.get(escolhaCat - 1);

                    if (tipo.equalsIgnoreCase("despesa")
                            && formaPagamento.equalsIgnoreCase("credito")
                            && compraParcelada) {

                        List<Lancamento> parcelas = new ArrayList<>();

                        for (int i = 1; i <= quantidadeParcelas; i++) {
                            int idParcela = idLancamento + (i - 1);
                            LocalDate dataParcela = data.plusMonths(i - 1);
                            String descricaoParcela = descricao + " (" + i + "/" + quantidadeParcelas + ")";

                            Lancamento parcela = new Lancamento(
                                    valorParcela,
                                    dataParcela,
                                    tipo,
                                    categoriaEscolhida,
                                    idParcela,
                                    descricaoParcela,
                                    formaPagamento,
                                    quantidadeParcelas,
                                    valorParcela,
                                    i,
                                    idConta
                            );

                            parcelas.add(parcela);
                        }

                        boolean algumaBloqueada = false;

                        for (Lancamento parcela : parcelas) {
                            if (controle.lancamentoPertenceAFaturaPaga(parcela)) {
                                algumaBloqueada = true;
                                break;
                            }
                        }

                        if (algumaBloqueada) {
                            System.out.println("Não foi possível cadastrar a compra parcelada, pois uma ou mais parcelas pertencem a faturas já pagas.");
                        } else {
                            for (Lancamento parcela : parcelas) {
                                controle.cadastrarLancamento(parcela);
                            }

                            System.out.println("\nFeito. Compra parcelada registrada com sucesso!");
                        }

                    } else {
                        int numeroParcela = tipo.equalsIgnoreCase("receita") ? 0 : 1;

                        Lancamento novoLancamento = new Lancamento(
                                valor,
                                data,
                                tipo,
                                categoriaEscolhida,
                                idLancamento,
                                descricao,
                                formaPagamento,
                                quantidadeParcelas,
                                valorParcela,
                                numeroParcela,
                                idConta
                        );

                        if (controle.cadastrarLancamento(novoLancamento)) {
                            System.out.println("\nFeito. Lançamento registrado com sucesso!");
                        } else {
                            System.out.println("Erro ao registrar o lançamento.");
                        }
                    }

                    break;
                }
                    
                case 2:
                    System.out.println("\nEDITAR LANÇAMENTO");
                    if (controle.getListaLancamentos().isEmpty()) {
                        System.out.println("Nenhum lançamento cadastrado.");
                    } else {
                        System.out.println("Lançamentos atuais:");
                        for (Lancamento l : controle.getListaLancamentos()) {
                            System.out.println("ID: " + l.getIdLancamento() + " - " + l.getDescricao() + " (R$ " + l.getValor() + ")");
                        }
                        System.out.print("Digite o ID do lançamento que quer alterar: ");
                        int idEdit = input.nextInt();
                        input.nextLine(); 
                        controle.editarLancamento(idEdit);
                    }
                    break;

                case 3:
                    System.out.println("\nEXCLUIR LANÇAMENTO");
                    if (controle.getListaLancamentos().isEmpty()) {
                        System.out.println("Nenhum lançamento cadastrado.");
                    } else {
                        System.out.println("Lançamentos atuais:");
                        for (Lancamento l : controle.getListaLancamentos()) {
                            System.out.println("ID: " + l.getIdLancamento() + " - " + l.getDescricao() + " (R$ " + l.getValor() + ")");
                        }
                        System.out.print("Digite o ID do lançamento que deseja remover: ");
                        int idExcluir = input.nextInt();
                        input.nextLine();
                        controle.excluirLancamento(idExcluir);
                    }
                    break;
                    
                case 4: {
                    System.out.println("\nRELATÓRIO FINANCEIRO");
                    System.out.println("Escolha o tipo de filtro para o relatório:");
                    System.out.println("1. Relatório por Categoria e Data Início/Fim");
                    System.out.println("2. Relatório por Apenas Categoria");
                    System.out.println("3. Relatório por Apenas Data Início/Fim");
                    System.out.println("4. Relatório por Tipo (Receita/Despesa)");
                    System.out.println("5. Relatório de Fatura por Conta e Mês");
                    System.out.print("Escolha a opção: ");
                    int tipoRelatorio = input.nextInt();
                    input.nextLine(); 

                    Relatorio relatorio = new Relatorio();
                    List<Categoria> listaCat = controle.getListaCategorias(); // Puxa dinamicamente do controle

                    if (tipoRelatorio == 1) {
                        System.out.println("\nCategorias disponíveis:");
                        for (int i = 0; i < listaCat.size(); i++) {
                            System.out.println((i + 1) + ". " + listaCat.get(i).getNomeCategoria());
                        }
                        System.out.print("Escolha o número da Categoria: ");
                        int escolhaFiltro = input.nextInt();
                        input.nextLine();
                        String catFiltro = listaCat.get(escolhaFiltro - 1).getNomeCategoria(); 
                        
                        System.out.print("Data de Início (DD/MM/YYYY): ");
                        String dataInicio = input.nextLine();
                        System.out.print("Data de Fim (DD/MM/YYYY): ");
                        String dataFim = input.nextLine();

                        relatorio.gerarPorCategoriaEData(controle.getListaLancamentos(), catFiltro, dataInicio, dataFim);

                    } else if (tipoRelatorio == 2) {
                        System.out.println("\nCategorias disponíveis:");
                        for (int i = 0; i < listaCat.size(); i++) {
                            System.out.println((i + 1) + ". " + listaCat.get(i).getNomeCategoria());
                        }
                        System.out.print("Escolha o número da Categoria: ");
                        int escolhaFiltro = input.nextInt();
                        input.nextLine();
                        String catFiltro = listaCat.get(escolhaFiltro - 1).getNomeCategoria(); 

                        relatorio.gerarApenasPorCategoria(controle.getListaLancamentos(), catFiltro);
                        
                    } else if (tipoRelatorio == 3) {
                        System.out.print("Data de Início (DD/MM/YYYY): ");
                        String dataInicio = input.nextLine();
                        System.out.print("Data de Fim (DD/MM/YYYY): ");
                        String dataFim = input.nextLine();

                        relatorio.gerarApenasPorData(controle.getListaLancamentos(), dataInicio, dataFim);
                        
                    } else if (tipoRelatorio == 4) {
                        System.out.print("Digite o Tipo (receita/despesa): ");
                        String tipoFiltro = input.nextLine();

                        relatorio.gerarApenasPorTipo(controle.getListaLancamentos(), tipoFiltro);

                    } else if (tipoRelatorio == 5) {
                        if (controle.getListaContas().isEmpty()) {
                            System.out.println("Nenhuma conta cadastrada.");
                            break;
                        }

                        System.out.println("\nContas disponíveis:");

                        for (Conta conta : controle.getListaContas()) {
                            System.out.println(conta.getIdConta() + " - " + conta.getNomeConta());
                        }

                        System.out.print("Escolha o ID da conta: ");
                        int idConta = input.nextInt();
                        input.nextLine();

                        Conta contaEscolhida = controle.buscarContaPorId(idConta);

                        if (contaEscolhida == null) {
                            System.out.println("Conta inválida.");
                            break;
                        }

                        System.out.print("Digite o mês da fatura (1 a 12): ");
                        int mes = input.nextInt();
                        input.nextLine();

                        if (mes < 1 || mes > 12) {
                            System.out.println("Mês inválido.");
                            break;
                        }

                        System.out.print("Digite o ano da fatura: ");
                        int ano = input.nextInt();
                        input.nextLine();

                        relatorio.gerarFaturaPorContaEMes(
                                controle.getListaLancamentos(),
                                contaEscolhida,
                                mes,
                                ano
                        );

                    } else {
                        System.out.println("Opção de filtro inválida.");
                    }
                    break;
                }
                    
                case 5: {
                    System.out.println("\n=== LISTAGEM GERAL DE LANÇAMENTOS ===");
                    List<Lancamento> todosLancamentos = controle.getListaLancamentos();

                    if (todosLancamentos.isEmpty()) {
                        System.out.println("Nenhum lançamento cadastrado no sistema.");
                    } else {
                        for (Lancamento l : todosLancamentos) {
                            System.out.println(l);
                        }
                    }
                    break;
                }
                case 6: {
                    System.out.println("\n=== SALDOS DAS CONTAS ===");

                    if (controle.getListaContas().isEmpty()) {
                        System.out.println("Nenhuma conta cadastrada.");
                        break;
                    }

                    System.out.println("1. Ver saldo de todas as contas");
                    System.out.println("2. Ver saldo de uma conta específica");
                    System.out.print("Escolha a opção: ");

                    int subOpcao = input.nextInt();
                    input.nextLine();

                    if (subOpcao == 1) {
                        System.out.println("\n--- SALDO DE TODAS AS CONTAS ---");
                        double saldoTotal = 0.0;

                        for (Conta conta : controle.getListaContas()) {
                            saldoTotal += conta.getSaldo();

                            System.out.printf(
                                    "%d - %s | Saldo: R$ %.2f\n",
                                    conta.getIdConta(),
                                    conta.getNomeConta(),
                                    conta.getSaldo()
                            );
                        }

                        System.out.println("----------------------------------");
                        System.out.printf("Saldo total: R$ %.2f\n", saldoTotal);

                    } else if (subOpcao == 2) {
                        System.out.println("\nContas disponíveis:");

                        for (Conta conta : controle.getListaContas()) {
                            System.out.println(conta.getIdConta() + " - " + conta.getNomeConta());
                        }

                        System.out.print("\nDigite o ID da conta: ");
                        int idConta = input.nextInt();
                        input.nextLine();

                        Conta contaEscolhida = controle.buscarContaPorId(idConta);

                        if (contaEscolhida == null) {
                            System.out.println("Conta não encontrada.");
                        } else {
                            System.out.println("\n--- SALDO DA CONTA ---");
                            System.out.println("Conta: " + contaEscolhida.getNomeConta());
                            System.out.printf("Saldo: R$ %.2f\n", contaEscolhida.getSaldo());
                        }

                    } else {
                        System.out.println("Opção inválida.");
                    }

                    break;
                }
                case 7: {
                    System.out.println("\nCADASTRAR NOVA CONTA");

                    int idConta = controle.gerarProximoIdConta();

                    System.out.println("Nome da conta: ");
                    String nomeConta = input.nextLine();

                    System.out.println("Saldo inicial: R$ ");
                    String saldoString = input.nextLine().replace(",",".");

                    double saldoInicial;

                    try {
                        saldoInicial = Double.parseDouble(saldoString);
                    } catch (NumberFormatException e) {
                        System.out.println("Saldo inválido. Cadastro cancelado.");
                        break;
                    }

                    Conta novaConta = new Conta(idConta, nomeConta, saldoInicial);

                    if(controle.cadastrarConta(novaConta)){
                        System.out.println("Conta cadastrada com sucesso!");
                    } else {
                        System.out.println("Erro ao cadastrar a conta.");
                    }

                    break;
                }
                case 8: {
                    System.out.println("\n=== FATURAS DAS CONTAS ===");

                    if (controle.getListaContas().isEmpty()) {
                        System.out.println("Nenhuma conta cadastrada.");
                        break;
                    }

                    LocalDate hoje = LocalDate.now();
                    int mesAtual = hoje.getMonthValue();
                    int anoAtual = hoje.getYear();

                    double faturaTotal = 0.0;

                    System.out.println("Próximas faturas em aberto:\n");

                    for (Conta conta : controle.getListaContas()) {
                        int[] proximaFatura = controle.buscarProximaFaturaEmAberto(
                                conta.getIdConta(),
                                mesAtual,
                                anoAtual
                        );

                        if (proximaFatura == null) {
                            System.out.printf(
                                    "%d - %s | Sem fatura em aberto\n",
                                    conta.getIdConta(),
                                    conta.getNomeConta()
                            );
                            continue;
                        }

                        int mesFatura = proximaFatura[0];
                        int anoFatura = proximaFatura[1];

                        double valorFatura = controle.buscarValorFaturaPorContaEMes(
                                conta.getIdConta(),
                                mesFatura,
                                anoFatura
                        );

                        faturaTotal += valorFatura;

                        System.out.printf(
                                "%d - %s | Fatura: %02d/%d | Valor: R$ %.2f | Status: Em aberto\n",
                                conta.getIdConta(),
                                conta.getNomeConta(),
                                mesFatura,
                                anoFatura,
                                valorFatura
                        );
                    }

                    System.out.println("----------------------------------");
                    System.out.printf("Total em aberto exibido: R$ %.2f\n", faturaTotal);

                    break;
                }
                case 9: {
                    System.out.println("\n=== MARCAR FATURA COMO PAGA ===");

                    if (controle.getListaContas().isEmpty()) {
                        System.out.println("Nenhuma conta cadastrada.");
                        break;
                    }

                    System.out.println("\nContas disponíveis:");

                    for (Conta conta : controle.getListaContas()) {
                        System.out.println(conta.getIdConta() + " - " + conta.getNomeConta());
                    }

                    System.out.print("Escolha o ID da conta: ");
                    int idConta = input.nextInt();
                    input.nextLine();

                    Conta contaEscolhida = controle.buscarContaPorId(idConta);

                    if (contaEscolhida == null) {
                        System.out.println("Conta inválida.");
                        break;
                    }

                    System.out.print("Digite o mês da fatura (1 a 12): ");
                    int mes = input.nextInt();
                    input.nextLine();

                    if (mes < 1 || mes > 12) {
                        System.out.println("Mês inválido.");
                        break;
                    }

                    System.out.print("Digite o ano da fatura: ");
                    int ano = input.nextInt();
                    input.nextLine();

                    double valorFatura = controle.buscarValorFaturaPorContaEMes(idConta, mes, ano);

                    if (valorFatura <= 0) {
                        System.out.println("Não existe fatura para essa conta nesse mês.");
                        break;
                    }

                    boolean paga = controle.faturaEstaPaga(idConta, mes, ano);

                    if (paga) {
                        System.out.println("Essa fatura já está marcada como paga.");
                        break;
                    }

                    System.out.printf("Conta: %s\n", contaEscolhida.getNomeConta());
                    System.out.printf("Fatura: %02d/%d\n", mes, ano);
                    System.out.printf("Valor: R$ %.2f\n", valorFatura);

                    System.out.print("Deseja marcar essa fatura como paga? (s/n): ");
                    String confirmar = input.nextLine();

                    if (confirmar.equalsIgnoreCase("s")) {
                        if (controle.pagarFatura(idConta, mes, ano)) {
                            System.out.println("Fatura marcada como paga com sucesso!");
                        } else {
                            System.out.println("Não foi possível marcar a fatura como paga.");
                        }
                    } else {
                        System.out.println("Operação cancelada.");
                    }

                    break;
                }

                case 10: {
                    System.out.println("\n=== ALTERAR PIN ===");

                    System.out.print("Digite o PIN atual: ");
                    String pinAtual = input.nextLine();

                    if (!autenticacao.validarPin(pinAtual)) {
                        System.out.println("PIN atual incorreto. Alteração cancelada.");
                        break;
                    }

                    System.out.print("Digite o novo PIN: ");
                    String novoPin = input.nextLine();

                    if (novoPin.isBlank()) {
                        System.out.println("O novo PIN não pode estar vazio. Alteração cancelada.");
                        break;
                    }

                    System.out.print("Confirme o novo PIN: ");
                    String confirmarPin = input.nextLine();

                    if (!novoPin.equals(confirmarPin)) {
                        System.out.println("Os PINs não coincidem. Alteração cancelada.");
                        break;
                    }

                    autenticacao.setPin(novoPin);

                    System.out.println("PIN alterado com sucesso!");

                    break;
                }

                case 0:
                    System.out.println("Fechando o sistema...");
                    break;
                    
                default:
                    System.out.println("Opção inválida. Digita direito aí.");
                    break;
            }
        }
        
        input.close();
    }
}