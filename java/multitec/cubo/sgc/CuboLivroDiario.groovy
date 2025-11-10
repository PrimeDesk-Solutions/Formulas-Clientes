package multitec.cubo.sgc;

import java.time.LocalDate

import br.com.multiorm.MultiResultSet
import br.com.multitec.utils.DateUtils
import sam.core.variaveis.MDate
import sam.server.samdev.cubo.Cubo
import sam.server.samdev.cubo.CuboAgregador
import sam.server.samdev.cubo.CuboBase;

public class CuboLivroDiario extends CuboBase {
	@Override
	public String getNomeTarefa() {
		return "SGC-Cubo-Livro diário";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("datas", DateUtils.getStartAndEndMonth(MDate.date()));
	}

	@Override
	public Cubo executar() {
		Cubo cubo = Cubo.criar(getDadosDiarioGeral(getIntervaloDatas("datas")));

		cubo.adicionarDimensaoNaLinha("Data");
		cubo.adicionarDimensaoNaLinha("Conta débito");
		cubo.adicionarDimensaoNaLinha("Conta crédito");
		cubo.adicionarDimensaoNaLinha("Histótico");
		cubo.adicionarDimensaoNaColuna("Conta crédito")
		cubo.adicionarMetrica("Valor", CuboAgregador.SOMA);
		
		return cubo; 
	}

	private MultiResultSet getDadosDiarioGeral(LocalDate[] datas){
		def sql = ' SELECT ebb05data as Data, abc10deb.abc10codigo as "Conta débito", abc10cred.abc10codigo as "Conta crédito", ebb05historico as Histótico, ebb05valor as Valor ' +
				  ' FROM Ebb05 ' +
				  ' INNER JOIN Abc10 abc10deb ON ebb05deb = abc10deb.abc10id ' +
				  ' INNER JOIN Abc10 abc10cred ON ebb05cred = abc10cred.abc10id ' +
				  ' WHERE ebb05data BETWEEN :dtInicial AND :dtFinal ' +
				    obterWherePadrao("Ebb05") +
				  ' ORDER BY ebb05data, ebb05num';
		
		return getAcessoAoBanco().buscarMultiResultSet(sql, criarParametroSql("dtInicial", datas[0]), criarParametroSql("dtFinal", datas[1]));
		
	}
}
//meta-sis-eyJkZXNjciI6IlNHQy1DdWJvLUxpdnJvIGRpw6FyaW8iLCJ0aXBvIjoiY3VibyJ9