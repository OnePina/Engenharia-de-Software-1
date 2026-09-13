package Logica;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;

public class Autenticacao {

    private String pin;

    public Autenticacao(String pin) {
        this.pin = pin;
    }

    public boolean validarPin(String pinDigitado) {
        return this.pin.equals(aplicarHash(pinDigitado));
    }

    public void setPin(String novoPin) {
        this.pin = aplicarHash(novoPin);
        salvarPin();
    }

    public void salvarPin() {
        try {
            Files.write(Path.of("sistema.txt"), List.of(pin));
        } catch (Exception e) {
            System.out.println("Erro ao salvar PIN: " + e.getMessage());
        }
    }

    public static boolean existePinCadastrado() {
        try {
            Path caminho = Path.of("sistema.txt");

            if (!Files.exists(caminho)) {
                return false;
            }

            List<String> linhas = Files.readAllLines(caminho);

            if (linhas.isEmpty() || linhas.get(0).trim().isEmpty()) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Autenticacao criarNovoPin(String novoPin) {
        Autenticacao autenticacao = new Autenticacao("");
        autenticacao.setPin(novoPin);
        return autenticacao;
    }

    public static Autenticacao carregarPin() {
        try {
            if (!existePinCadastrado()) {
                return null;
            }

            Path caminho = Path.of("sistema.txt");
            List<String> linhas = Files.readAllLines(caminho);

            return new Autenticacao(linhas.get(0).trim());

        } catch (Exception e) {
            System.out.println("Erro ao carregar PIN: " + e.getMessage());
            return null;
        }
    }

    private String aplicarHash(String pinOriginal) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinOriginal.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash do PIN.");
        }
    }
}