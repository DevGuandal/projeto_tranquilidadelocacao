package rotasegura.modelo;

import rotasegura.excecoes.DadosInvalidosException;

/** Categoria premium: diária, seguro e manutenção mais caros. */
public class SUV extends Veiculo {
    public SUV(String placa, String modelo, int ano, double km) throws DadosInvalidosException {
        super(placa, modelo, ano, km);
    }
    @Override public String getCategoria() { return "SUV"; }
    @Override public double getDiariaBase() { return 220.0; }
    @Override protected double getTaxaSeguro() { return 0.12; }
    @Override public double getCustoManutencao() { return 650.0; }
}
