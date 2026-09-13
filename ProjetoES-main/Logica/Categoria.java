package Logica;

public class Categoria {

    private String nomeCategoria;
    private int idCategoria;

    // Construtor estrutural da Categoria
    public Categoria(String nomeCategoria, int idCategoria) {
        this.nomeCategoria = nomeCategoria;
        this.idCategoria = idCategoria;
    }

    // Getters e Setters
    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    @Override
    public String toString() {
        return nomeCategoria;
    }
}