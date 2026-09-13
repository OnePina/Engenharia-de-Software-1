package Logica;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Relatorio {

    private final DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Auxiliar para converter String de entrada do console para LocalDate de forma segura
    private LocalDate converterData(String dataTexto) {
        try {
            return LocalDate.parse(dataTexto, formatador);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // 1. Relatório por Categoria e Data Início/Fim
    public void gerarPorCategoriaEData(List<Lancamento> lancamentos, String categoria, String dataInicio, String dataFim) {
        System.out.println("\n--- RELATÓRIO: CATEGORIA (" + categoria + ") E PERÍODO [" + dataInicio + " - " + dataFim + "] ---");
        
        LocalDate inicio = converterData(dataInicio);
        LocalDate fim = converterData(dataFim);

        if (inicio == null || fim == null) {
            System.out.println("Erro: Formato de datas inválido para o filtro.");
            return;
        }

        int encontrados = 0;
        for (Lancamento l : lancamentos) {
            boolean bateCategoria = l.getCategoria().getNomeCategoria().equalsIgnoreCase(categoria);
            boolean bateData = (!l.getData().isBefore(inicio)) && (!l.getData().isAfter(fim));

            if (bateCategoria && bateData) {
                System.out.println(l);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Nenhum lançamento encontrado para os filtros aplicados.");
        }
    }

    // 2. Relatório por Apenas Categoria
    public void gerarApenasPorCategoria(List<Lancamento> lancamentos, String categoria) {
        System.out.println("\n--- RELATÓRIO: APENAS CATEGORIA (" + categoria + ") ---");
        
        int encontrados = 0;
        for (Lancamento l : lancamentos) {
            if (l.getCategoria().getNomeCategoria().equalsIgnoreCase(categoria)) {
                System.out.println(l);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Nenhum lançamento encontrado para esta categoria.");
        }
    }

    // 3. Relatório por Apenas Data Início/Fim
    public void gerarApenasPorData(List<Lancamento> lancamentos, String dataInicio, String dataFim) {
        System.out.println("\n--- RELATÓRIO: APENAS PERÍODO [" + dataInicio + " - " + dataFim + "] ---");
        
        LocalDate inicio = converterData(dataInicio);
        LocalDate fim = converterData(dataFim);

        if (inicio == null || fim == null) {
            System.out.println("Erro: Formato de datas inválido para o filtro.");
            return;
        }

        int encontrados = 0;
        for (Lancamento l : lancamentos) {
            if ((!l.getData().isBefore(inicio)) && (!l.getData().isAfter(fim))) {
                System.out.println(l);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Nenhum lançamento encontrado para o período especificado.");
        }
    }

    // 4. Relatório por Tipo (Receita/Despesa)
    public void gerarApenasPorTipo(List<Lancamento> lancamentos, String tipo) {
        System.out.println("\n--- RELATÓRIO: APENAS TIPO (" + tipo.toUpperCase() + ") ---");
        
        int encontrados = 0;
        for (Lancamento l : lancamentos) {
            if (l.getTipo().equalsIgnoreCase(tipo)) {
                System.out.println(l);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Nenhum lançamento encontrado para o tipo '" + tipo + "'.");
        }
    }

    public void gerarFaturaPorContaEMes(List<Lancamento> lancamentos, Conta conta, int mes, int ano) {
        System.out.println("\n--- RELATÓRIO DE FATURA ---");
        System.out.println("Conta: " + conta.getNomeConta());
        System.out.printf("Mês/Ano: %02d/%d\n", mes, ano);

        boolean faturaPaga = Conta.faturaEstaPaga(conta.getIdConta(), mes, ano);
        System.out.println("Status: " + (faturaPaga ? "Paga" : "Em aberto"));

        double total = 0.0;
        int encontrados = 0;

        for (Lancamento l : lancamentos) {
            boolean mesmaConta = l.getIdConta() == conta.getIdConta();
            boolean ehDespesa = l.getTipo().equalsIgnoreCase("despesa");
            boolean ehCredito = l.getFormaPagamento().equalsIgnoreCase("credito");
            boolean mesmoMes = l.getData().getMonthValue() == mes;
            boolean mesmoAno = l.getData().getYear() == ano;

            if (mesmaConta && ehDespesa && ehCredito && mesmoMes && mesmoAno) {
                System.out.printf(
                        "- %s | R$ %.2f | Data: %s",
                        l.getDescricao(),
                        l.getValor(),
                        l.getData()
                );

                if (l.getQuantidadeParcelas() > 1) {
                    System.out.printf(
                            " | Parcela %d/%d",
                            l.getNumeroParcela(),
                            l.getQuantidadeParcelas()
                    );
                }

                System.out.println();

                total += l.getValor();
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Nenhum lançamento de crédito encontrado para esta fatura.");
        } else {
            System.out.println("----------------------------------");
            System.out.printf("Total da fatura: R$ %.2f\n", total);
        }
    }
}