package Silcon.relatorios.scf

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.Normalizer
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aab10
import sam.model.entities.aa.Aab1008
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abe0104
import sam.model.entities.da.Daa01
import sam.server.cas.service.CAS1010Service
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

import br.com.multitec.utils.Email
import javax.mail.util.ByteArrayDataSource
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport

class SCF_Boleto_Itau extends RelatorioBase {
    Integer DAC = 0
    Integer peso = 2
    public final static String PATTERN_DDMMYYYY = "dd/MM/yyyy";
    @Override
    public String getNomeTarefa() {
        return "SCF - Boleto Itaú";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        LocalDate dataProc = MDate.date();
        filtrosDefault.put("dataProc", dataProc);
        filtrosDefault.put("numeroInicial", "000000000");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("tipoBoleto", "0");

        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = new ArrayList<>();

        List<Long> codigoBanco = getListLong("banco");
        Integer movimento = getInteger("movimento");
        LocalDate dataProc = getLocalDate("dataProc");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidade = getListLong("entidade");
        LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
        Long eaa01id = getLong("eaa01id");
        Long daa01id = getLong("daa01id")
        Boolean enviaEmail = getBoolean("enviaEmail");
        List<Long> ids = get("ids"); // Ids vindo da tela da remessa bancaria

        LocalDate dataAtual = LocalDate.now();


        if(dataProc == null) {
            Integer ano = dataAtual.getYear()
            Integer mes = dataAtual.getMonthValue()
            Integer dia = dataAtual.getDayOfMonth()
            dataProc = LocalDate.of(ano, mes,dia )
        }

        File pasta = new File (".");
        String nomeClasse =  getClass().getPackage().getName().replace(".", "/");
        String caminhoLogo =  pasta.getCanonicalPath().replace("\\" , "/")  + "/samdev/resources/" + nomeClasse + "/LogoItau.png"

        params.put("caminhoLogo", caminhoLogo);
        params.put("dataProc", dataProc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("logoBanco", "LOGO_BANCO  |  999-9");
        params.put("localPagamento", "ATÉ O VENCIMENTO, PAGUE EM QUALQUER BANCO OU CORRESPONDENTE NÃO BANCÁRIO. APÓS O VENCIMENTO, ACESSE ITAU.COM.BR/BOLETOS E PAGUE EM QUALQUER BANCO OU CORRESPONDENTE NÃO BANCÁRIO.");

        Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());

        List<TableMap> daa01s = null;
        List<Long> daa01ids = new ArrayList<>()

        if(eaa01id != null){ // Impressão SRF1009
            daa01ids = buscarIdsDocsSCFPeloIdDocSRF(eaa01id);

            if(daa01ids == null) return null;

            daa01s = buscarDadosBoletoPelosIds(daa01ids);
        }else if(daa01id != null){ // Impressão SCF0101
            daa01ids.add(daa01id);

            if(daa01ids == null) return null;

            daa01s = buscarDadosBoletoPelosIds(daa01ids);
        }else if(ids != null && ids.size() > 0){ // Impressão SCF1001
            daa01s = buscarDadosBoletoPelosIds(ids);
        }else{ // Impressão Relatório
            daa01s = buscarDadosBoleto(codigoBanco, movimento, numeroInicial, numeroFinal, entidade, dataVenc);
        }

        DecimalFormat df = new DecimalFormat("00000000");
        for (TableMap daa01 : daa01s) {
            TableMap tm = new TableMap();

            String campoLivre = campoLivrePorBanco(daa01.getString("abf01numero"), daa01.getString("abf01agencia"), daa01.getString("abf01conta"), daa01.getLong("daa01nossoNum"));
            Long fatorVencimento = definirFatorVencimento(daa01.getDate("daa01dtVctoN"));
            String carteira = daa01.getTableMap("abf01json") == null ? "" : daa01.getTableMap("abf01json").getString("carteira")
            if(carteira == null || carteira == "" ) interromper("O cadastro do banco esta sem carteira, por favor configurar para a impressão do boleto")

            tm.put("abe01nome", daa01.getString("abe01nome"));
            tm.put("abe01ni", daa01.getString("abe01ni"));
            tm.put("daa01dtVctoN", daa01.getDate("daa01dtVctoN"));
            tm.put("daa01valor", daa01.getBigDecimal("daa01valor"));
            tm.put("daa01id", daa01.getLong("daa01id"))
            tm.put("carteira", daa01.getTableMap("abf01json") == null ? "" : daa01.getTableMap("abf01json").getString("carteira") )
            tm.put("tipoBoleto", daa01.getTableMap("abf01json") == null ? "" : daa01.getTableMap("abf01json").getString("cod_carteira") == '1' ? 0 : 1 )
            Locale localeBR = new Locale("pt","BR");
            NumberFormat dinheiro = NumberFormat.getCurrencyInstance(localeBR);

            if(daa01.getTableMap("daa01json") != null && daa01.getTableMap("daa01json").getBigDecimal("multa") != null) {
                tm.put("instrucao1","Após " +daa01.getDate("daa01dtVctoN").format("dd/MM/yyyy") +"  cobrar multa de "+ dinheiro.format(daa01.getTableMap("daa01json").getBigDecimal("multa") ))
            }else {
                tm.put("instrucao1","" )
            }
            if(daa01.getTableMap("daa01json") != null && daa01.getTableMap("daa01json").getBigDecimal("juros") != null) {
                tm.put("instrucao2","Após " +daa01.getDate("daa01dtVctoN").format("dd/MM/yyyy") +"  cobra "+ dinheiro.format(daa01.getTableMap("daa01json").getBigDecimal("juros") ) + " por dia de atraso" )
            }else {
                tm.put("instrucao2","" )
            }

            def t =  daa01.getTableMap("daa01json")
            if(daa01.getTableMap("daa01json") != null && daa01.getTableMap("daa01json").getBigDecimal("desconto") != null && daa01.getTableMap("daa01json").getDate("dt_limite_desc") != null ) {
                tm.put("instrucao4","ATÉ " + daa01.getTableMap("daa01json").getDate("dt_limite_desc").format(PATTERN_DDMMYYYY) + " CONCEDER DESCONTO DE " + dinheiro.format(daa01.getTableMap("daa01json").getBigDecimal("desconto")) )
            }else {
                tm.put("instrucao4","" )
            }

            tm.put("abe01id", daa01.getLong("abe01id"));
            tm.put("daa01nossoNum", df.format(daa01.getLong("daa01nossoNum")));
            tm.put("daa01nossonumdv", daa01.getString("daa01nossoNumDV") != null ? daa01.getString("daa01nossoNumDV") : modulo10(daa01.getLong("daa01nossoNum").toString()) )
            tm.put("abb01data", daa01.getDate("abb01data"));
            tm.put("abb01num", daa01.getInteger("abb01num"));
            tm.put("abb01parcela", daa01.getString("abb01parcela"))
            tm.put("aac10rs", aac10.getAac10rs());
            tm.put("aac10ni", aac10.getAac10ni());
            tm.put("abf01agencia", daa01.getString("abf01agencia"));
            tm.put("abf01conta", daa01.getString("abf01conta"))
            tm.put("abf01digconta", daa01.getString("abf01digconta"))
            tm.put("aah01nome", daa01.getString("aah01nome"));
            tm.put("desconto", daa01.getTableMap("daa01json") != null ? daa01.getTableMap("daa01json").getBigDecimal_Zero("desconto") : new BigDecimal(0));
            tm.put("multa", daa01.getTableMap("daa01json") != null ? daa01.getTableMap("daa01json").getBigDecimal_Zero("multa") : new BigDecimal(0));
            String codigoBarras = montarCodigoBarras(fatorVencimento,daa01.getBigDecimal("daa01valor"),daa01.getString('abf01agencia'),daa01.getString("abf01conta"),df.format(daa01.getLong("daa01nossoNum")),carteira);
            tm.put("codigoBarras", codigoBarras);
            tm.put( "codigoLinhaDigitavel", montarLinhaDigitavel( fatorVencimento,daa01.getBigDecimal("daa01valor"),daa01.getString("abf01agencia"),daa01.getString("abf01conta"),daa01.getString("abf01digconta"),df.format(daa01.getLong("daa01nossoNum")),carteira) )

            TableMap endereco = buscarEndereco(daa01.getLong("abe01id"))
            String enderecoCompleto = ""
            if(endereco != null ) {
                enderecoCompleto += endereco.getString("abe0101endereco") == null ? "" :  endereco.getString("abe0101endereco") +", "
                enderecoCompleto += endereco.getString("abe0101numero") == null ? "" :  endereco.getString("abe0101numero") + " "
                enderecoCompleto += endereco.getString("abe0101bairro") == null ? "" : endereco.getString("abe0101bairro") + " "
                enderecoCompleto += endereco.getString("aag0201nome") == null ? "" : endereco.getString("aag0201nome") + "/"
                enderecoCompleto += endereco.getString("aag02nome") == null ? "" : endereco.getString("aag02nome") + " "
            }
            tm.put("endereco",enderecoCompleto)
            dados.add(tm)
        }
//		if(enviaEmail) {
//			if(dados != null && dados.size() > 0) {
//				enviarEmail(numeroInicial,dados)
//			}else {
//				interromper("Não Foi encontrado nenhum dado!")
//			}
//		}

        return gerarPDF("SCF_BoletoItau",dados);
    }

