package rotasegura.modelo;

import rotasegura.excecoes.DadosInvalidosException;

/** Categoria econômica: diária baixa, seguro e manutenção mais baratos. */
public class Popular extends Veiculo {
    public Popular(String placa, String modelo, int ano, double km) throws DadosInvalidosException {
        super(placa, modelo, ano, km);
    }
    @Override public String getCategoria() { return "POPULAR"; }
    @Override public double getDiariaBase() { return 90.0; }
    @Override protected double getTaxaSeguro() { return 0.05; }
    @Override public double getCustoManutencao() { return 250.0; }
}
