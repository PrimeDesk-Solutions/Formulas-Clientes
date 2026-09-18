package multitec.cubo.sgc;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.stream.Collectors;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Aba01;
import sam.model.entities.eb.Eba10;
import sam.server.samdev.cubo.Cubo;
import sam.server.samdev.cubo.CuboAgregador;
import sam.server.samdev.cubo.CuboBase;
import sam.server.samdev.cubo.CuboColuna;
import sam.server.samdev.cubo.CuboColunaTipo;

public class SGC_AgrupamentoCubo extends CuboBase {
	@Override
	public String getNomeTarefa() {
		return "SGC-Cubo-Agrupamento de Contas";
	}

	@Override
	public Cubo executar() {
		LocalDate anoMes = getLocalDate("anoMes");
		Integer detalhamento = getInteger("detalhamento");
		
		String estrutura = getSession().createCriteria(Aba01.class)
									   .addFields("aba01conteudo")
									   .addWhere(Criterions.eq("aba01aplic", "EB"))
									   .addWhere(Criterions.eq("aba01param", "ESTRCODAGRUP"))
									   .addWhere(getSamWhere().getCritPadrao(Aba01.class))
									   .get(ColumnType.STRING);
		
		List<TableMap> contas = new ArrayList<>(); 
		List<Integer> grupos = new ArrayList<>();
		
		getEstrutura(estrutura, grupos, 0);
		
		Query query = getSession().createQuery(" SELECT DISTINCT eba10codigo, eba10nome, abc10id, abc10codigo, abc10nome " + 
											   " FROM Eba10" + 
											   " LEFT JOIN Eba1001 ON eba1001agrup = eba10id" + 
											   " LEFT JOIN Abc10 ON abc10id = eba1001cta" + 
											   " WHERE eba10codigo LIKE :grupo " +
											   getSamWhere().getWherePadrao(" AND ", Eba10.class) +
											   " ORDER BY eba10codigo");
		query.setParameter("grupo", "01%");
		contas = query.getListTableMap();
		
		List<TableMap> dados = new ArrayList<>();
		for (TableMap tm : contas) {
			String tab = tm.getString("abc10codigo") == null && tm.getString("eba10codigo").length() == grupos.get(0) ? "" : 
				tm.getString("abc10codigo") == null && tm.getString("eba10codigo").length() > grupos.get(0) ? StringUtils.ajustString("", tm.getString("eba10codigo").length() * 2) :
					StringUtils.ajustString("", tm.getString("abc10codigo").length() * 2);
			if(tm.getString("abc10codigo") != null) {
				TableMap tableMap = new TableMap();
				tableMap.put("codigo", StringUtils.ajustString("", tm.getString("eba10codigo").length() * 2) + tm.getString("eba10codigo"));
				tableMap.put("nome", tm.getString("eba10nome"));
				tableMap.put("saldo", BigDecimal.ZERO);
				int indexOf = 0;
				if(grupos.indexOf(tm.getString("eba10codigo").trim().length()) > 0) {
					indexOf = grupos.indexOf(tm.getString("eba10codigo").trim().length())-1;
					tableMap.put("pai", StringUtils.ajustString(tm.getString("eba10codigo").trim(), grupos.get(indexOf)));
				}
				if(!dados.contains(tableMap)) dados.add(tableMap);
			}
			String codigo = tm.getString("abc10codigo") != null ? tab + tm.getString("abc10codigo") : tab + tm.getString("eba10codigo");
			String nome = tm.getString("abc10nome") != null ? tm.getString("abc10nome") : tm.getString("eba10nome");
			
			
			Query queryEbb02 = getSession().createQuery(" SELECT SUM(ebb02deb) AS ebb02deb, SUM(ebb02cred) AS ebb02cred " + 
												   		" FROM ebb02 WHERE ebb02cta = :abc10id " + 
														" AND ((ebb02ano <= :ano AND ebb02mes <= :mes) OR (ebb02ano IS NULL AND ebb02mes IS NULL)) ");
			queryEbb02.setParameter("abc10id", tm.getLong("abc10id"));
			queryEbb02.setParameter("ano", anoMes.getYear());
			queryEbb02.setParameter("mes", anoMes.getMonthValue());
			TableMap ebb02 = queryEbb02.getUniqueTableMap();
			
			BigDecimal deb = ebb02.getBigDecimal("ebb02deb") != null ? ebb02.getBigDecimal("ebb02deb") : BigDecimal.ZERO;
			BigDecimal cred = ebb02.getBigDecimal("ebb02cred") != null ? ebb02.getBigDecimal("ebb02cred") : BigDecimal.ZERO;
			
			TableMap tableMap = new TableMap();
			tableMap.put("codigo", codigo);
			tableMap.put("nome", nome);
			tableMap.put("saldo", deb.subtract(cred).setScale(2, RoundingMode.HALF_EVEN));
			if(tm.getString("abc10codigo") != null) tableMap.put("pai", tm.getString("eba10codigo"));
			if(tm.getString("abc10codigo") == null) {
				int indexOf = 0;
				if(grupos.indexOf(tm.getString("eba10codigo").trim().length()) > 0) {
					indexOf = grupos.indexOf(tm.getString("eba10codigo").trim().length())-1;
					tableMap.put("pai", StringUtils.ajustString(tm.getString("eba10codigo").trim(), grupos.get(indexOf)));
				}
			}
			dados.add(tableMap);
		}
		
		dados.stream().forEach({dado -> 
			if(dado.getString("pai") != null && dado.getString("codigo").trim().length() > grupos.get(grupos.size()-1)) {
				TableMap pai = dados.stream().filter({d -> d.getString("codigo").trim().equalsIgnoreCase(dado.getString("pai"))}).findFirst().get();
				pai.put("saldo", pai.getBigDecimal("saldo").add(dado.getBigDecimal("saldo")));
				somandoNosPais(dados, dado.getBigDecimal("saldo"), pai.getString("codigo"), grupos);
			}else {
				
			}
		});
		Cubo cubo = new Cubo();
		
		cubo.adicionarColunas(new CuboColuna("Código", CuboColunaTipo.TEXTO));
		cubo.adicionarColunas(new CuboColuna("Nome", CuboColunaTipo.TEXTO));
		cubo.adicionarColunas(new CuboColuna("Saldo", CuboColunaTipo.NUMERO));
		
		cubo.adicionarDimensaoNaLinha("Código");
		cubo.adicionarDimensaoNaLinha("Nome");
		cubo.adicionarMetrica("Saldo", CuboAgregador.SOMA);
		cubo.adicionarMetrica("Saldo", CuboAgregador.QUANTIDADE);
		
		List<Object[]> dadosCubo = new ArrayList<>();
		if(detalhamento == 0) {
			List<TableMap> sintetico = dados.stream().filter({dado -> dado.getString("codigo").trim().length() <= grupos.get(grupos.size()-1)}).collect(Collectors.toList());
			for (TableMap tm : sintetico) {
				Object[] obj = [tm.getString("codigo"), tm.getString("nome"), tm.getBigDecimal("saldo")];
				dadosCubo.add(obj);
			}
		} else {
			for (TableMap tm : dados) {
				Object[] obj = [tm.getString("codigo"), tm.getString("nome"), tm.getBigDecimal("saldo")];
				dadosCubo.add(obj);
			}
		}

		cubo.adicionarDados(dadosCubo);
		return cubo;
	}