    private void enviarEmail(Integer numeroInicial, List<TableMap> dados ) {
        JasperReport report = carregarArquivoRelatorio("SCF_BoletoItau");
        JasperPrint print = processarRelatorio(report, dados);

        Long abe01id = dados[0].getLong("abe01id")
        List<Abe0104> emailsDestinos = buscarEmailDestino(abe01id)

        String em =""
        for(email in emailsDestinos) {
            em += " "+email.abe0104eMail
        }

        Aab10 aab10 = obterUsuarioLogado();

        Aab1008 aab1008 = getSession().createCriteria(Aab1008.class)
                .addWhere(Criterions.eq("aab1008user", aab10.aab10id))
                .get()


        if(aab1008 == null ) {
            interromper("O usuario não possui e-mail cadastrado")
        }

        if(aab1008.aab1008email == null) interromper("Não foi encontrado um E-mail para o usuario")


        String corpo = "&nbsp;Prezados<br><br>&nbsp;Segue em anexo o(s) boleto(s) da "+Normalizer.normalize(obterEmpresaAtiva().aac10rs, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","")+" referente a NF "+numeroInicial+".<br><br>&nbsp;Qualquer duvida entrar em contato."

        byte[] bytes = convertPrintToPDF(print);
        ByteArrayDataSource arquivoPdf = new ByteArrayDataSource(bytes, "application/pdf");
        String nomeArquivoPdf = "Boleto Num "+numeroInicial+".pdf";

        if(emailsDestinos == null || emailsDestinos.size()<= 0 ) {
            interromper("E-mail não encontrado!")
        }

        for(emailDestino in emailsDestinos) {
            if(emailDestino.getAbe0104eMail() != null) {
                Email email = new Email(aab1008)
                email.assunto = "Boleto Referente a NF " + numeroInicial

                email.anexar(arquivoPdf,nomeArquivoPdf,nomeArquivoPdf)

                email.corpoMsg = corpo

                email.setEmailDestinoPara(emailDestino.getAbe0104eMail())

                if (aab1008.aab1008assinatura != null) {
                    CAS1010Service cas1010Service = instanciarService(CAS1010Service.class);
                    email.adicionarAssinatura(cas1010Service.converterAssinaturaDoEmailEmInputStream(aab1008.aab1008assinatura));
                }

                email.enviar();
            }
        }
    }

    private List<Abe0104> buscarEmailDestino(Long abe01id) {
        return getSession().createCriteria(Abe0104.class)
                .addJoin(Joins.fetch("abe0104clas"))
                .addJoin(Joins.fetch("abe0104ent"))
                .addWhere(Criterions.eq("aae01codigo", "9002"))
                .addWhere(Criterions.eq("abe01id", abe01id))
                .getList()
    }

    private TableMap buscarEndereco(Long id){
        String sql = " select abe0101endereco, abe0101numero, abe0101bairro, aag0201nome,aag02nome from abe0101 "+
                " inner join abe01 on abe01id = abe0101ent  "+
                " inner join aag0201 on aag0201id = abe0101municipio "+
                " inner join aag02 on aag02id = aag0201uf "+
                " where abe01id = :id and abe0101principal = 1"
        Parametro paramId = Parametro.criar("id", id)
        return getAcessoAoBanco().buscarUnicoTableMap(sql,paramId)
    }

    private List<TableMap> buscarDadosBoleto(List<Long> codigoBanco, Integer movimento, Integer numeroInicial, Integer numeroFinal, List<Long> entidade, LocalDate[] dataVenc) {
        String whereIdCodigoBanco = codigoBanco != null && codigoBanco.size() > 0 ? " and abf01.abf01id IN (:idCodigoBanco)": "";
        Parametro parametroBanco = codigoBanco != null && codigoBanco.size() > 0 ? Parametro.criar("idCodigoBanco", codigoBanco) : null;

        String whereMovimento = movimento != null ? " and daa0102.daa0102movim = :idMovimento": "";
        Parametro parametroMovimento = movimento != null ? Parametro.criar("idMovimento", movimento) : null;

        String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
        String whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " and daa01.daa01dtVctoN >= '" + dataVenc[0] + "' and daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";

        String whereEntidade = entidade != null && entidade.size() > 0 ? " and abe01.abe01id in (:entId) " : ""
        Parametro parametroEntidade = entidade != null && entidade.size() > 0 ? Parametro.criar("entId", entidade) : null

        String sql = " SELECT abe01.abe01codigo, abe01.abe01id, abe01.abe01nome, daa01.daa01json, abe01.abe01ni, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01id, daa01.daa01nossoNum, daa01.daa01nossoNumDV, abb01.abb01num, " +
                " abb01.abb01data,abb01.abb01parcela, abb01.abb01num, abf01.abf01agencia, abf01.abf01conta,abf01.abf01digconta, abf01.abf01digconta, abf01.abf01json, abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim " +
                " FROM daa01 daa01 " +
                " INNER JOIN abb01 abb01 ON abb01id = daa01central " +
                " INNER JOIN abe01 abe01 ON abe01id = abb01ent " +
                " INNER JOIN abf01 abf01 ON abf01id = daa01banco " +
                " LEFT JOIN Aah01 ON aah01id = abb01tipo " +
                " LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id " +
                getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
                whereIdCodigoBanco +
                whereMovimento +
                whereNumero +
                whereVencimento +
                whereEntidade+
                " order by abb01.abb01num, abb01.abb01parcela"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroBanco, parametroMovimento,parametroEntidade);
    }

