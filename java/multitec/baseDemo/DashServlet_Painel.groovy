package multitec.baseDemo;

import java.time.DayOfWeek

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multiorm.ColumnType
import br.com.multiorm.MultiResultSet
import br.com.multiorm.Query
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.aa.Aac01
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_Painel extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Painel Mensagens";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 6, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		Map<String, Object> valoresDefault = Utils.map(
			"mensagens", this.buscarListaPainel()
		)
		
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_Painel.html", valoresDefault);
				
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
	
	/**
	 * Retorna uma lista com informações para o painel
	 */
	public List<String> buscarListaPainel() {
		List<String> retorno = new ArrayList<>();

		//Acompanhamentos por SQL
		List<TableMap> lrs = findPainelByUser();
		for(int i =0; i < lrs.size(); i++){
			int ctrlReg = lrs.get(i).getInteger("aaa60ctrlReg");
			int ctrlMsg = lrs.get(i).getInteger("aaa60ctrlMsg");
			List<String> msg = getMensagensPainel(traduzPainelSql(lrs.get(i).getString("aaa60sql")), ctrlReg, ctrlMsg, lrs.get(i).getString("aaa60msg"));
			for (int linhaMapa = 0; linhaMapa < msg.size(); linhaMapa++) {
				retorno.add(retorno.size(), msg.get(linhaMapa));
			}
		}
		return retorno;
	}
	
	private List<Integer> atalhosDaMsg;
	public List<String> getMensagensPainel(String sql, int ctrlReg, int ctrlMsg, String msg)throws Exception{
		List<String> msgs = new ArrayList<>();
		getMensagem(session.createQuery(sql).getMultiResultSet(), ctrlReg, ctrlMsg, msgs, msg);
		return msgs;
	}
	
	private void getMensagem(MultiResultSet resultSet, int ctrlReg, int ctrlMsg, List<String> msgs, String msg) throws Exception {
		atalhosDaMsg = null;
		if (resultSet.next() == false) {
			if (ctrlReg == 1) {
				msgs.add(msg);
			}
		} else {
			if(ctrlReg == 2)return;
			if (ctrlMsg == 0) {
				msgs.add(traduzMensagem(resultSet, msg));
			} else if (ctrlMsg == 1) {
				resultSet.last();
				msgs.add(traduzMensagem(resultSet, msg));
			} else {
				msgs.add(traduzMensagem(resultSet, msg));
				while (resultSet.next()) {
					msgs.add(traduzMensagem(resultSet, msg));
				}
			}
		}
	}
	
	private String traduzMensagem(MultiResultSet resultSet, String msg) {
		String retorno = msg;
		if (msg.contains("\$")) {
			if (atalhosDaMsg == null) {
				atalhosDaMsg = new ArrayList<Integer>();
				int lastIndex = msg.indexOf("\$");
				while (lastIndex != -1) {
					int num = getNumeroInMsg(msg, lastIndex);
					if(num != 0){
						atalhosDaMsg.add(num);
					}else{
						break;
					}
					lastIndex = msg.indexOf("\$", lastIndex +1);
				}
			}
			for (Integer i : atalhosDaMsg) {
				try {
					int index = i - 1;
					retorno = retorno.replace("\$" + i, resultSet.getORMObject(index) == null ? "null" : resultSet.getORMObject(index).toString());
				} catch (Exception err) {
					retorno = retorno.replace("\$" + i, "\"!ERRO!\"");
				}
			}
		}

		return retorno.length() > 300 ? retorno.substring(0, 299) : retorno;
	}

	private static int getNumeroInMsg(String msg, int indexInit){
		String numero = "";

		while(msg.length() > indexInit + 1){
			int num = 0;
			try{
				num = Integer.parseInt(String.valueOf(msg.charAt(++indexInit)));
			}catch(Exception err){
				try{
					return Integer.parseInt(numero);
				}catch(Exception err1){
					return 0;
				}
			}
			numero += num;
		}

		try{
			return Integer.parseInt(numero);
		}catch(Exception err1){
			return 0;
		}
	}

	/**
	 * Retorna todos os registros da tabela para o usuário logado
	 * @return List de Object[] contendo aa39msg, aa39sql, aa39ctrlreg, aa39ctrlmsg
	 */
	public List<TableMap> findPainelByUser(){
		String whereDiaSemana = "";
		switch (MDate.date().getDayOfWeek()) {
		case DayOfWeek.SUNDAY:
			whereDiaSemana = " AND aaa60dom = 1 ";
			break;
		case DayOfWeek.MONDAY:
			whereDiaSemana = " AND aaa60seg = 1 ";
			break;
		case DayOfWeek.TUESDAY:
			whereDiaSemana = " AND aaa60ter = 1 ";
			break;
		case DayOfWeek.WEDNESDAY:
			whereDiaSemana = " AND aaa60qua = 1 ";
			break;
		case DayOfWeek.THURSDAY:
			whereDiaSemana = " AND aaa60qui = 1 ";
			break;
		case DayOfWeek.FRIDAY:
			whereDiaSemana = " AND aaa60sex = 1 ";
			break;
		case DayOfWeek.SATURDAY:
			whereDiaSemana = " AND aaa60sab = 1 ";
			break;
		}

		String hqlFindAaa60 = "SELECT * " +
				" FROM Aaa60 " +
				" WHERE aaa60user = :userId " +
				" AND :diaHoje BETWEEN aaa60dti AND aaa60dtf AND :horaHoje BETWEEN aaa60horai AND aaa60horaf " +
				whereDiaSemana;
		Query q = session.createQuery(hqlFindAaa60);
		q.setParameter("userId", variaveis.getAab10().getAab10id());
		q.setParameter("diaHoje", MDate.date());
		q.setParameter("horaHoje", MDate.time());
		return q.getListTableMap();
	}
	
	private String traduzPainelSql(String sqlCustomizada) {
		StringBuilder build = new StringBuilder(sqlCustomizada);
		while(build.indexOf("\$gc(") >= 0) {
			int indexInicio = build.indexOf("\$gc(");
			int indexFinal = build.indexOf("\$", indexInicio+1)+1;
			
			if(indexInicio < 0 || indexFinal < 0)throw new ValidacaoException("Função de grupo centralizador inválido");
			
			String funcao = build.substring(indexInicio, indexFinal).replace(" ", "");
			
			int indexParamIni = funcao.indexOf("(");
			int indexParamFin = funcao.indexOf(")");
			
			if(indexParamIni < 0 || indexParamFin < 0)throw new ValidacaoException("Parâmetros da função de grupo centralizador inválido: " + funcao);
			
			String tabelaCentralizadora = funcao.substring(indexParamIni+1, indexParamFin).toUpperCase();
			
			Aac01 aac01 = session.createQuery("SELECT * FROM Aac01 WHERE aac01id = " +
			" (SELECT aac1001gc FROM aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = :tabela)")
			.setParameter("aac10id", variaveis.getAab10().getAab10emprDefault().getAac10id())
			.setParameter("tabela", tabelaCentralizadora.toUpperCase())
			.getUniqueResult(ColumnType.ENTITY);
			
			if(aac01 == null){
				throw new ValidacaoException("Não foi encontrado Grupo Centralizador para a tabela " + tabelaCentralizadora + " para a Empresa Logada.\n" +
						"Verifique os registros de Grupo Centralizador no cadastro \"CAS2010 - Empresas\"");
			}

			String value = StringUtils.concat(" = ", aac01.getAac01id());
			
			build = build.replace(indexInicio, indexFinal, value);
		}
		
		return build.toString();
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLVBhaW5lbCBNZW5zYWdlbnMiLCJ0aXBvIjoic2VydmxldCIsInciOjYsImgiOjEyLCJyZXNpemUiOnRydWUsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=