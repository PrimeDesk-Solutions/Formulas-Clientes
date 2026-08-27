package Profer.formulas.srf
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import sam.dicdados.FormulaTipo
import sam.dto.srf.EmailNFeDto
import sam.model.entities.aa.Aaa16
import sam.model.entities.aa.Aae11
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0114
import sam.server.samdev.formula.FormulaBase

class SRF_ComporEmailsNFe extends FormulaBase {
	
	//Parâmetros de envio
	Long eaa01id; //id do documento Eaa01
	Long aaa16id; //id do processamento da mensageria
	Long eaa0114id; //id da carta de correção
	
	//Parâmetro de retorno
	List<EmailNFeDto> emails = new ArrayList<>(); //Lista de e-mails a serem enviados
	
	//EmailNFeDto
	//Campos:
	//assunto String
	//corpo String
	//emailsDestinoPara Set<String> - Método facilitador para adicionar endereço de e-mail: addEmailDestinoPara("fulano@email.com")
	//enviarXML Boolean
	//enviarDanfe Boolean
	//enviarBoleto Boolean
	//emailRemetente Integer - 0: Principal 1: Cobrança 2: Faturamento - Indica qual e-mail de remetente do usuário será utilizado para os envios.
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_EMAIL_NFE;
	}
	
	@Override
	public void executar() {
		eaa01id = get("eaa01id");
		aaa16id = get("aaa16id");
		eaa0114id = get("eaa0114id");
				
		Aaa16 aaa16 = getSession().get(Aaa16.class, "aaa16id, aaa16tipo, aaa16retProt", aaa16id);
		
		Eaa01 eaa01 = getSession().get(Eaa01.class, "eaa01id, eaa01central, eaa01cancMotivo, eaa01nfeChave, eaa01rep0, eaa01rep1, eaa01rep2, eaa01rep3, eaa01rep4", eaa01id);
		
		Abb01 abb01 = getSession().get(Abb01.class, "abb01id, abb01tipo, abb01ent, abb01num, abb01data", eaa01.eaa01central.abb01id);
		
		Abe01 abe01 = getSession().get(Abe01.class, "abe01id, abe01nome, abe01ni", abb01.abb01ent.abe01id);
		
		String assunto = getVariaveis().getAac10().getAac10na() + " - Arq. Digital Ref. a Nota Fiscal: " + abb01.abb01num;
		
		String corpo = comporCorpoMsg(aaa16, eaa01, abe01, abb01, eaa0114id);
		
		Eaa0102 eaa0102 = getSession().get(Eaa0102.class, "eaa0102id, eaa0102eMail, eaa0102despacho", Criterions.eq("eaa0102doc", eaa01.eaa01id));
		
		if(eaa0102.eaa0102eMail == null) return;
		
		if(aaa16.getAaa16tipo().equals(Aaa16.TIPO_CANCELAMENTO_NFE) || aaa16.getAaa16tipo().equals(Aaa16.TIPO_CCE)) {
			//E-mail de CANCELAMENTO ou CARTA DE CORREÇÃO
			EmailNFeDto email = new EmailNFeDto();
			email.assunto = assunto;
			email.corpo = corpo;
			email.addEmailDestinoPara(eaa0102.eaa0102eMail);
			email.enviarXML = true;
			email.enviarDanfe = false;
			email.enviarBoleto = false;
			email.emailRemetente = 2;
			
			emails.add(email);
			
		}else {
			//E-mail de FATURAMENTO
			EmailNFeDto emailFat = new EmailNFeDto();
			emailFat.assunto = assunto;
			emailFat.corpo = corpo;
			emailFat.addEmailDestinoPara(eaa0102.eaa0102eMail);
			emailFat.enviarXML = true;
			emailFat.enviarDanfe = true;
			emailFat.enviarBoleto = false;
			emailFat.emailRemetente = 2;

			
			EmailNFeDto emailCob = new EmailNFeDto();
			String emailDestinoCob = obterEmailCobranca(eaa01.eaa01id, abe01.abe01id, eaa0102);
			if(eaa0102.eaa0102eMail != emailDestinoCob) {
				//E-mail de COBRANÇA, destinatário para faturamento é diferente do de cobrança.
				//Então o boleto será enviado somente para o destinatário de cobrança
				emailCob.assunto = assunto;
				emailCob.corpo = corpo;
				emailCob.addEmailDestinoPara(emailDestinoCob);
				emailCob.enviarXML = false;
				emailCob.enviarDanfe = false;
				emailCob.enviarBoleto = true;
				emailCob.emailRemetente = 1;
				
				emails.add(emailCob);
			}else {
				//Destinatário para faturamento e cobrança é o mesmo ou não existe um específico para cobrança.
				//Então o boleto será enviado junto com o de faturamento (com xml e danfe).
				emailFat.enviarBoleto = true;
				emailCob.enviarXML = true;
			}
			
			emails.add(emailFat);
			
			//E-mail para REPRESENTANTES
			Set<String> emailsReps = obterEmailsRepresentantes(eaa01);
			if(emailsReps != null && emailsReps.size() > 0) {
				EmailNFeDto emailRep = new EmailNFeDto();
				emailRep.assunto = assunto;
				emailRep.corpo = corpo;
				emailRep.enviarXML = false;
				emailRep.enviarDanfe = true;
				emailRep.enviarBoleto = false;
				emailRep.emailRemetente = 2;
				
				for(String emailDestinoRep : emailsReps) {
					emailRep.addEmailDestinoPara(emailDestinoRep);
				}
				
				emails.add(emailRep);
			}
			
			//E-mail para TRANSPORTADORA
			String emailDestinoTransp = obterEmailTransportadoraDespacho(eaa0102);
			if(emailDestinoTransp != null) {
				EmailNFeDto emailTransp = new EmailNFeDto();
				emailTransp.assunto = assunto;
				emailTransp.corpo = corpo;
				emailTransp.addEmailDestinoPara(emailDestinoTransp);
				emailTransp.enviarXML = true;
				emailTransp.enviarDanfe = false;
				emailTransp.enviarBoleto = false;
				emailTransp.emailRemetente = 2;
				
				emails.add(emailTransp);
			}
		}
		
		put("emails", emails);
	}
	
	private String comporCorpoMsg(Aaa16 aaa16, Eaa01 eaa01, Abe01 abe01, Abb01 abb01, Long eaa0114id) {
		StringBuilder strCorpo = new StringBuilder("");
		if(aaa16.getAaa16tipo().equals(Aaa16.TIPO_CANCELAMENTO_NFE)) {
			Aae11 aae11 = getSession().get(Aae11.class, "aae11id, aae11descr", eaa01.getEaa01cancMotivo().getIdValue());
			strCorpo.append("<html>");
			strCorpo.append("<body>Esta mensagem refere-se a Cancelamento de Nota Fiscal Eletr&ocirc;nica Nacional de n&uacute;mero <b>" + abb01.getAbb01num() + "</b> emitida para:");
			strCorpo.append("<p><b>Cliente:</b> " + abe01.getAbe01nome());
			strCorpo.append("<br><b>CNPJ:</b> "  + abe01.getAbe01ni());
			strCorpo.append("<p><b>Motivo do cancelamento:</b> "  + aae11.getAae11descr());
			strCorpo.append("<p> Para verificar a autoriza&ccedil;&atilde;o da SEFAZ referente &agrave; nota acima mencionada, acesse o site <a href='http://www.nfe.fazenda.gov.br/portal'>www.nfe.fazenda.gov.br/portal</a>");
			strCorpo.append("<p>Chave de acesso: " + eaa01.getEaa01nfeChave());
			strCorpo.append("<br>Protocolo: " + aaa16.getAaa16retProt());
			strCorpo.append("<p>Este e-mail foi enviado automaticamente pelo SAM (Sistemas Administrativos Multitec) da MULTITEC SISTEMAS");
			strCorpo.append("<p>Acesse o site: <a href='http://www.multitecsistemas.com.br'>www.multitecsistemas.com.br</a>");
		}else if(aaa16.getAaa16tipo().equals(Aaa16.TIPO_CCE)) {
			Eaa0114 eaa0114 = session.get(Eaa0114.class, "eaa0114id, eaa0114correcao", eaa0114id);
	
			strCorpo.append("<html>");
			strCorpo.append("<body>Esta mensagem refere-se a Carta de Corre&ccedil;&atilde;o da Nota Fiscal Eletr&ocirc;nica Nacional de n&uacute;mero <b>" + abb01.getAbb01num() + "</b> emitida para:");
			strCorpo.append("<p><b>Cliente:</b> " + abe01.getAbe01nome());
			strCorpo.append("<br><b>CNPJ:</b> "  + abe01.getAbe01ni());
			strCorpo.append("<p><b>Corre&ccedil;&atilde;o:</b> "  + eaa0114.getEaa0114correcao());
			strCorpo.append("<p> Para verificar a autoriza&ccedil;&atilde;o da SEFAZ referente &agrave; nota acima mencionada, acesse o site <a href='http://www.nfe.fazenda.gov.br/portal'>www.nfe.fazenda.gov.br/portal</a>");
			strCorpo.append("<p>Chave de acesso: " + eaa01.getEaa01nfeChave());
			strCorpo.append("<br>Protocolo: " + aaa16.getAaa16retProt());
			strCorpo.append("<p>Este e-mail foi enviado automaticamente pelo SAM (Sistemas Administrativos Multitec) da MULTITEC SISTEMAS");
			strCorpo.append("<p>Acesse o site: <a href='http://www.multitecsistemas.com.br'>www.multitecsistemas.com.br</a>");
		}else {
			strCorpo.append("<html>");
			strCorpo.append("<body>Esta mensagem refere-se a Nota Fiscal Eletr&ocirc;nica Nacional de n&uacute;mero <b>" + abb01.getAbb01num() + "</b> emitida para:");
			strCorpo.append("<p><b>Cliente:</b> " + abe01.getAbe01nome());
			strCorpo.append("<br><b>CNPJ:</b> "  + abe01.getAbe01ni());
			strCorpo.append("<p> Para verificar a autoriza&ccedil;&atilde;o da SEFAZ referente &agrave; nota acima mencionada, acesse o site <a href='http://www.nfe.fazenda.gov.br/portal'>www.nfe.fazenda.gov.br/portal</a>");
			strCorpo.append("<p>Chave de acesso: " + eaa01.getEaa01nfeChave());
			strCorpo.append("<br>Protocolo: " + aaa16.getAaa16retProt());
			strCorpo.append("<p>Este e-mail foi enviado automaticamente pelo SAM (Sistemas Administrativos Multitec) da MULTITEC SISTEMAS");
			strCorpo.append("<p>Acesse o site: <a href='http://www.multitecsistemas.com.br'>www.multitecsistemas.com.br</a>");
		}
		return strCorpo.toString();
	}
	
	private String obterEmailCobranca(Long eaa01id, Long abe01id, Eaa0102 eaa0102) {
		String emailCobranca = null;
	
		Eaa0101 eaa0101 = getSession().get(Eaa0101.class, "eaa0101id, eaa0101eMail",
				Criterions.and(Criterions.eq("eaa0101doc", eaa01id), Criterions.eq("eaa0101cobranca", Abe0101.SIM)));
		if(eaa0101 != null) emailCobranca = eaa0101.getEaa0101eMail();
		if(emailCobranca == null) {
			Abe0101 abe0101 = getSession().get(Abe0101.class, "abe0101id, abe0101eMail",
					Criterions.and(Criterions.eq("abe0101ent", abe01id), Criterions.eq("abe0101cobranca", Abe0101.SIM)));
			if(abe0101 != null) emailCobranca = abe0101.getAbe0101eMail();
		}
		if(emailCobranca == null && eaa0102 != null) emailCobranca = eaa0102.getEaa0102eMail();
	
		return emailCobranca;
	}
	
	private Set<String> obterEmailsRepresentantes(Eaa01 eaa01) {
		Set<String> emails = new HashSet<String>();
	
		for(int i = 0; i <= 4; i++) {
			Abe01 abe01Rep = i == 0 ? eaa01.eaa01rep0 : (i == 1 ? eaa01.eaa01rep1 : (i == 2 ? eaa01.eaa01rep2 : (i == 3 ? eaa01.eaa01rep3 : eaa01.eaa01rep4)));
			if(abe01Rep != null) {
				Abe0101 abe0101 = session.createCriteria(Abe0101.class)
										 .addFields("abe0101id, abe0101eMail")
										 .addJoin(Joins.join("abe0101ent"))
										 .addWhere(Criterions.eq("abe0101ent", abe01Rep.getIdValue()))
										 .addWhere(Criterions.eq("abe0101principal", Abe0101.SIM))
										 .setMaxResults(1)
										 .get();
	
				if(abe0101 != null && abe0101.abe0101eMail != null) emails.add(abe0101.abe0101eMail);
			}
		}
	
		return emails;
	}
	
	private String obterEmailTransportadoraDespacho(Eaa0102 eaa0102) {
		String email = null;
	
		if(eaa0102.eaa0102despacho != null) {
			Abe0101 abe0101 = session.createCriteria(Abe0101.class)
									 .addFields("abe0101id, abe0101eMail")
									 .addJoin(Joins.join("abe0101ent"))
									 .addWhere(Criterions.eq("abe0101ent", eaa0102.eaa0102despacho.abe01id))
									 .addWhere(Criterions.eq("abe0101principal", Abe0101.SIM))
									 .setMaxResults(1)
									 .get();

			if(abe0101 != null && abe0101.abe0101eMail != null) email = abe0101.abe0101eMail;
		}
		
		return email;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTIifQ==