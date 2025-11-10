package multitec.formulas.sgt.nfp

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.criteria.ClientCriteriaConvert
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0113
import sam.server.samdev.formula.FormulaBase

class NF_Paulista_2 extends FormulaBase {
    ClientCriterion whereTipo
    ClientCriterion whereData
    ClientCriterion whereEnt
    ClientCriterion whereNum

    DateTimeFormatter ptBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Override
    FormulaTipo obterTipoFormula() {
        return FormulaTipo.SGT_EXPORTAR_DOCUMENTOS_SRF
    }

    @Override
    void executar() {
        whereTipo = get("whereTipo")
        whereData = get("whereData")
        whereEnt = get("whereEnt")
        whereNum = get("whereNum")

        selecionarAlinhamento("0020")

        def txt = new TextFile("|", true)

        def aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)

        def eaa01s = buscarDocumentosNFP(whereTipo, whereData, whereNum, whereEnt)

        gerarLeiauteModelos(txt, eaa01s, aac10)

        put("dadosArquivo", txt)
    }

    void gerarLeiauteModelos(TextFile txt, List<Eaa01> eaa01s, Aac10 aac10) {
        def totGeralNF = BigDecimal.ZERO
        def totalTipo20 = 0
        def totalTipo21 = 0
        def totalTipo22 = 0

        def dataInicial = whereData.getValor1()
        def dataFinal = whereData.getValor2()

        /** Tipo 10 */
        txt.print("10")
        txt.print("1.00")
        txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14)
        txt.print(dataInicial)
        txt.print(dataFinal)

        txt.newLine()

        for (eaa01 in eaa01s) {
        	Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));

            /** Tipo 20 **/
            txt.print("20")
            txt.print(eaa01.eaa01central.abb01serie == "DU" ? "2" : eaa01.eaa01central.abb01serie == "U" ? "3" : "1")
            txt.print(null)
            txt.print(eaa01.eaa01central.abb01num)
            txt.print(eaa01.eaa01central.abb01data.format(ptBr))
            txt.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ptBr))
            txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ni), eaa0102.eaa0102ti == 0 ? 14 : 11)
            txt.print(eaa0102.eaa0102nome)

            def endPrinc = buscarEnderecoDocumento(eaa01.eaa01id, 0)
            txt.print(endPrinc.eaa0101endereco)
            txt.print(endPrinc.eaa0101numero)
            txt.print(endPrinc.eaa0101complem)
            txt.print(endPrinc.eaa0101bairro)
            txt.print(endPrinc.eaa0101municipio == null ? null : endPrinc.eaa0101municipio.aag0201nome)
            txt.print(endPrinc.eaa0101municipio == null ? null : endPrinc.eaa0101municipio.aag0201uf.aag02uf)
            txt.print(endPrinc.eaa0101cep)
            txt.print(eaa0102.eaa0102fone)
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "17")), 2))
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "18")), 2))
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "19")), 2))
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "20")), 2))
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "21")), 2))
            txt.print(null)
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "23")), 2))
            txt.print(null)
            txt.print(null)

            def endEntre = buscarEnderecoDocumento(eaa01.eaa01id, 1)
            txt.print(endEntre == null ? "2" : "1")
            txt.print(endEntre == null ? null : endEntre.eaa0101endereco)
            txt.print(endEntre == null ? null : endEntre.eaa0101numero)
            txt.print(endEntre == null ? null : endEntre.eaa0101complem)
            txt.print(endEntre == null ? null : endEntre.eaa0101bairro)
            txt.print(endEntre == null ? null : endEntre.eaa0101municipio == null ? null : endEntre.eaa0101municipio.aag0201nome)
            txt.print(endEntre == null ? null : endEntre.eaa0101municipio == null ? null : endEntre.eaa0101municipio.aag0201uf.aag02uf)

            def isAPrazo = false
            def eaa0113s = buscarFinanceirosDocumentos(eaa01.eaa01id)
            if (eaa0113s.size() > 1) {
                isAPrazo = true
            } else {
                def eaa0113 = eaa0113s.stream()
									  .filter({eaa0113 -> eaa0113.eaa0113clasParc == 0})
                        			  .min({eaa0113 -> eaa0113.eaa0113dtVctoN}).orElse(null)

                if (eaa0113 != null) isAPrazo = eaa0113.eaa0113dtVctoN != eaa01.eaa01central.abb01data
            }
            txt.print(isAPrazo ? "1" : "0")

            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "34")), 2))
            txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("20", "35")), 2))
            txt.print(isAPrazo ? eaa0113s.size() : "0")

            txt.newLine()

            totalTipo20++
            totGeralNF = totGeralNF.add(eaa01.eaa01json.getBigDecimal(getCampo("20", "23")))

            /** Tipo 21 **/
            def eaa0103s = buscarItensDoDocumento(eaa01.eaa01id)
            for (eaa0103 in eaa0103s) {
                txt.print("21")
                txt.print(eaa0103.eaa0103seq)
                txt.print(eaa0103.eaa0103item.abm01codigo)
                txt.print(eaa0103.eaa0103tipo == 3 ? "0" : eaa0103.eaa0103tipo == 2 ? "1" : "2")
                txt.print(eaa0103.eaa0103descr, eaa0103.eaa0103descr.length() > 120 ? 120 : eaa0103.eaa0103descr.length())
                txt.print(eaa0103.eaa0103umComl == null ? null : eaa0103.eaa0103umComl.aam06codigo)
                txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("21", "7")), 3))
                txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("21", "8")), 2))
                txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("21", "9")), 2))

                txt.newLine()
                totalTipo21++
            }

            /** Tipo 21 **/
            for (eaa0113 in eaa0113s) {
                txt.print("22")
                txt.print(formatarValor(eaa0113.eaa0113valor, 2))
                txt.print(eaa0113.eaa0113dtVctoN.format(ptBr))

                txt.newLine()
                totalTipo22++
            }
        }

        /** Tipo 90 **/
        txt.print("90")
        txt.print(totalTipo20, 5)
        txt.print(totalTipo21, 5)
        txt.print(totalTipo22, 5)
        txt.print(totGeralNF)

        txt.newLine()
    }

    String formatarValor(BigDecimal valor, int casasDecimais) {
        def format = NumberFormat.getInstance(new Locale("pt", "BR"))
        format.setGroupingUsed(false)
        format.setMinimumFractionDigits(casasDecimais)
        format.setMaximumFractionDigits(casasDecimais)
        return format.format(valor)
    }

    List<Eaa01> buscarDocumentosNFP(ClientCriterion whereTipo, ClientCriterion whereData, ClientCriterion whereNum, ClientCriterion whereEnt) {
        Criterion critTipo = whereTipo != null ? ClientCriteriaConvert.convertCriterion(whereTipo) : Criterions.isTrue()
        Criterion critData = whereData != null ? ClientCriteriaConvert.convertCriterion(whereData) : Criterions.isTrue()
        Criterion critNum = whereNum != null ? ClientCriteriaConvert.convertCriterion(whereNum) : Criterions.isTrue()
        Criterion critEnt = whereEnt != null ? ClientCriteriaConvert.convertCriterion(whereEnt) : Criterions.isTrue()

        return getSession().createCriteria(Eaa01.class).alias("eaa01")
                .addJoin(Joins.fetch("eaa01.eaa01central").alias("abb01"))
                .addJoin(Joins.fetch("abb01.abb01tipo").alias("aah01"))
                .addJoin(Joins.fetch("abb01.abb01ent").alias("abe01"))
                .addJoin(Joins.fetch("eaa01.eaa01cancMotivo").left(true).alias("aae11"))
                .addJoin(Joins.fetch("abb01.abb01operCod").left(true).alias("abb10"))
                .addWhere(critTipo).addWhere(critData).addWhere(critNum).addWhere(critEnt)
                .getList(ColumnType.ENTITY)
    }

    List<Eaa0103> buscarItensDoDocumento(Long eaa01id) {
        return getSession().createCriteria(Eaa0103.class)
                .addJoin(Joins.fetch("eaa0103.eaa0103item").alias("abm01"))
                .addJoin(Joins.fetch("eaa0103.eaa0103cfop").left(true).alias("aaj15"))
                .addJoin(Joins.fetch("eaa0103.eaa0103ncm").left(true).alias("abg01"))
                .addJoin(Joins.fetch("eaa0103.eaa0103umComl").left(true).alias("aam06"))
                .addJoin(Joins.fetch("eaa0103.eaa0103cstIcms").left(true).alias("aaj10"))
                .addWhere(Criterions.eq("eaa0103.eaa0103doc", eaa01id))
                .getList(ColumnType.ENTITY)
    }

    Eaa0101 buscarEnderecoDocumento(Long eaa01id, Integer tipo) {
        def critTipo = tipo == 0 ? Criterions.eq("eaa0101.eaa0101principal", Eaa0101.SIM)
                : Criterions.eq("eaa0101.eaa0101entrega", Eaa0101.SIM)

        return getSession().createCriteria(Eaa0101.class)
                .addJoin(Joins.fetch("eaa0101.eaa0101municipio").left(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
                .addJoin(Joins.fetch("eaa0101.eaa0101pais").left(true).alias("aag01"))
                .addWhere(Criterions.eq("eaa0101.eaa0101doc", eaa01id))
                .addWhere(critTipo)
                .get(ColumnType.ENTITY)
    }

    List<Eaa0113> buscarFinanceirosDocumentos(Long eaa01id) {
        return getSession().createCriteria(Eaa0113.class).alias("eaa0113")
                .addJoin(Joins.fetch("eaa0113.eaa0113tipo").alias("aah01"))
                .addWhere(Criterions.eq("eaa0113.eaa0113doc", eaa01id))
                .getList(ColumnType.ENTITY)
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==