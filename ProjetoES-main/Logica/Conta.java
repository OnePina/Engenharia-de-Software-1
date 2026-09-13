package Logica;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Conta {

    private int idConta;
    private String nomeConta;
    private double saldo;

    public Conta(int idConta, String nomeConta, double saldo) {
        this.idConta = idConta;
        this.nomeConta = nomeConta;
        this.saldo = saldo;
    }

    public int getIdConta() {
        return idConta;
    }

    public String getNomeConta() {
        return nomeConta;
    }

    public double getSaldo() {
        return saldo;
    }


    public void aplicarLancamento(Lancamento lancamento) {
        if (lancamento.getTipo().equalsIgnoreCase("receita")) {
            saldo += lancamento.getValor();

        } else if (lancamento.getTipo().equalsIgnoreCase("despesa")) {

            if (lancamento.getFormaPagamento().equalsIgnoreCase("credito")) {
                int mes = lancamento.getData().getMonthValue();
                int ano = lancamento.getData().getYear();

                atualizarFatura(
                        lancamento.getIdConta(),
                        mes,
                        ano,
                        lancamento.getValor()
                );

            } else {
                saldo -= lancamento.getValor();
            }
        }
    }

    public void estornarLancamento(Lancamento lancamento) {
        if (lancamento.getTipo().equalsIgnoreCase("receita")) {
            saldo -= lancamento.getValor();

        } else if (lancamento.getTipo().equalsIgnoreCase("despesa")) {

            if (lancamento.getFormaPagamento().equalsIgnoreCase("credito")) {
                int mes = lancamento.getData().getMonthValue();
                int ano = lancamento.getData().getYear();

                atualizarFatura(
                        lancamento.getIdConta(),
                        mes,
                        ano,
                        -lancamento.getValor()
                );

            } else {
                saldo += lancamento.getValor();
            }
        }
    }

    public static void salvarContas(List<Conta> contas) {
        try {
            List<String> linhas = new ArrayList<>();

            for (Conta conta : contas) {
                String linha = conta.getIdConta() + ";" +
                            conta.getNomeConta().replace(";", ",") + ";" +
                            conta.getSaldo();

                linhas.add(linha);
            }

            Files.write(Path.of("contas.txt"), linhas);

        } catch (Exception e) {
            System.out.println("Erro ao salvar contas: " + e.getMessage());
        }
    }

    public static List<Conta> carregarContas() {
        List<Conta> contas = new ArrayList<>();

        try {
            Path caminho = Path.of("contas.txt");

            if (!Files.exists(caminho)) {
                return contas;
            }

            List<String> linhas = Files.readAllLines(caminho);

            for (String linha : linhas) {
                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idConta = Integer.parseInt(partes[0]);
                String nomeConta = partes[1];
                double saldo = Double.parseDouble(partes[2]);

                Conta conta = new Conta(idConta, nomeConta, saldo);

                contas.add(conta);
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar contas: " + e.getMessage());
        }

        return contas;
    }

    // Vai ser usado quando cadastrar, editar, excluir lançamento no credito
    // Para evitar o mesmo problema que tivemos com o saldo de ter de calcular tudo
    // e ainda sim manter uma boa organização do que estamos colocando em cada parte
    // achei melhor criar um novo txt exclusivo das faturas, já que elas poderão
    // ser extendidas por alguns meses devido a parcelas
    public static void atualizarFatura(int idConta, int mes, int ano, double valorAlteracao) {
        try {
            Path caminho = Path.of("faturas.txt");

            List<String> linhas = new ArrayList<>();

            if (Files.exists(caminho)) {
                linhas = Files.readAllLines(caminho);
            }

            boolean encontrou = false;

            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i);

                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idContaArquivo = Integer.parseInt(partes[0]);
                int mesArquivo = Integer.parseInt(partes[1]);
                int anoArquivo = Integer.parseInt(partes[2]);
                double valorFatura = Double.parseDouble(partes[3]);
                boolean paga = Boolean.parseBoolean(partes[4]);

                if (idContaArquivo == idConta && mesArquivo == mes && anoArquivo == ano) {
                    valorFatura += valorAlteracao;

                    if (valorFatura < 0) {
                        valorFatura = 0;
                    }

                    linhas.set(i, idConta + ";" + mes + ";" + ano + ";" + valorFatura + ";" + paga);

                    encontrou = true;
                    break;
                }
            }

            if (!encontrou && valorAlteracao > 0) {
                linhas.add(idConta + ";" + mes + ";" + ano + ";" + valorAlteracao + ";false");
            }

            Files.write(caminho, linhas);

        } catch (Exception e) {
            System.out.println("Erro ao atualizar fatura: " + e.getMessage());
        }
    }

    // Busca o valor da fatura, sem precisar passar por todos os lançamentos
    // basicamente a gente pode pegar o valor de uma fatura que virá em qualquer
    // data.
    public static double buscarValorFatura(int idConta, int mes, int ano) {
        try {
            Path caminho = Path.of("faturas.txt");

            if (!Files.exists(caminho)) {
                return 0.0;
            }

            List<String> linhas = Files.readAllLines(caminho);

            for (String linha : linhas) {
                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idContaArquivo = Integer.parseInt(partes[0]);
                int mesArquivo = Integer.parseInt(partes[1]);
                int anoArquivo = Integer.parseInt(partes[2]);
                double valorFatura = Double.parseDouble(partes[3]);

                if (idContaArquivo == idConta && mesArquivo == mes && anoArquivo == ano) {
                    return valorFatura;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar fatura: " + e.getMessage());
        }

        return 0.0;
    }

    // Apenas verifica se a fatura está paga ou não
    // ex: 1;6;2026;300.00;false
    // significa que a conta com o id 1 tem uma fatura
    // do mês 6 de 2026 que ainda não está marcada/ainda
    // não foi paga.
    public static boolean faturaEstaPaga(int idConta, int mes, int ano) {
        try {
            Path caminho = Path.of("faturas.txt");

            if (!Files.exists(caminho)) {
                return false;
            }

            List<String> linhas = Files.readAllLines(caminho);

            for (String linha : linhas) {
                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idContaArquivo = Integer.parseInt(partes[0]);
                int mesArquivo = Integer.parseInt(partes[1]);
                int anoArquivo = Integer.parseInt(partes[2]);
                boolean paga = Boolean.parseBoolean(partes[4]);

                if (idContaArquivo == idConta && mesArquivo == mes && anoArquivo == ano) {
                    return paga;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao verificar fatura: " + e.getMessage());
        }

        return false;
    }

    // Para ter um melhor controle das finanças é sempre bom saber o que foi pago
    // utilizamos essa função apenas para alterar o valor de false para true
    // quando uma fatura de um determinado mês for paga. APENAS CRÉDITO!
    public static void marcarFaturaComoPaga(int idConta, int mes, int ano) {
        try {
            Path caminho = Path.of("faturas.txt");

            if (!Files.exists(caminho)) {
                return;
            }

            List<String> linhas = Files.readAllLines(caminho);

            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i);

                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idContaArquivo = Integer.parseInt(partes[0]);
                int mesArquivo = Integer.parseInt(partes[1]);
                int anoArquivo = Integer.parseInt(partes[2]);
                double valorFatura = Double.parseDouble(partes[3]);

                if (idContaArquivo == idConta && mesArquivo == mes && anoArquivo == ano) {
                    linhas.set(i, idConta + ";" + mes + ";" + ano + ";" + valorFatura + ";true");
                    break;
                }
            }

            Files.write(caminho, linhas);

        } catch (Exception e) {
            System.out.println("Erro ao marcar fatura como paga: " + e.getMessage());
        }
    }

    public static int[] buscarProximaFaturaEmAberto(int idConta, int mesInicial, int anoInicial) {
        try {
            Path caminho = Path.of("faturas.txt");

            if (!Files.exists(caminho)) {
                return null;
            }

            List<String> linhas = Files.readAllLines(caminho);

            int referenciaInicial = anoInicial * 12 + mesInicial;

            int menorReferenciaEncontrada = Integer.MAX_VALUE;
            int mesEncontrado = 0;
            int anoEncontrado = 0;

            for (String linha : linhas) {
                if (linha.isBlank()) {
                    continue;
                }

                String[] partes = linha.split(";");

                int idContaArquivo = Integer.parseInt(partes[0]);
                int mesArquivo = Integer.parseInt(partes[1]);
                int anoArquivo = Integer.parseInt(partes[2]);
                double valorFatura = Double.parseDouble(partes[3]);
                boolean paga = Boolean.parseBoolean(partes[4]);

                int referenciaArquivo = anoArquivo * 12 + mesArquivo;

                if (idContaArquivo == idConta
                        && valorFatura > 0
                        && !paga
                        && referenciaArquivo >= referenciaInicial
                        && referenciaArquivo < menorReferenciaEncontrada) {

                    menorReferenciaEncontrada = referenciaArquivo;
                    mesEncontrado = mesArquivo;
                    anoEncontrado = anoArquivo;
                }
            }

            if (mesEncontrado == 0) {
                return null;
            }

            return new int[] {mesEncontrado, anoEncontrado};

        } catch (Exception e) {
            System.out.println("Erro ao buscar próxima fatura em aberto: " + e.getMessage());
        }

        return null;
    }

    @Override
    public String toString() {
        return idConta + " - " + nomeConta +
                " | Saldo: R$ " + saldo;
    }
}