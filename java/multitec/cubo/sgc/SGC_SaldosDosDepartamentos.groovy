package multitec.cubo.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multiorm.MultiResultSet;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import sam.core.variaveis.MDate
import sam.model.entities.eb.Ebb01;
import sam.server.samdev.cubo.Cubo;
import sam.server.samdev.cubo.CuboAgregador;
import sam.server.samdev.cubo.CuboBase;

public class SGC_SaldosDosDepartamentos extends CuboBase {
	@Override
	public String getNomeTarefa() {
		return "SGC-Cubo-Saldos dos Departamentos";
	}

	@Override
	public Cubo executar() {
		LocalDate data = DateUtils.parseDate("01" + getString("anoMes"))
		Cubo cubo = Cubo.criar(getSaldosDepartamentosGeral(data, getListLong("departamentos")));
		
		if(cubo.getDados().size() == 0) throw new ValidacaoException("Não foram encontrados saldos no período informado.");
		
		cubo.adicionarDimensaoNaLinha("Código");
		cubo.adicionarDimensaoNaLinha("Nome");
		cubo.adicionarMetrica("Valor", CuboAgregador.SOMA);
		cubo.adicionarMetrica("Valor", CuboAgregador.QUANTIDADE);
		
		return cubo;
	}
	

	private MultiResultSet getSaldosDepartamentosGeral(LocalDate date, List<Long> idsDepartamento) {
		String whereDepto = idsDepartamento != null ? " AND ebb01depto IN (:idsDepartamento) " : "";
		return getSession().createQuery(
				" SELECT abb11.abb11codigo as \"Código\", abb11.abb11nome as \"Nome\", ebb01valor as \"Valor\"" +
				" FROM Ebb01 " +
				" INNER JOIN Abb11 abb11 ON ebb01depto = abb11.abb11id " +
				" WHERE ebb01ano = :ano AND ebb01mes = :mes" +
				whereDepto + " " + getSamWhere().getWherePadrao("AND", Ebb01.class) +
				" ORDER BY abb11.abb11codigo")
			.setParameter("ano", date.getYear())
			.setParameter("mes", date.getMonthValue())
			.setParameter("idsDepartamento", idsDepartamento)
			.getMultiResultSet();
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("anoMes", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		return Utils.map("filtros", filtrosDefault);
	}
}
//meta-sis-eyJkZXNjciI6IlNHQy1DdWJvLVNhbGRvcyBkb3MgRGVwYXJ0YW1lbnRvcyIsInRpcG8iOiJjdWJvIn0=