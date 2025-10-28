package multitec.baseDemo

import java.time.LocalDate
import java.time.format.DateTimeParseException

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.DecimalUtils
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb11
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abc20
import sam.model.entities.ab.Abc2001
import sam.model.entities.ab.Abe01
import sam.model.entities.eb.Ebb05
import sam.model.entities.eb.Ebb0501
import sam.server.samdev.formula.FormulaBase

class SGC_ImportarLctos extends FormulaBase {
		
	private MultipartFile arquivo;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SGC;
	}
	
	public void executar() {
		arquivo = get("arquivo");
		
		List<Ebb05> ebb05s = new ArrayList<>();
		
		File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
		arquivo.transferTo(file);
		
		List<String> registros = FileUtils.readLines(file, "UTF-8");
		
		TextFileLeitura txt = new TextFileLeitura(registros, "|");
		
		Ebb05 ebb05 = null;
		
		int linha = 1;
		while(txt.nextLine()) {
			
			if(txt.getCampo(1).equals("0")) {
				if(txt.getRegistro().size() != 21) throw new ValidacaoException("A quantidade de campos está inválida. Linha: " + linha);
				
				ebb05 = importarLancamento(txt, linha);
				ebb05s.add(ebb05);
			}else if(txt.getCampo(1).equals("1")) {
				if(txt.getRegistro().size() != 5) throw new ValidacaoException("A quantidade de campos está inválida. Linha: " + linha);
				
				Ebb0501 ebb0501 = importarRateio(txt, linha, ebb05);
				ebb05.addToEbb0501s(ebb0501);
			}
			
			linha++;
		}
	
		put("ebb05s", ebb05s);
	}
	
	private Ebb05 importarLancamento(TextFileLeitura txt, int linha) {
		Ebb05 ebb05 = null;
		
		Abc2001 abc2001LctP = buscarPrimeiraClassificacaoPorLctoContabilPadrao(txt.getCampo(3));
		
		if(abc2001LctP == null && txt.getCampo(4).length() == 0) throw new ValidacaoException("A conta contábil para débito não foi informada no txt. Linha: " + linha);
		if(abc2001LctP == null && txt.getCampo(6).length() == 0) throw new ValidacaoException("A conta contábil para crédito não foi informada no txt. Linha: " + linha);
		
		Abc10 abc10Deb = buscarContaContabil(txt.getCampo(4));
		Abc10 abc10Cred = buscarContaContabil(txt.getCampo(6));
		
		if(abc10Deb == null && abc2001LctP != null) {
			abc10Deb = abc2001LctP.getAbc2001ctaDeb();
		}
		
		if(abc10Cred == null && abc2001LctP != null) {
			abc10Cred = abc2001LctP.getAbc2001ctaCred();
		}
		
		if(txt.getCampo(4).length() > 0 && abc10Deb == null) throw new ValidacaoException("A conta contábil para débito " + txt.getCampo(4) + " não existe no sistema. Linha: " + linha);
		if(txt.getCampo(6).length() > 0 && abc10Cred == null) throw new ValidacaoException("A conta contábil para crédito " + txt.getCampo(6) + " não existe no sistema. Linha: " + linha);
		if(txt.getCampo(5).length() == 0) throw new ValidacaoException("A conciliação da conta débito não foi informada no txt. Linha: " + linha);
		if(txt.getCampo(7).length() == 0) throw new ValidacaoException("A conciliação da conta crédito não foi informada no txt. Linha: " + linha);
		if(txt.getCampo(8).length() == 0) throw new ValidacaoException("A Integração com departamento não foi informado no txt. Linha: " + linha);
		
		Aah01 aah01TipoDcto = buscarTipoDoctoPeloCodigo(txt.getCampo(12));
		Abe01 abe01Ent = buscarEntidadePeloCodigo(txt.getCampo(13));
		
		if(aah01TipoDcto != null || txt.getCampo(14).length() > 0 || txt.getCampo(15).length() > 0) {
			if(txt.getCampo(12).length() == 0) throw new ValidacaoException("O tipo de documento da central de documentos não foi informado no txt. Linha: " + linha);
			
			if(txt.getCampo(12).length() > 0 && aah01TipoDcto == null) throw new ValidacaoException("O tipo de documento " + txt.getCampo(12) + " não existe no sistema. Linha: " + linha);
					
			if(txt.getCampo(15).length() == 0) throw new ValidacaoException("A data da central de documentos não foi informada no txt. Linha: " + linha);
			try{
				DateUtils.parseDate(txt.getCampo(15), "ddMMyyyy");
			}catch(DateTimeParseException e) {
				throw new ValidacaoException("A data da central de documentos deverá ser no formato (ddMMyyyy). Linha: " + linha);
			}
		}
		
		String historico = txt.getCampo(11);
				
		if(historico == null || historico.length() == 0) {
			if(abc2001LctP != null) {
				if(abc2001LctP.getAbc2001origemHist() != null) {
					if(abc2001LctP.getAbc2001origemHist().equals(Abc2001.ORIGEMHIST_HIST_EXCLUSIVO)) {
						historico = abc2001LctP.getAbc2001histExc();
					}else if(abc2001LctP.getAbc2001hp() != null){
						historico = abc2001LctP.getAbc2001hp().getAbb12redacao();
					}
				}
			}
			
		}
		if(historico == null || historico.length() == 0) throw new ValidacaoException("O histórico do lançamento contábil não foi informado no txt e não encontrado no lançamento contábil padrão. Linha: " + linha);
		
		Long aah01id = aah01TipoDcto != null ? aah01TipoDcto.getAah01id() : null;
		Integer abb01num = txt.getCampo(14).length() == 0 ? null : Integer.parseInt(txt.getCampo(14));
		Long abe01id = abe01Ent != null ? abe01Ent.getAbe01id() : null;
		LocalDate abb01data = txt.getCampo(15).length() == 0 ? null : DateUtils.parseDate(txt.getCampo(15), "ddMMyyyy");
		String abb01serie = txt.getCampo(16).length() == 0 ? null : txt.getCampo(16);
		String abb01parcela = txt.getCampo(17).length() == 0 ? null : txt.getCampo(17);
		Integer abb01quita = txt.getCampo(18).length() == 0 ? null : Integer.parseInt(txt.getCampo(18));
		
		Abb01 abb01 = verificarExistenciaDocNaCentral(aah01id, abb01num, abb01serie, abb01parcela, abe01id, abb01data, abb01quita);

		if(txt.getCampo(9).length() == 0) throw new ValidacaoException("O valor do lançamento não foi informado no txt. Linha: " + linha);
		try {
			DecimalUtils.create(txt.getCampo(9)).get();
		}catch(NumberFormatException err) {
			throw new ValidacaoException("O valor do lançamento deverá ser no formato numérico. Linha: " + linha);
		}
		
		BigDecimal valor = DecimalUtils.create(txt.getCampo(9)).divide(100).round(2).get();
		
		if(abb01 == null) {
			if(aah01TipoDcto != null) {
				
				abb01 = new Abb01();
				
				abb01.setAbb01tipo(aah01TipoDcto);
				abb01.setAbb01num(abb01num);
				abb01.setAbb01ent(abe01Ent);
				abb01.setAbb01data(abb01data);
				abb01.setAbb01serie(abb01serie);
				abb01.setAbb01parcela(abb01parcela);
				abb01.setAbb01quita(abb01quita);
				abb01.setAbb01valor(valor);
				abb01.setAbb01operCod(null);
				abb01.setAbb01operAutor("SGC0520");
				abb01.setAbb01aprovado(1);
				samWhere.setDefaultValues(abb01);
			}
		}
		
		if(txt.getCampo(2).length() == 0) throw new ValidacaoException("A data do lançamento contábil não foi informada no txt. Linha: " + linha);
		try{
			DateUtils.parseDate(txt.getCampo(2), "ddMMyyyy");
		}catch(DateTimeParseException e) {
			throw new ValidacaoException("A data do lançamento deverá ser no formato (ddMMyyyy). Linha: " + linha);
		}
		
		if(txt.getCampo(10).length() == 0) throw new ValidacaoException("O aceite não foi informado no txt. Linha: " + linha);
		
		String json = txt.getCampo(19);
		
		ebb05 = new Ebb05();
		ebb05.setEbb05data(DateUtils.parseDate(txt.getCampo(2), "ddMMyyyy"));
		ebb05.setEbb05deb(abc10Deb);
		ebb05.setEbb05concDeb(Integer.parseInt(txt.getCampo(5)));
		ebb05.setEbb05cred(abc10Cred);
		ebb05.setEbb05concCred(Integer.parseInt(txt.getCampo(7)));
		ebb05.setEbb05intDepto(Integer.parseInt(txt.getCampo(8)));
		ebb05.setEbb05valor(valor);
		ebb05.setEbb05aceite(Integer.parseInt(txt.getCampo(10)));
		ebb05.setEbb05historico(historico);
		ebb05.setEbb05central(abb01);
		ebb05.setEbb05json(json != null && json.length() > 0 ? JSonMapperCreator.create().read(json, TableMap.class) : null);
		
		return ebb05;
	}
	
	private Abb01 verificarExistenciaDocNaCentral(Long aah01id, Integer abb01num, String abb01serie, String abb01parcela, Long abe01id, LocalDate abb01data, Integer abb01quita) {
		return session.createCriteria(Abb01.class)
		.addFields("abb01id, abb01tipo, abb01num, abb01ent, abb01data, abb01operHora, abb01serie, abb01parcela, abb01quita, abb01valor, abb01operCod, abb01status, abb01aprovado, abb01gc")
		.addWhere(Criterions.eq("abb01tipo", aah01id))
		.addWhere(Criterions.eq("abb01num", abb01num))
		.addWhere(Criterions.eq("abb01serie", abb01serie))
		.addWhere(Criterions.eq("abb01parcela", abb01parcela))
		.addWhere(Criterions.eq("abb01ent", abe01id))
		.addWhere(Criterions.eq("abb01data", abb01data))
		.addWhere(Criterions.eq("abb01quita", abb01quita))
		.addWhere(samWhere.getCritPadrao(Abb01.class))
		.setMaxResults(1)
		.get(ColumnType.ENTITY);
	}
	
	private Abc2001 buscarPrimeiraClassificacaoPorLctoContabilPadrao(String abc20codigo) {
		return session.createQuery(" SELECT abc2001.abc2001id, abc2001.abc2001ctaDeb, abc2001.abc2001ctaCred, abc2001.abc2001hp, abc2001.abc2001histExc, abc2001.abc2001origemHist, abb12.abb12id, abb12.abb12redacao",
									" FROM Abc2001 AS abc2001",
								   " INNER JOIN Abc20 ON abc2001lp = abc20id",
								   " LEFT JOIN PART abc2001.abc2001hp AS abb12",
								   " WHERE UPPER(abc20codigo) = :abc20codigo ",
								   " AND abc20fin = :finalidade",
									samWhere.getWhereGc("AND", Abc2001.class))
					  .setMaxResult(1)
					  .setParameters("abc20codigo", abc20codigo.toUpperCase(),
									   "finalidade", Abc20.FIN_CONTABILIDADE)
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Abc10 buscarContaContabil(String codigo) {
		Abc10 abc10 = null;
		if(codigo != null) {
			if(codigo.length() > 6) {
				abc10 = buscarContaPorCodigo(codigo);
			}else {
				try {
					Integer reduzido = Integer.parseInt(codigo);
					abc10 = buscarContaPeloReduzido(reduzido);
				}catch(NumberFormatException e) {
					abc10 = null;
				}
			}
		}
		return abc10;
	}
	
	private Abc10 buscarContaPorCodigo(String abc10codigo){
		return session.createQuery(" SELECT abc10id, abc10codigo, abc10nome",
								   " FROM Abc10",
								   " WHERE LOWER(abc10codigo) = :abc10codigo",
									samWhere.getWhereGc("AND", Abc10.class))
						   .setParameter("abc10codigo", abc10codigo.toLowerCase())
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Abc10 buscarContaPeloReduzido(Integer abc10reduzido) {
		return session.createQuery(" SELECT abc10id, abc10nome",
								   " FROM Abc10",
								   " WHERE abc10reduzido = :abc10reduzido",
								   samWhere.getWhereGc("AND", Abc10.class))
					  .setParameter("abc10reduzido", abc10reduzido)
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Aah01 buscarTipoDoctoPeloCodigo(String aah01codigo){
		return session.createQuery(" SELECT aah01id, aah01nome, aah01numeracao",
								   " FROM Aah01",
								   " WHERE LOWER(aah01codigo) = :aah01codigo",
								   samWhere.getWhereGc("AND", Aah01.class))
					  .setParameter("aah01codigo", aah01codigo.toLowerCase())
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Abe01 buscarEntidadePeloCodigo(String abe01codigo){
		return session.createQuery(" SELECT abe01id, abe01codigo, abe01na, abe01nome",
								   " FROM Abe01",
								   " WHERE LOWER(abe01codigo) = :abe01codigo",
									samWhere.getWhereGc("AND", Abe01.class))
					  .setParameter("abe01codigo", abe01codigo.toLowerCase())
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Ebb0501 importarRateio(TextFileLeitura txt, Integer linha, Ebb05 ebb05) {
		Ebb0501 ebb0501 = null;
		
		if(ebb05 == null) throw new ValidacaoException("Arquivo incompleto. Não foi encontrado tipo do registro 0-Lançamento. Linha: " + linha);
		if(txt.getCampo(2) == null) throw new ValidacaoException("O departamento não foi informado no txt. Linha: " + linha);
		
		Abb11 abb11 = buscarDepartamentoPorCodigo(txt.getCampo(2));
		
		if(abb11 == null) throw new ValidacaoException("O departamento " + txt.getCampo(2) + " não existe no sistema. Linha: " + linha);
		if(txt.getCampo(3).length() == 0) throw new ValidacaoException("O valor do departamento não foi informado no txt. Linha: " + linha);
		try {
			DecimalUtils.create(txt.getCampo(3)).get();
		}catch(NumberFormatException err) {
			throw new ValidacaoException("O valor do departamento deverá ser no formato numérico. Linha: " + linha);
		}
		
		ebb0501 = new Ebb0501();
		ebb0501.setEbb0501lcto(ebb05);
		ebb0501.setEbb0501depto(abb11);
		ebb0501.setEbb0501valor(DecimalUtils.create(txt.getCampo(3)).divide(100).round(2).get());
		
		return ebb0501;
	}
	
	private Abb11 buscarDepartamentoPorCodigo(String abb11codigo){
		return session.createQuery(" SELECT abb11id, abb11nome",
								   " FROM Abb11",
								   " WHERE LOWER(abb11codigo) = :abb11codigo",
								   samWhere.getWhereGc("AND", Abb11.class))
					  .setParameter("abb11codigo", abb11codigo.toLowerCase())
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTUifQ==