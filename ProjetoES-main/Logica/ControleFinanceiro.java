package Logica;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class ControleFinanceiro {
    private List<Lancamento> arrayLancamentos = new ArrayList<>();
    private List<Categoria> arrayCategorias = new ArrayList<>();
    private List<Conta> contas = new ArrayList<>();

    private Scanner scanner;

    public ControleFinanceiro(Scanner scanner) {
        this.scanner = scanner;
        arrayCategorias.add(new Categoria("Alimentação", 1));
        arrayCategorias.add(new Categoria("Moradia", 2));
        arrayCategorias.add(new Categoria("Transporte", 3));
        arrayCategorias.add(new Categoria("Saúde", 4));
        arrayCategorias.add(new Categoria("Lazer", 5));
        arrayCategorias.add(new Categoria("Salário/Renda", 6));
        arrayCategorias.add(new Categoria("Educação", 7));
        arrayCategorias.add(new Categoria("Assinaturas/Serviços", 8));
        arrayCategorias.add(new Categoria("Vestuário", 9));
        arrayCategorias.add(new Categoria("Investimentos", 10));
        arrayCategorias.add(new Categoria("Outros", 11));
        
        contas = Conta.carregarContas();
        carregarDados();
    }
    
    public List<Conta> getListaContas(){
        return contas;
    }

    public List<Lancamento> getListaLancamentos() {
        return arrayLancamentos;
    }

    public List<Categoria> getListaCategorias() {
    return arrayCategorias;
}

    public Categoria buscarIDCategoria(int idCategoria){
        for (Categoria categoria : arrayCategorias) {
                if (idCategoria == categoria.getIdCategoria()) {
                    return categoria;   
                }
            }
        return null;
    }

    public Conta buscarContaPorId(int idConta) {
        for (Conta conta : contas) {
            if (conta.getIdConta() == idConta) {
                return conta;
            }
        }

        return null;
    }

    public boolean cadastrarConta(Conta conta) {
        if (conta == null) {
            return false;
        }

        contas.add(conta);
        Conta.salvarContas(contas);;
        return true;
    }

    public int gerarProximoIdConta() {
        if (contas.isEmpty()) {
            return 1;
        }

        return contas.get(contas.size() - 1).getIdConta() + 1;
    }

    public boolean cadastrarLancamento(Lancamento lancamento) {
        if (lancamento == null) {
            return false;
        }

        Conta conta = buscarContaPorId(lancamento.getIdConta());

        if (conta == null) {
            System.out.println("Conta não encontrada. Lançamento cancelado.");
            return false;
        }

        if (lancamentoPertenceAFaturaPaga(lancamento)) {
            System.out.println("Não é possível cadastrar este lançamento, pois ele pertence a uma fatura já paga.");
            return false;
        }

        conta.aplicarLancamento(lancamento);

        arrayLancamentos.add(lancamento);

        salvarDados();

        Conta.salvarContas(contas);

        return true;
    }
    
    public Lancamento buscarLancamento(int idLancamento){
        
        for (Lancamento lancamento : arrayLancamentos) {
            if (idLancamento == lancamento.getIdLancamento()) {
                return lancamento;
            }
        }
        return null;
    }

    public double calcularSaldoTotal() {
        double saldoTotal = 0;

        for (Conta conta : contas) {
            saldoTotal += conta.getSaldo();
        }

        return saldoTotal;
    }

    public void editarLancamento(int idLancamento) {
        if (arrayLancamentos.isEmpty()) {
            System.out.println("Não existem lançamentos cadastrados para alterar.");
            return;
        }

        Lancamento lancamento = buscarLancamento(idLancamento);       
        if (lancamento == null) {
            System.out.println("Lançamento não existe.");
            return;
        } else {

            if (lancamentoPertenceAFaturaPaga(lancamento)) {
                System.out.println("Não é possível editar este lançamento, pois ele pertence a uma fatura já paga.");
                return;
            }

            System.out.println("\nLançamento encontrado: " + lancamento.getDescricao());

            Conta contaOriginal = buscarContaPorId(lancamento.getIdConta());

            if (contaOriginal != null) {
                contaOriginal.estornarLancamento(lancamento);
            }

            // Editar valor com correção da vírgula
            System.out.println("\nValor atual: R$ " + lancamento.getValor());
            System.out.print("Digite o novo valor (ou pressione ENTER para manter): ");
            // Substitui vírgula por ponto para não quebrar a conversão
            String valorString = scanner.nextLine().replace(",", "."); 

            if (!valorString.isBlank()) {
                try {
                    double novoValor = Double.parseDouble(valorString);

                    if (novoValor <= 0) {
                        System.out.println("Valor inválido. Mantendo valor antigo.");
                    } else {
                        lancamento.setValor(novoValor);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Valor inválido. Mantendo valor antigo.");
                }
            }

            // Editar data
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            System.out.println("\nData atual: " + lancamento.getData().format(formatador));

            boolean ehDespesaCredito = lancamento.getTipo().equalsIgnoreCase("despesa")
                    && lancamento.getFormaPagamento().equalsIgnoreCase("credito");

            if (ehDespesaCredito) {
                System.out.println("A data de lançamento no crédito não pode ser alterada.");
                System.out.println("Para corrigir a data, exclua o lançamento e cadastre novamente.");
            } else {
                System.out.print("Digite a nova data DD/MM/AAAA (ou pressione ENTER para manter): ");
                String dataString = scanner.nextLine();

                if (!dataString.isBlank()) {
                    try {
                        LocalDate novaData = LocalDate.parse(dataString, formatador);
                        lancamento.setData(novaData);
                    } catch (DateTimeParseException e) {
                        System.out.println("Data inválida. Mantendo data antiga.");
                    }
                }
            }
            
            // Editar Categoria
            System.out.println("\nCategoria atual: " + lancamento.getCategoria().getNomeCategoria());
            System.out.println("Categorias disponíveis:");
            arrayCategorias.forEach(Categoria -> System.out.println(Categoria.getIdCategoria() + " - " + Categoria.getNomeCategoria()));
            
            System.out.print("Selecione a nova categoria pelo número (ou pressione ENTER para manter): ");
            String idStringCategoria = scanner.nextLine();
                
            if (!idStringCategoria.isBlank()) {
                try {
                    int novaCategoriaID = Integer.parseInt(idStringCategoria);
                    Categoria novaCategoria = buscarIDCategoria(novaCategoriaID);
                    
                    if (novaCategoria == null) {
                        System.out.println("Nova categoria não encontrada. Mantendo a antiga.");
                    } else {
                        lancamento.setCategoria(novaCategoria);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ID inválido. Mantendo categoria antiga.");
                }
            }
            
            // Editar descrição
            System.out.println("\nDescrição atual: " + lancamento.getDescricao());
            System.out.print("Digite a nova descrição (ou pressione ENTER para manter): ");
            String novaDescricao = scanner.nextLine();

            if (!novaDescricao.isBlank()) {
                lancamento.setDescricao(novaDescricao);
            } else {
                System.out.println("Mantendo descrição antiga.");
            }

            Conta contaAtualizada = buscarContaPorId(lancamento.getIdConta());

            if (contaAtualizada != null) {
                contaAtualizada.aplicarLancamento(lancamento);
            }

            System.out.println("\nLançamento atualizado com sucesso.");

            salvarDados();
            Conta.salvarContas(contas);
        }
    }
    
    public void excluirLancamento(int idLancamento) {
        Lancamento lancamentoParaExcluir = buscarLancamento(idLancamento);

        if (lancamentoParaExcluir == null) {
            System.out.println("Não foi possível localizar o lançamento.");
            return;
        }

        if (lancamentoPertenceAFaturaPaga(lancamentoParaExcluir)) {
            System.out.println("Não é possível excluir este lançamento, pois ele pertence a uma fatura já paga.");
            return;
        }

        String resposta = "";

        while (!resposta.equalsIgnoreCase("s") && !resposta.equalsIgnoreCase("n")) {
            System.out.println("Deseja realmente excluir este lançamento? (s/n)");
            resposta = scanner.nextLine();

            if (!resposta.equalsIgnoreCase("s") && !resposta.equalsIgnoreCase("n")) {
                System.out.println("Opção inválida. Digite apenas 's' para sim ou 'n' para não.");
            }
        }

        if (resposta.equalsIgnoreCase("s")) {
            Conta conta = buscarContaPorId(lancamentoParaExcluir.getIdConta());

            if (conta != null) {
                conta.estornarLancamento(lancamentoParaExcluir);
            }

            arrayLancamentos.remove(lancamentoParaExcluir);

            salvarDados();
            Conta.salvarContas(contas);

            System.out.println("Lançamento excluído com sucesso.");
            return;

        } else if (resposta.equalsIgnoreCase("n")) {
            System.out.println("Exclusão de lançamento cancelada.");
            return;
        }
    }

    // Método para salvar a lista atual no arquivo lancamentos.txt
    public void salvarDados() {
        List<String> linhas = new ArrayList<>();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Lancamento l : arrayLancamentos) {
            // Unimos os atributos por ";" para facilitar a leitura depois
            String linha = l.getIdLancamento() + ";" +
                            l.getIdConta() + ";" +
                            l.getValor() + ";" +
                            l.getData().format(formatador) + ";" +
                            l.getTipo() + ";" +
                            l.getCategoria().getIdCategoria() + ";" +
                            l.getDescricao().replace(";", ",") + ";" +
                            l.getFormaPagamento() + ";" +
                            l.getQuantidadeParcelas() + ";" +
                            l.getValorParcela() + ";" +
                            l.getNumeroParcela();
            linhas.add(linha);
        }
        
        try {
            Files.write(Path.of("lancamentos.txt"), linhas);
        } catch (Exception e) {
            System.out.println("Erro ao salvar os dados no arquivo: " + e.getMessage());
        }
    }

    // Método para carregar os dados do arquivo para a memória RAM
    public void carregarDados() {
        try {
            Path caminho = Path.of("lancamentos.txt");
            if (!Files.exists(caminho)) {
                return; // Se o arquivo não existir (primeira execução), ignora
            }
            
            List<String> linhas = Files.readAllLines(caminho);
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (String linha : linhas) {
                if (linha.isBlank()) continue;
                
                String[] partes = linha.split(";");
                
                int id = Integer.parseInt(partes[0]);
                int idConta = Integer.parseInt(partes[1]);
                double valor = Double.parseDouble(partes[2]);
                LocalDate data = LocalDate.parse(partes[3], formatador);
                String tipo = partes[4];
                int idCat = Integer.parseInt(partes[5]);

                String descricao = (partes.length > 6) ? partes[6] : "";

                String formaPagamento;
                if (partes.length > 7) {
                    formaPagamento = partes[7];
                } else {
                    formaPagamento = tipo.equalsIgnoreCase("despesa") ? "debito" : "N/A";
                }

                int quantidadeParcelas = (partes.length > 8) ? Integer.parseInt(partes[8]) : 1;
                double valorParcela = (partes.length > 9) ? Double.parseDouble(partes[9]) : valor;
                int numeroParcela = (partes.length > 10) ? Integer.parseInt(partes[10]) : 1;

                Categoria categoria = buscarIDCategoria(idCat);

                Lancamento lancamento = new Lancamento(
                        valor,
                        data,
                        tipo,
                        categoria,
                        id,
                        descricao,
                        formaPagamento,
                        quantidadeParcelas,
                        valorParcela,
                        numeroParcela,
                        idConta
                );

                arrayLancamentos.add(lancamento);
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar os dados salvos: " + e.getMessage());
        }
    }

    public double calcularFaturaPorContaEMes(int idConta, int mes, int ano) {
        double totalFatura = 0.0;

        for (Lancamento lancamento : arrayLancamentos) {
            boolean mesmaConta = lancamento.getIdConta() == idConta;
            boolean ehDespesa = lancamento.getTipo().equalsIgnoreCase("despesa");
            boolean ehCredito = lancamento.getFormaPagamento().equalsIgnoreCase("credito");
            boolean mesmoMes = lancamento.getData().getMonthValue() == mes;
            boolean mesmoAno = lancamento.getData().getYear() == ano;

            if (mesmaConta && ehDespesa && ehCredito && mesmoMes && mesmoAno) {
                totalFatura += lancamento.getValor();
            }
        }

        return totalFatura;
    }

    public boolean pagarFatura(int idConta, int mes, int ano) {
        Conta conta = buscarContaPorId(idConta);

        if (conta == null) {
            System.out.println("Conta não encontrada.");
            return false;
        }

        double valorFatura = Conta.buscarValorFatura(idConta, mes, ano);

        if (valorFatura <= 0) {
            System.out.println("Não existe fatura para esse mês.");
            return false;
        }

        if (Conta.faturaEstaPaga(idConta, mes, ano)) {
            System.out.println("Essa fatura já está paga.");
            return false;
        }

        Conta.marcarFaturaComoPaga(idConta, mes, ano);

        return true;
    }

    public double buscarValorFaturaPorContaEMes(int idConta, int mes, int ano) {
        return Conta.buscarValorFatura(idConta, mes, ano);
    }

    public boolean faturaEstaPaga(int idConta, int mes, int ano) {
        return Conta.faturaEstaPaga(idConta, mes, ano);
    }

    public int[] buscarProximaFaturaEmAberto(int idConta, int mesInicial, int anoInicial) {
        return Conta.buscarProximaFaturaEmAberto(idConta, mesInicial, anoInicial);
    }

    public boolean lancamentoPertenceAFaturaPaga(Lancamento lancamento) {
    if (lancamento == null) {
        return false;
    }

        if (!lancamento.getTipo().equalsIgnoreCase("despesa")) {
            return false;
        }

        if (!lancamento.getFormaPagamento().equalsIgnoreCase("credito")) {
                return false;
            }

            int idConta = lancamento.getIdConta();
            int mes = lancamento.getData().getMonthValue();
            int ano = lancamento.getData().getYear();

            return Conta.faturaEstaPaga(idConta, mes, ano);
    }
}