    private List<TableMap> buscarDadosBoletoPelosIds(List<Long> daa01id) {


        String sql = " SELECT abe01.abe01codigo, abe01.abe01id, abe01.abe01nome, daa01.daa01json, abe01.abe01ni, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01id, daa01.daa01nossoNum, daa01.daa01nossoNumDV, abb01.abb01num, " +
                " abb01.abb01data,abb01.abb01parcela, abb01.abb01num, abf01.abf01agencia, abf01.abf01conta,abf01.abf01digconta, abf01.abf01digconta, abf01.abf01json, abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim " +
                " FROM daa01 daa01 " +
                " INNER JOIN abb01 abb01 ON abb01id = daa01central " +
                " INNER JOIN abe01 abe01 ON abe01id = abb01ent " +
                " INNER JOIN abf01 abf01 ON abf01id = daa01banco " +
                " LEFT JOIN Aah01 ON aah01id = abb01tipo " +
                " LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id " +
                getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
                "and daa01id in (:daa01id) "
        " order by abb01.abb01num, abb01.abb01parcela"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("daa01id", daa01id));
    }

    private List<Long> buscarIdsDocsSCFPeloIdDocSRF(Long eaa01id) {
        String sql = " SELECT daa01id " +
                " FROM Daa01 " +
                " INNER JOIN Abb0102 ON daa01central = abb0102doc " +
                " INNER JOIN Abb01 ON daa01central = abb01id " +
                getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
                " AND abb0102central = (SELECT eaa01central FROM Eaa01 WHERE eaa01id = :eaa01id) " +
                " AND daa01rp = 0 AND daa01previsao = 0 AND daa01banco IS NOT NULL " +
                " AND abb01quita = 0 ";

        List<Long> daa01ids = getAcessoAoBanco().obterListaDeLong(sql, Parametro.criar("eaa01id", eaa01id));
        return daa01ids;
    }



    private String montarCodigoBarras(Long fatorVencimento, BigDecimal valor,String agencia, String conta, String nossoNumero,String carteira) {

        StringBuilder cbLeitor = new StringBuilder();
        cbLeitor.append("341")
        cbLeitor.append("9")
        cbLeitor.append(formatarCampo("" + fatorVencimento, 4))
        cbLeitor.append(formatarCampo("" + valor.multiply(100).intValue(), 10))
        cbLeitor.append(formatarCampo(carteira,3))
        cbLeitor.append(formatarCampo(nossoNumero, 8))
        //def dv = calcularDVInverso("005712345")
        cbLeitor.append(calcularDVInverso(agencia+conta+formatarCampo(carteira,3)+formatarCampo(nossoNumero,8)))
        cbLeitor.append(formatarCampo(agencia, 4))
        cbLeitor.append(conta)
        cbLeitor.append(calcularDVInverso(agencia+conta))
        cbLeitor.append("000")

        DAC = calcularDAC(cbLeitor.toString())
        String codLeitor = cbLeitor.toString().substring(0, 4) + DAC + cbLeitor.toString().substring(4)
        return codLeitor;
    }

    private String montarLinhaDigitavel(Long fatorVencimento, BigDecimal valor,String agencia, String conta, String digConta, String nossoNum,String carteira) {


        peso = 2
        StringBuilder cbDigitavel = new StringBuilder()
        cbDigitavel.append(formatarCampo("341", 3))
        cbDigitavel.append("9")
        cbDigitavel.append(formatarCampo(carteira,3))
        cbDigitavel.append(nossoNum.substring(0, 2))
        cbDigitavel.append(calcularDV(cbDigitavel.toString()))

        cbDigitavel.append(nossoNum.substring(2))
        cbDigitavel.append(calcularDV(agencia+conta+carteira+nossoNum))
        cbDigitavel.append( agencia.substring(0,3) )
        cbDigitavel.append(calcularDV(nossoNum.substring(2)+""+ calcularDV(agencia+conta+carteira+formatarCampo(nossoNum,8)) + agencia.substring(0,3)))

        cbDigitavel.append( agencia.substring(agencia.length() - 1))
        cbDigitavel.append(conta)
        cbDigitavel.append(digConta)
        cbDigitavel.append("000")
        cbDigitavel.append( calcularDV(agencia.substring(agencia.length() - 1) + conta +digConta+"000" ) )

        cbDigitavel.append(DAC)

        cbDigitavel.append(fatorVencimento)
        cbDigitavel.append(formatarCampo("" + valor.multiply(100).intValue(), 10))

        String codDig = cbDigitavel.toString().substring(0, 5) + ".";
        codDig = codDig + cbDigitavel.toString().substring(5, 10) + " ";
        codDig = codDig + cbDigitavel.toString().substring(10, 15) + ".";
        codDig = codDig + cbDigitavel.toString().substring(15, 21) + " ";
        codDig = codDig + cbDigitavel.toString().substring(21, 26) + ".";
        codDig = codDig + cbDigitavel.toString().substring(26, 32) + " ";
        codDig = codDig + cbDigitavel.toString().substring(32, 33) + " ";
        codDig = codDig + cbDigitavel.toString().substring(33);
        return codDig;
    }

    private String campoLivrePorBanco(String numeroBanco, String agencia, String conta, Long nossoNumero) {


        StringBuilder campoLivre = new StringBuilder("");
        campoLivre.append("7")																					//20-20 - fixo 7
        campoLivre.append(formatarCampo(agencia, 5));											                //21-25 - Agência
        campoLivre.append(formatarCampo(conta,9));																//25-33 - Numero Conta
        campoLivre.append(formatarCampo(nossoNumero.toString(),9));														//34-42 - Nosso numero
        campoLivre.append("2");																					//43-43 - tipo cobrança

        return campoLivre.toString();
    }

    private String formatarCampo(String valor, int tamanho) {
        String campo = StringUtils.extractNumbers(valor);
        campo = StringUtils.ajustString(campo, tamanho, '0', true);

        return campo;
    }

