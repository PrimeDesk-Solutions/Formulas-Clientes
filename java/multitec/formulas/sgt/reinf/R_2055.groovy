package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe03
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2055 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_REINF;
	}

	@Override
	public void executar() {
		ReinfService reinfService = instanciarService(ReinfService.class);
		
		boolean isRetificacao = get("isRetificacao");
		LocalDate periodo = get("periodo");
		List<Aaa17> aaa17s = new ArrayList();
		
		Aac10 aac10 = Variaveis.obter().getAac10();
		
		Integer tpAmb = 1;
		
		selecionarAlinhamento("0052");

		Set<Long> empresas = reinfService.selecionarEmpresasReinf();
		
		String sql = " SELECT eaa01eg, abb01ent FROM Eaa01 " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Abe01 ON abe01id = abb01ent " +
					 " INNER JOIN Abe03 ON abe03ent = abe01id " +
					 " WHERE eaa01clasDoc = 1 AND eaa01esMov = 0 AND " +
					 " eaa01iReinf = 1 AND " +
					 " abe03prRural = 1 AND " +
					 " eaa01cancData IS NULL AND " +
                     " eaa01eg IN (:aac01ids) AND " +
					 Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()) + " = :numMeses " +
					 " GROUP BY eaa01eg, abb01ent";
					 
		def numMeses = periodo.year * 12 + periodo.monthValue;
		
		List<TableMap> entidadesPorEmpresa = getAcessoAoBanco().buscarListaDeTableMap(sql, criarParametroSql("aac01ids", empresas), criarParametroSql("numMeses", numMeses));
		for(int i = 0; i < entidadesPorEmpresa.size(); i++) {
			TableMap tm = entidadesPorEmpresa.get(i);
			
			String sqlNotas = " SELECT * FROM Eaa01 " +
							  " INNER JOIN Abb01 ON abb01id = eaa01central " +
							  " INNER JOIN Abe01 ON abe01id = abb01ent " +
							  " INNER JOIN Abe03 ON abe03ent = abe01id " +
							  " WHERE eaa01clasDoc = 1 AND eaa01esMov = 0 " +
							  " AND eaa01iReinf = 1 AND " +
							  " abb01ent = :abe01id AND " +
							  " abe03prRural = 1 AND " +
							  " eaa01cancData IS NULL AND " +
							  " eaa01eg IN (:eg) AND " +
							  Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()) + " = :numMeses " +
							  " ORDER BY abb01ent, abb01tipo, abb01num, eaa01esData";

			List<Eaa01> notas = getAcessoAoBanco().buscarListaDeRegistros(sqlNotas, criarParametroSql("abe01id", tm.getLong("abb01ent")), criarParametroSql("eg", tm.getLong("eaa01eg")), criarParametroSql("numMeses", numMeses));
			
			Aac10 aac10EG = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aac10 WHERE aac10id = :id", criarParametroSql("id", tm.getLong("eaa01eg")));
			
			ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evt2055AquisicaoProdRural/v1_05_01");
			
			ElementXml evtAqProd = reinf.addNode("evtAqProd");
			evtAqProd.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
			
			ElementXml ideEvento = evtAqProd.addNode("ideEvento");
			String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-2055", aac10.aac10id, periodo) : null;
			ideEvento.addNode("indRetif", isRetificacao && recibo != null ? 2 : 1, true);
			if(recibo != null) ideEvento.addNode("nrRecibo", recibo, false);
			
			ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
			ideEvento.addNode("tpAmb", tpAmb, true);
			ideEvento.addNode("procEmi", "1", true);
			ideEvento.addNode("verProc", Utils.getVersao(), true);
			
			ElementXml ideContri = evtAqProd.addNode("ideContri");
			ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
			ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
			
			ElementXml infoAquisProd = evtAqProd.addNode("infoAquisProd");
			
			ElementXml ideEstabAdquir = infoAquisProd.addNode("ideEstabAdquir");
			ideEstabAdquir.addNode("tpInscAdq", "1", true);
			ideEstabAdquir.addNode("nrInscAdq", StringUtils.extractNumbers(aac10EG.aac10ni), true);

			Abe01 abe01Produtor = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe01 WHERE abe01id = :abe01id", criarParametroSql("abe01id", tm.getLong("abb01ent")));
			Abe03 abe03 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe03 WHERE abe03ent = :abe01id", criarParametroSql("abe01id", tm.getLong("abb01ent")));
			
			ElementXml ideProdutor = ideEstabAdquir.addNode("ideProdutor");
			ideProdutor.addNode("tpInscProd", abe01Produtor.abe01ti_Zero == 0 ? "1" : "2", true);
			ideProdutor.addNode("nrInscProd", StringUtils.extractNumbers(abe01Produtor.abe01ni), true);
			ideProdutor.addNode("indOpcCP", abe03.abe03contCprb_Zero == 1 ? "S" : null, false);
			
			Map<Integer, TableMap> mapValores = new HashMap();
			for (eaa01 in notas) {
				Integer indic = abe03.abe03prAquis;
				
				TableMap tmValor = mapValores.containsKey(indic) ? mapValores.get(indic) : new TableMap();
				TableMap eaa01json = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
				
				tmValor.put("vlrBruto", tmValor.getBigDecimal_Zero("vlrBruto") + eaa01json.getBigDecimal_Zero(getCampo("R-2055", "vlrBruto")));
				tmValor.put("vlrCPDescPR", tmValor.getBigDecimal_Zero("vlrCPDescPR") + eaa01json.getBigDecimal_Zero(getCampo("R-2055", "vlrCPDescPR")));
				tmValor.put("vlrRatDescPR", tmValor.getBigDecimal_Zero("vlrRatDescPR") + eaa01json.getBigDecimal_Zero(getCampo("R-2055", "vlrRatDescPR")));
				tmValor.put("vlrSenarDesc", tmValor.getBigDecimal_Zero("vlrSenarDesc") + eaa01json.getBigDecimal_Zero(getCampo("R-2055", "vlrSenarDesc")));
				
				mapValores.put(indic, tmValor);
			}
			
			for(Integer indic : mapValores.keySet()) {
				TableMap tmIndic = mapValores.get(indic);
				ElementXml detAquis = ideProdutor.addNode("detAquis");
				detAquis.addNode("indAquis", indic, true);
				detAquis.addNode("vlrBruto", ReinfUtils.formatarDecimal(tmIndic.getBigDecimal_Zero("vlrBruto"), 2, false), true);
				detAquis.addNode("vlrCPDescPR", ReinfUtils.formatarDecimal(tmIndic.getBigDecimal_Zero("vlrCPDescPR"), 2, false), true);
				detAquis.addNode("vlrRatDescPR", ReinfUtils.formatarDecimal(tmIndic.getBigDecimal_Zero("vlrRatDescPR"), 2, false), true);
				detAquis.addNode("vlrSenarDesc", ReinfUtils.formatarDecimal(tmIndic.getBigDecimal_Zero("vlrSenarDesc"), 2, false), true);
			}
			
			Aaa17 aaa17 = reinfService.comporAaa17("R-2055", periodo, isRetificacao);
			aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
			
			if(reinfService.confirmarGeracaoXML("R-2055", periodo, aaa17.getAaa17xmlEnvio(), "nrInscAdq", "nrInscProd")) {
				aaa17s.add(aaa17);
			}
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==