	private void somandoNosPais(List<TableMap> dados, BigDecimal saldo, String codigo, List<Integer> grupos) {
		int indexOf = grupos.indexOf(codigo.trim().length()) - 1;
		if(indexOf >= 0) {
			String codigoPai = StringUtils.ajustString(codigo.trim(), grupos.get(indexOf));
			TableMap pai = dados.stream().filter({dado -> dado.getString("codigo").trim().equalsIgnoreCase(codigoPai)}).findFirst().get();
			pai.put("saldo", pai.getBigDecimal("saldo").add(saldo));
			somandoNosPais(dados, saldo, codigoPai, grupos);
		}
	}

	private void getEstrutura(String estrutura, List<Integer> grupos, int tamanhoTotal) {
		tamanhoTotal = tamanhoTotal + StringUtils.substringBeforeFirst(estrutura, "|").length();
		grupos.add(new Integer(tamanhoTotal));
		estrutura = StringUtils.substringAfterFirst(estrutura, "|");
		if(!estrutura.contains("|")) {
			grupos.add(new Integer(tamanhoTotal + estrutura.length()));
			tamanhoTotal = tamanhoTotal + estrutura.length();
		}else {
			getEstrutura(estrutura, grupos, tamanhoTotal);
		}
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("detalhamento", "0");
		filtrosDefault.put("anoMes", MDate.date());
		return Utils.map("filtros", filtrosDefault);
	}
}
//meta-sis-eyJkZXNjciI6IlNHQy1DdWJvLUFncnVwYW1lbnRvIGRlIENvbnRhcyIsInRpcG8iOiJjdWJvIn0=