//	private Long definirFatorVencimento(LocalDate data) {
//		LocalDate dataBase = DateUtils.parseDate("07/10/1997");
//		Long fator = DateUtils.dateDiff(dataBase, data, ChronoUnit.DAYS);
//		return fator;
//	}
    private Long definirFatorVencimento(LocalDate data) {
        // Novo calculo de fator de vencimento a partir do dia 22/02/2025
        LocalDate dtRef = DateUtils.parseDate("22/02/2025");
        Long fator;

        if(data >= LocalDate.of(2025,2,22)){ // Novo Calculo
            def novoContador = 1000
            def diferencaDatas = DateUtils.dateDiff(dtRef, data, ChronoUnit.DAYS )
            fator = novoContador + diferencaDatas
        }else{ // Calculo Antigo (Apagar depois do dia 22/02)
            LocalDate dataBase = DateUtils.parseDate("07/10/1997");
            fator = DateUtils.dateDiff(dataBase, data, ChronoUnit.DAYS);
        }

        return fator;
    }

    private Integer calcularDAC(String cod) {
        int dac = 0
        int soma = 0
        String valorFixo = "4329876543298765432987654329876543298765432"
        int count = 0
        for(c in cod) {
            Integer numc = Integer.parseInt(c)
            Integer numf = Integer.parseInt(valorFixo.substring(count, count+1))
            soma += (numc * numf)
            count++
        }

        int resto = soma % 11

        if(resto == 0 || resto == 1 || resto == 10 || resto == 11) {
            dac = 1
        }else {
            dac = (11 - resto)
        }

        return dac
    }

    private Integer calcularDVInverso(String cod) {
        int dv = 0
        int soma = 0
        String codInverso = new StringBuilder(cod).reverse().toString()
        for(c in codInverso) {
            Integer num = Integer.parseInt(c);
            num = num * peso;

            if(num >= 10) {
                int somaNum = 0
                for(numc in num.toString()) {
                    numc = Integer.parseInt(numc);
                    somaNum += numc
                }
                soma += somaNum
            }else {
                soma = soma + num;
            }

            peso = peso == 2 ? 1 : peso+1;
        }
        int resto = soma % 10;

        if(resto == 0 ) {
            dv = 0;
        }else {
            dv = 10 - resto;
        }

        return dv;
    }

    private Integer calcularDV(String cod) {
        int dv = 0
        int soma = 0
        for(c in cod) {
            Integer num = Integer.parseInt(c);
            num = num * peso;

            if(num >= 10) {
                int somaNum = 0
                for(numc in num.toString()) {
                    numc = Integer.parseInt(numc);
                    somaNum += numc
                }
                soma += somaNum
            }else {
                soma = soma + num;
            }

            peso = peso == 2 ? 1 : peso+1;
        }
        int resto = soma % 10;

        if(resto == 0 ) {
            dv = 0;
        }else {
            dv = 10 - resto;
        }

        return dv;
    }
    private Integer modulo11DigitoGeral(String codBarras) {
        int dv = 0;
        int soma = 0;
        int peso = 2;
        for(int i = 43; i > 0; i--) {
            Integer num = Integer.parseInt(codBarras.substring(i-1, i));
            num = num * peso;
            soma = soma + num;

            peso = peso == 9 ? 2 : peso+1;
        }
        dv = soma % 11;

        if(dv == 0 || dv == 1 || dv == 10) {
            dv = 1;
        }else {
            dv = 11 - dv;
        }

        return dv;
    }

    private int modulo10(String codigo) {
        int dv = 0;
        int soma = 0;
        int peso = 2;
        codigo = StringUtils.ajustString(codigo, 25, '0', true);

        for(int i = 25; i > 0; i--) {
            int num = Integer.parseInt(codigo.substring(i-1, i));
            num = num * peso;

            if(num >= 10) {
                String n = ""+num;
                int a = Integer.parseInt(n.substring(0, 1));
                int b = Integer.parseInt(n.substring(1));
                soma = soma + (a + b);
            }else {
                soma = soma + num;
            }

            peso = peso == 2 ? 1 : 2;
        }

        dv = soma % 10;

        if(dv != 0)dv = 10 - dv;

        if(dv == 10) dv = 0;

        return dv;
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEJvbGV0byBJdGHDuiAtIEVsIFRlY2giLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDRiAtIEJvbGV0byBJdGHDuiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNDRiAtIEJvbGV0byBJdGHDuiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==