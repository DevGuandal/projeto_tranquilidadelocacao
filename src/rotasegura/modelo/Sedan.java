package rotasegura.modelo;

import rotasegura.excecoes.DadosInvalidosException;

/** Categoria intermediária. */
public class Sedan extends Veiculo {
    public Sedan(String placa, String modelo, int ano, double km) throws DadosInvalidosException {
        super(placa, modelo, ano, km);
    }
    @Override public String getCategoria() { return "SEDAN"; }
    @Override public double getDiariaBase() { return 150.0; }
    @Override protected double getTaxaSeguro() { return 0.08; }
    @Override public double getCustoManutencao() { return 400.0; }
}
