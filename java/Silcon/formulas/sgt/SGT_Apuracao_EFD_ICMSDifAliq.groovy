package Silcon.formulas.sgt

import sam.server.samdev.utils.Parametro

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ea.Eaa01
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase

class SGT_Apuracao_EFD_ICMSDifAliq extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SGT_APURACAO;
    }

    @Override
    public void executar() {
        Edb01 edb01Retorno = null;
        Edb01 edb01 = get("edb01");
        Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)

        selecionarAlinhamento("0031");

        String cpoVl_Icms_UF_Rem = getCampo("0", "vl_icms_uf_rem");
        String cpoVl_Icms_UF_Dest = getCampo("0", "vl_icms_uf_dest");
        String cpoVl_Fcp_UF_Dest = getCampo("0", "vl_fcp_uf_dest");

        LocalDate dtInicial = LocalDate.of(edb01.edb01ano, edb01.edb01mes, 1);
        LocalDate dtFinal = LocalDate.of(edb01.edb01ano, edb01.edb01mes, dtInicial.lengthOfMonth());

        List<Aag02> aag02s = new ArrayList();
        if (edb01.edb01uf != null) {
            aag02s.add(edb01.edb01uf)
        } else {
            aag02s.addAll(buscarEstados());
        }

        if (aag02s != null && aag02s.size() > 0) {
            for (aag02 in aag02s) {
                if (aag02.aag02uf.equalsIgnoreCase("EX")) continue;

                boolean isPrimeiraApur = isPrimeiraApuracao(aag02.aag02id);
                boolean isUFDaEmpresa = false;
                if (aac10.aac10municipio != null) {
                    Aag02 aag02Emp = getSession().get(Aag02.class, aac10.aac10municipio.aag0201uf.aag02id);
                    isUFDaEmpresa = aag02.aag02uf.equals(aag02Emp.aag02uf);
                }

                /**
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 * * * * * * * * * * * * * * * * *  DIFAL  * * * * * * * * * * * * * *
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 */

                /**
                 * Saldo credor anterior - Difal
                 */
                def sdoCredAntDifal = buscarSaldoCredorAnterior(getCampo("0", "sdoCredAntDifal"), edb01.edb01ano, edb01.edb01mes, aag02.aag02id);

                /**
                 * Saídas com débito do imposto - Difal
                 */
                def saidasDebDifal = 0;
                def saidasDebDifal_PrimeiroPeriodo = 0;

                for(int reg = 0; reg < 2; reg++) {
                    String registro; //0-C101 1-D101
                    switch (reg) {
                        case 0: registro = "C101"; break;
                        case 1: registro = "D101"; break;
                        default: registro = null; break;
                    }
                    String nomeCampo;
                    if(isUFDaEmpresa){
                        nomeCampo = cpoVl_Icms_UF_Rem;
                    }else{
                        nomeCampo = cpoVl_Icms_UF_Dest;
                    }

                    def totalImpDifal = buscarTotalImpostoDifalFcp(aag02.aag02id, isUFDaEmpresa, nomeCampo, dtInicial, dtFinal, true, reg);
                    saidasDebDifal = saidasDebDifal + totalImpDifal;

                    def totalImpPrimeiroPeriodoDifal = buscarTotalImpostoPrimeiroPeriodoDifalFcp(aag02.aag02id, isUFDaEmpresa, nomeCampo, dtInicial, dtFinal, true, reg);
                    saidasDebDifal_PrimeiroPeriodo = saidasDebDifal_PrimeiroPeriodo + totalImpPrimeiroPeriodoDifal;
                }

                /**
                 * Ajustes de débito
                 */
                def ajustesDebitosDifal = buscarAjustesDebitosICMSDifalFcp(0, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Créditos do ICMS referente a difal
                 */
                def credDifal = 0;
                for(int reg = 0; reg < 2; reg++) {
                    String registro; //0-C101 1-D101
                    switch (reg) {
                        case 0: registro = "C101"; break;
                        case 1: registro = "D101"; break;
                        default: registro = null; break;
                    }

                    String nomeCampo;
                    if(isUFDaEmpresa){
                        nomeCampo = cpoVl_Icms_UF_Dest;
                    }else{
                        nomeCampo = cpoVl_Icms_UF_Rem;
                    }

                    def totalImpDifal = buscarTotalImpostoDifalFcp(aag02.aag02id, isUFDaEmpresa, nomeCampo, dtInicial, dtFinal, false, reg);
                    credDifal = credDifal + totalImpDifal;
                }

                /**
                 * Ajustes de crédito
                 */
                def ajustesCreditosDifal = buscarAjustesCreditosICMSDifalFcp(0, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Saldo Devedor antes das Deduções
                 */
                def soma1Difal = saidasDebDifal + ajustesDebitosDifal;
                def soma2Difal = sdoCredAntDifal + credDifal + ajustesCreditosDifal;
                def sdoDevDifal = soma1Difal - soma2Difal;
                if(sdoDevDifal < 0) sdoDevDifal = 0;

                /**
                 * Deduções - Difal
                 */
                def deducoesDifal = buscarDeducoesICMSDifalFcp(0, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Valor recolhido ou a recolher
                 */
                def vlrRecolDifal = sdoDevDifal - deducoesDifal;
                if(vlrRecolDifal < 0) vlrRecolDifal = 0;

                /**
                 * Saldo credor a transportar - Difal
                 */
                def sdoCredDifal = soma2Difal - soma1Difal;
                if(sdoCredDifal < 0) sdoCredDifal = 0;

                /**
                 * Valores extra-apuração - Difal
                 */
                def extraDifal = isPrimeiraApur ? saidasDebDifal_PrimeiroPeriodo : 0;
                def somaExtraDifal = buscarExtraApurICMS(0, aag02.aag02id, dtInicial, dtFinal);
                extraDifal = extraDifal + somaExtraDifal;

                /**
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 * * * * * * * * * * * * * * * * * *  FCP  * * * * * * * * * * * * * *
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 */

                /**
                 * Saldo credor anterior - Fcp
                 */
                def sdoCredAntFcp = buscarSaldoCredorAnterior(getCampo("0", "sdoCredAntFcp"), edb01.edb01ano, edb01.edb01mes, aag02.aag02id);

                /**
                 * Saídas com débito do imposto FCP
                 */
                def saidasDebFcp = 0;
                def saidasDebFcp_PrimeiroPeriodo = 0;

                for(int reg = 0; reg < 2; reg++) {
                    String registro; //0-C101 1-D101
                    switch (reg) {
                        case 0: registro = "C101"; break;
                        case 1: registro = "D101"; break;
                        default: registro = null; break;
                    }

                    def totalImpFcp = buscarTotalImpostoDifalFcp(aag02.aag02id, isUFDaEmpresa, cpoVl_Fcp_UF_Dest, dtInicial, dtFinal, true, reg);
                    saidasDebFcp = saidasDebFcp + totalImpFcp;

                    def totalImpPrimeiroPeriodoFcp = buscarTotalImpostoPrimeiroPeriodoDifalFcp(aag02.aag02id, isUFDaEmpresa, cpoVl_Fcp_UF_Dest, dtInicial, dtFinal, true, reg);
                    saidasDebFcp_PrimeiroPeriodo = saidasDebFcp_PrimeiroPeriodo + totalImpPrimeiroPeriodoFcp;
                }

                /**
                 * Ajustes de débito
                 */
                def ajustesDebitosFcp = buscarAjustesDebitosICMSDifalFcp(1, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Entradas com crédito do imposto - FCP
                 */
                def credFcp = 0;
                for(int reg = 0; reg < 2; reg++) {
                    String registro; //0-C101 1-D101
                    switch (reg) {
                        case 0: registro = "C101"; break;
                        case 1: registro = "D101"; break;
                        default: registro = null; break;
                    }

                    def totalImpFcp = buscarTotalImpostoDifalFcp(aag02.aag02id, isUFDaEmpresa, cpoVl_Fcp_UF_Dest, dtInicial, dtFinal, false, reg);
                    credFcp = credFcp + totalImpFcp;
                }

                /**
                 *  Ajustes de crédito FCP
                 */
                def ajustesCreditosFcp = buscarAjustesCreditosICMSDifalFcp(1, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Saldo Devedor antes das Deduções
                 */
                def soma1Fcp = saidasDebFcp + ajustesDebitosFcp;
                def soma2Fcp = sdoCredAntFcp + credFcp + ajustesCreditosFcp;
                def sdoDevFcp = soma1Fcp - soma2Fcp;
                if(sdoDevFcp < 0) sdoDevFcp = 0;

                /**
                 * Deduções - FCP
                 */
                def deducoesFcp = buscarDeducoesICMSDifalFcp(1, aag02.aag02id, dtInicial, dtFinal);

                /**
                 * Valor recolhido ou a recolher - FCP
                 */
                def vlrRecolFcp = sdoDevFcp - deducoesFcp;
                if(vlrRecolFcp < 0) vlrRecolFcp = 0;

                /**
                 * Saldo credor a transportar - FCP
                 */
                def sdoCredFcp = soma2Fcp - soma1Fcp;
                if(sdoCredFcp < 0) sdoCredFcp = 0;

                /**
                 * Valores extra-apuração - FCP
                 */
                def extraFcp = isPrimeiraApur ? saidasDebFcp_PrimeiroPeriodo : 0;
                def somaExtraFcp = buscarExtraApurICMS(1, aag02.aag02id, dtInicial, dtFinal);
                extraFcp = extraFcp + somaExtraFcp;

                if (isUFDaEmpresa || (
                        !sdoCredAntDifal.equals(0) || !saidasDebDifal.equals(0) || !ajustesDebitosDifal.equals(0) || !credDifal.equals(0) ||
                                !ajustesCreditosDifal.equals(0) || !sdoDevDifal.equals(0) || !deducoesDifal.equals(0) || !vlrRecolDifal.equals(0) ||
                                !sdoCredDifal.equals(0) || !extraDifal.equals(0) ||
                                !sdoCredAntFcp.equals(0) || !saidasDebFcp.equals(0) || !ajustesDebitosFcp.equals(0) || !credFcp.equals(0) ||
                                !ajustesCreditosFcp.equals(0) || !sdoDevFcp.equals(0) || !deducoesFcp.equals(0) || !vlrRecolFcp.equals(0) ||
                                !sdoCredFcp.equals(0) || !extraFcp.equals(0))) {

                    Edb01 edb01Icms = new Edb01();
                    edb01Icms.edb01ano = edb01.edb01ano;
                    edb01Icms.edb01mes = edb01.edb01mes;
                    edb01Icms.edb01tipo = edb01.edb01tipo;
                    edb01Icms.edb01uf = aag02;


                    TableMap edb01json;
                    if (edb01Icms.edb01json != null) {
                        edb01json = edb01Icms.edb01json;
                    } else {
                        edb01json = new TableMap();
                    }

                    /**
                     * Preenche valor dos campos Difal
                     */
                    edb01json.put(getCampo("0", "sdoCredAntDifal"), sdoCredAntDifal);
                    edb01json.put(getCampo("0", "debDifal"), saidasDebDifal);
                    edb01json.put(getCampo("0", "ajuDebDifal"), ajustesDebitosDifal);
                    edb01json.put(getCampo("0", "credDifal"), credDifal);
                    edb01json.put(getCampo("0", "ajuCredDifal"), ajustesCreditosDifal);
                    edb01json.put(getCampo("0", "sdoDevDifal"), sdoDevDifal);
                    edb01json.put(getCampo("0", "deducoesDifal"), deducoesDifal);
                    edb01json.put(getCampo("0", "vlrRecolDifal"), vlrRecolDifal);
                    edb01json.put(getCampo("0", "sdoCredDifal"), sdoCredDifal);
                    edb01json.put(getCampo("0", "extraDifal"), extraDifal);

                    /**
                     * Preenche valor dos campos Fcp
                     */
                    edb01json.put(getCampo("0", "sdoCredAntFcp"), sdoCredAntFcp);
                    edb01json.put(getCampo("0", "debFcp"), saidasDebFcp);
                    edb01json.put(getCampo("0", "ajuDebFcp"), ajustesDebitosFcp);
                    edb01json.put(getCampo("0", "credFcp"), credFcp);
                    edb01json.put(getCampo("0", "ajuCredFcp"), ajustesCreditosFcp);
                    edb01json.put(getCampo("0", "sdoDevFcp"), sdoDevFcp);
                    edb01json.put(getCampo("0", "deducoesFcp"), deducoesFcp);
                    edb01json.put(getCampo("0", "vlrRecolFcp"), vlrRecolFcp);
                    edb01json.put(getCampo("0", "sdoCredFcp"), sdoCredFcp);
                    edb01json.put(getCampo("0", "extraFcp"), extraFcp);

                    edb01Icms.edb01json = edb01json;

                    Boolean contemApuracao = validaContemApuracao(edb01json)

                    if (contemApuracao) {
                        getSamWhere().setDefaultValues(edb01Icms)
                        getSession().persist(edb01Icms);
                        edb01Retorno = edb01Icms;
                    }
                }
            }
        }

        put("edb01", edb01Retorno);
    }

    private List<Aag02> buscarEstados(){
        return getSession().createQuery(" SELECT * FROM Aag02 ORDER BY aag02uf ").getList(ColumnType.ENTITY);
    }

    private boolean isPrimeiraApuracao(Long aag02id) {
        Integer count = getSession().createQuery(" SELECT COUNT(edb01id) FROM Edb01 WHERE edb01uf = :aag02id " + getSamWhere().getWherePadrao("AND", Edb01.class)).setParameter("aag02id", aag02id).getUniqueResult(ColumnType.INTEGER);
        return count == null || count == 0;
    }

    private BigDecimal buscarSaldoCredorAnterior(String campo, Integer ano, Integer mes, Long aag02id) {
        String field = Fields.numMeses("edb01mes", "edb01ano").toString();
        int numMeses = (ano * 12) + mes;

        String sql = " SELECT jGet(edb01json." + campo + ") AS valor FROM Edb01 WHERE " + field + " < :numMeses " +
                getSamWhere().getWherePadrao("AND", Edb01.class) + " ORDER BY edb01ano DESC, edb01mes DESC";

        def valor = getSession().createQuery(sql).setParameter("numMeses", numMeses).setMaxResult(1).getUniqueResult(ColumnType.BIG_DECIMAL);
        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarTotalImpostoDifalFcp(Long aag02id, boolean isUFDaEmpresa, String nomeCampo, LocalDate dtInicial, LocalDate dtFinal, boolean isSaida, int reg){
        String cpoData = isSaida ? " abb01data " : " eaa01esData ";
        String whereUF = isUFDaEmpresa ? "" : "AND aag02id = :aag02id\n";

        String sql = "SELECT SUM(CAST(eaa0103json ->> '" + nomeCampo + "' AS NUMERIC))\n" +
                "FROM Eaa01\n" +
                "INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Aaj15 ON aaj15id = eaa0103cfop\n" +
                "INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND eaa01esMov = :eaa01esMov\n" +
                "AND CAST(eaa0103json ->> '" + nomeCampo + "' AS numeric) > 0\n" +
                "AND eaa01cancData IS NULL AND " + cpoData + " BETWEEN :dtInicial AND :dtFinal\n" +
                whereUF +
                "AND aaj03efd NOT IN ('01', '07')\n" +
                "AND eaa01iEfdIcms = :eaa01iEfdIcms\n" +
                "AND aah01modelo IN (:modelos)\n" +
                "AND eaa0101principal = 1"

        Parametro p1 = Parametro.criar("eaa01esMov", isSaida ? Eaa01.ESMOV_SAIDA : Eaa01.ESMOV_ENTRADA)
        Parametro p2 = Parametro.criar("dtInicial", dtInicial)
        Parametro p3 = Parametro.criar("dtFinal", dtFinal)
        Parametro p4 = Parametro.criar("eaa01iEfdIcms", 1)
        Parametro p5 = Parametro.criar("modelos", buscarModelosTotalImposto(reg))
        Parametro p6 = whereUF.length() > 0 ? Parametro.criar("aag02id", aag02id) : null

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5,p6)

        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarTotalImpostoPrimeiroPeriodoDifalFcp(Long aag02id, boolean isUFDaEmpresa, String nomeCampo, LocalDate dtInicial, LocalDate dtFinal, boolean isSaida, int reg){
        String cpoData = isSaida ? " abb01data " : " eaa01esData ";
        String whereUF = isUFDaEmpresa ? "" : "AND aag02id = :aag02id\n";

        String sql = "SELECT SUM(CAST(eaa0103json ->> '" + nomeCampo + "' AS NUMERIC))\n" +
                "FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Aaj15 ON aaj15id = eaa0103cfop\n" +
                "INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND eaa01esMov = :eaa01esMov\n" +
                "AND CAST(eaa0103json ->> '" + nomeCampo + "' AS numeric) > 0\n" +
                "AND eaa01cancData IS NULL AND " + cpoData + " BETWEEN :dtInicial AND :dtFinal\n" +
                whereUF +
                "AND aaj03efd IN ('01', '07')\n" +
                "AND eaa01iEfdIcms = :eaa01iEfdIcms\n" +
                "AND aah01modelo IN (:modelos)"

        Parametro p1 = Parametro.criar("eaa01esMov", isSaida ? Eaa01.ESMOV_SAIDA : Eaa01.ESMOV_ENTRADA)
        Parametro p2 = Parametro.criar("dtInicial", dtInicial)
        Parametro p3 = Parametro.criar("dtFinal", dtFinal)
        Parametro p4 = Parametro.criar("eaa01iEfdIcms", 1)
        Parametro p5 = Parametro.criar("modelos", buscarModelosTotalImposto(reg))
        Parametro p6 = whereUF.length() > 0 ? Parametro.criar("aag02id", aag02id) : null

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5,p6)

        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarAjustesDebitosICMSDifalFcp(int imposto, Long aag02id, LocalDate dtInicial, LocalDate dtFinal){
        String sql = "SELECT SUM(eaa01035valor) AS valor\n" +
                "FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Eaa01035 ON eaa01035item = eaa0103item\n" +
                "LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) AND\n" +
                "SUBSTR(aaj17codigo, 3, 1) IN (:ter) AND\n" +
                "SUBSTR(aaj17codigo, 4, 1) IN (:qua)\n" +
                "AND eaa01iEfdIcms = 1\n" +
                "AND aag02id = :aag02id AND eaa01cancData IS NULL\n" +
                "AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63')\n" +
                "AND eaa0101principal = 1 AND aag02id = :aag02id"

        Parametro p1 = Parametro.criar("ter", imposto == 0 ? Utils.list("2") : imposto == 1 ? Utils.list("3") : Utils.list("2", "3"))
        Parametro p2 = Parametro.criar("qua", Utils.list("0", "1"))
        Parametro p3 = Parametro.criar("dtInicial", dtInicial,)
        Parametro p4 = Parametro.criar("dtFinal", dtFinal)
        Parametro p5 = Parametro.criar("aag02id", aag02id)

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5)

        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarAjustesCreditosICMSDifalFcp(int imposto, Long aag02id, LocalDate dtInicial, LocalDate dtFinal){
        String sql = "SELECT SUM(eaa01035valor) AS valor\n" +
                "FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Eaa01035 ON eaa01035item = eaa0103item\n" +
                "LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) AND\n" +
                "SUBSTR(aaj17codigo, 3, 1) IN (:ter) AND\n" +
                "SUBSTR(aaj17codigo, 4, 1) IN (:qua)\n" +
                "AND eaa01iEfdIcms = 1\n" +
                "AND aag02id = :aag02id AND eaa01cancData IS NULL\n" +
                "AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63')\n" +
                "AND eaa0101principal = 1 AND aag02id = :aag02id"

        Parametro p1 = Parametro.criar("ter", imposto == 0 ? Utils.list("2") : imposto == 1 ? Utils.list("3") : Utils.list("2", "3"))
        Parametro p2 = Parametro.criar("qua", Utils.list("2", "3"))
        Parametro p3 = Parametro.criar("dtInicial", dtInicial,)
        Parametro p4 = Parametro.criar("dtFinal", dtFinal)
        Parametro p5 = Parametro.criar("aag02id", aag02id)

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5)

        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarDeducoesICMSDifalFcp(int imposto, Long aag02id, LocalDate dtInicial, LocalDate dtFinal){
        String sql = "SELECT SUM(eaa01035valor) AS valor\n" +
                "FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Eaa01035 ON eaa01035item = eaa0103item\n" +
                "LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) AND\n" +
                "SUBSTR(aaj17codigo, 3, 1) IN (:ter) AND\n" +
                "SUBSTR(aaj17codigo, 4, 1) IN (:qua)\n" +
                "AND eaa01iEfdIcms = 1\n" +
                "AND aag02id = :aag02id AND eaa01cancData IS NULL\n" +
                "AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63')\n" +
                "AND eaa0101principal = 1 AND aag02id = :aag02id"

        Parametro p1 = Parametro.criar("ter", imposto == 0 ? Utils.list("2") : imposto == 1 ? Utils.list("3") : Utils.list("2", "3"))
        Parametro p2 = Parametro.criar("qua", Utils.list("2", "3"))
        Parametro p3 = Parametro.criar("dtInicial", dtInicial,)
        Parametro p4 = Parametro.criar("dtFinal", dtFinal)
        Parametro p5 = Parametro.criar("aag02id", aag02id)

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5)

        return valor == null ? 0 : valor;
    }

    private BigDecimal buscarExtraApurICMS(int imposto, Long aag02id, LocalDate dtInicial, LocalDate dtFinal){
        String sql = "SELECT SUM(eaa01035valor) AS valor\n" +
                "FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc\n" +
                "INNER JOIN Abb01 ON abb01id = eaa01central\n" +
                "INNER JOIN Aah01 ON aah01id = abb01tipo\n" +
                "INNER JOIN Eaa01035 ON eaa01035item = eaa0103item\n" +
                "LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id\n" +
                "INNER JOIN Eaa0101 ON eaa0101doc = eaa01id\n" +
                "INNER JOIN Aag0201 ON aag0201id = eaa0101municipio\n" +
                "INNER JOIN Aag02 ON aag02id = aag0201uf\n" +
                getSamWhere().getWherePadrao("WHERE", Eaa01.class) + "\n" +
                "AND ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) AND\n" +
                "SUBSTR(aaj17codigo, 3, 1) IN (:ter) AND\n" +
                "SUBSTR(aaj17codigo, 4, 1) IN (:qua)\n" +
                "AND eaa01iEfdIcms = 1\n" +
                "AND aag02id = :aag02id AND eaa01cancData IS NULL\n" +
                "AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63')\n" +
                "AND eaa0101principal = 1 AND aag02id = :aag02id"

        Parametro p1 = Parametro.criar("ter", Utils.list("2"))
        Parametro p2 = Parametro.criar("qua", Utils.list("5"))
        Parametro p3 = Parametro.criar("dtInicial", dtInicial,)
        Parametro p4 = Parametro.criar("dtFinal", dtFinal)
        Parametro p5 = Parametro.criar("aag02id", aag02id)

        BigDecimal valor = getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5)

        return valor == null ? 0 : valor;
    }

    private List<String> buscarModelosTotalImposto(int reg) {
        switch (reg) { //0-C100 1-D100
            case 0: return Utils.list("01", "1B", "04", "55", "65");
            case 1: return Utils.list("07", "08", "8B", "09", "10", "11", "26", "27", "57", "67", "63");
            default: return null;
        }
    }

    private Boolean validaContemApuracao(TableMap edb01json) {
        Boolean contemApuracao = false

        for (json in edb01json) {
            if (json.getValue() != 0) {
                contemApuracao = true
                break
            }
        }

        return contemApuracao
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==