package Logica;

import java.time.LocalDate;

public class Lancamento {

    private double valor;
    private LocalDate data;
    private String tipo;
    private Categoria categoria;
    private int idLancamento;
    private String descricao;
    private String formaPagamento;
    private int quantidadeParcelas;
    private double valorParcela;
    private int numeroParcela;
    private int idConta;

    public Lancamento(
                double valor,
                LocalDate data,
                String tipo,
                Categoria categoria,
                int idLancamento,
                String descricao,
                String formaPagamento,
                int quantidadeParcelas,
                double valorParcela,
                int numeroParcela,
                int idConta) 

        {
            setValor(valor);
            setData(data);
            setTipo(tipo);
            setCategoria(categoria);
            setIdLancamento(idLancamento);
            setDescricao(descricao);
            setFormaPagamento(formaPagamento);
            setQuantidadeParcelas(quantidadeParcelas);
            setValorParcela(valorParcela);
            setNumeroParcela(numeroParcela);
            setIdConta(idConta);
        }

    // Getters
    public int getNumeroParcela(){
        return numeroParcela;
    }
    
    public int getIdConta() {
        return idConta;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public String getTipo() {
        return tipo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getIdLancamento() {
        return idLancamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public double getValorParcela() {
        return valorParcela;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    // Setters públicos para campos que podem ser editados
    public void setNumeroParcela(int numeroParcela) {
        if (this.tipo.equalsIgnoreCase("receita")) {
            this.numeroParcela = 0;
            return;
        }

        if (this.formaPagamento.equalsIgnoreCase("credito")) {
            if (numeroParcela <= 0 || numeroParcela > this.quantidadeParcelas) {
                throw new IllegalArgumentException("Número da parcela inválido.");
            }

            this.numeroParcela = numeroParcela;
        } else {
            this.numeroParcela = 1;
        }
    }

    public void setValor(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Não é permitido um valor menor ou igual a 0.\n");
        }

        this.valor = valor;
    }

    public void setData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória.\n");
        }

        this.data = data;
    }

    public void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.\n");
        }

        this.categoria = categoria;
    }

    public void setDescricao(String descricao) {
        if (descricao == null) {
            this.descricao = "";
        } else {
            this.descricao = descricao;
        }
    }

    // Setters privados para campos que não devem ser alterados depois do cadastro

    private void setTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Tipo é obrigatório.\n");
        }

        if (!tipo.equalsIgnoreCase("receita") && !tipo.equalsIgnoreCase("despesa")) {
            throw new IllegalArgumentException("Classificação incorreta. Use receita ou despesa.\n");
        }

        this.tipo = tipo.toLowerCase();
    }

    private void setIdLancamento(int idLancamento) {
        if (idLancamento <= 0) {
            throw new IllegalArgumentException("ID inválido.\n");
        }

        this.idLancamento = idLancamento;
    }

    private void setIdConta(int idConta) {
        if (idConta <= 0) {
            throw new IllegalArgumentException("Conta Inválida.");
        }
        this.idConta = idConta;
    }

    // Forma de pagamento para caso seja débito ou crédito
    // Se for débito o sistema acontecerá normalmente
    // Se for crédito irá para a lógica de crédito


    public void setFormaPagamento(String formaPagamento) {
        if (this.tipo.equalsIgnoreCase("receita")) {
            this.formaPagamento = "N/A";
            return;
        }

        if (formaPagamento == null || formaPagamento.isBlank()) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória para despesas.");
        }

        String fp = formaPagamento.trim().toLowerCase()
                .replace("é", "e")
                .replace("édito", "edito");

        if (!fp.equals("credito") && !fp.equals("debito")) {
            throw new IllegalArgumentException("Forma de pagamento inválida. Use crédito ou débito.");
        }

        this.formaPagamento = fp;
    }

    public void setQuantidadeParcelas(int quantidadeParcelas) {
        if (this.tipo.equalsIgnoreCase("receita")) {
            this.quantidadeParcelas = 0;
            return;
        }

        if (this.formaPagamento.equalsIgnoreCase("credito")) {
            if (quantidadeParcelas <= 0) {
                throw new IllegalArgumentException("Quantidade de parcelas inválida.");
            }

            this.quantidadeParcelas = quantidadeParcelas;
        } else {
            this.quantidadeParcelas = 1;
        }
    }

    public void setValorParcela(double valorParcela) {
        if (this.tipo.equalsIgnoreCase("receita")) {
            this.valorParcela = 0.0;
            return;
        }

        if (this.formaPagamento.equalsIgnoreCase("credito")) {
            if (valorParcela <= 0) {
                throw new IllegalArgumentException("Valor da parcela inválido.");
            }

            this.valorParcela = valorParcela;
        } else {
            this.valorParcela = this.valor;
        }
    }

    @Override
    public String toString() {
        String textoParcela = "";

        if (formaPagamento.equalsIgnoreCase("credito") && quantidadeParcelas > 1) {
            textoParcela = " | Parcela: " + numeroParcela + "/" + quantidadeParcelas;
        }

        return "ID: " + idLancamento +
                " | Conta ID: " + idConta +
                " | Tipo: " + tipo +
                " | Valor: R$ " + valor +
                " | Data: " + data +
                " | Categoria: " + categoria.getNomeCategoria() +
                " | Forma de pagamento: " + formaPagamento +
                textoParcela +
                " | Descrição: " + descricao;
    }
}