package multitec.formulas.sgt.nfp

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.criteria.ClientCriteriaConvert
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aah20
import sam.model.entities.aa.Aaj15
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

class NF_Paulista_1_1A extends FormulaBase {
    ClientCriterion whereTipo
    ClientCriterion whereData
    ClientCriterion whereEnt
    ClientCriterion whereNum

    DateTimeFormatter stamp = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
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
        def totalTipo20 = 0
        def totalTipo30 = 0
        def totalTipo40 = 0
        def totalTipo50 = 0

        def dataInicial = whereData.getValor1()
        def dataFinal = whereData.getValor2()

        /** Tipo 10 */
        txt.print("10")
        txt.print("1,00")
        txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14)
        txt.print(dataInicial)
        txt.print(dataFinal)

        txt.newLine()

        /** Tipos 20, 30, 40  50 */
        for (eaa01 in eaa01s) {
        	Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));

            /** Tipo 20 */
            txt.print("20")
            txt.print(eaa01.eaa01iNFP == 3 ? "R" : eaa01.eaa01iNFP == 4 ? "C" : "I")

            if (eaa01.eaa01iNFP == 4) {
                if (eaa01.eaa01cancMotivo != null) {
                    if (eaa01.eaa01cancMotivo.aae11descr.length() > 255) {
                        txt.print(eaa01.eaa01cancMotivo.aae11descr, 255)
                    } else {
                        txt.print(eaa01.eaa01cancMotivo)
                    }
                } else {
                    txt.print(StringUtils.space(15))
                }
            } else {
                txt.print(null)
            }

            txt.print(eaa01.eaa01operDescr)
            txt.print(eaa01.eaa01central.abb01serie == null ? "0" : StringUtils.extractNumbers(eaa01.eaa01central.abb01serie))
            txt.print(eaa01.eaa01central.abb01num)
            txt.print(eaa01.eaa01central.abb01data.format(ptBr))

            def esData = eaa01.eaa01esData
            def esHora = eaa01.eaa01esHora
            if (esData != null) {
                def esDataHora = LocalDateTime.of(esData, MDate.time())
                if (esHora != null) {
                    esDataHora = LocalDateTime.of(esData, esHora)
                }
                txt.print(esDataHora.format(stamp))
            } else {
                txt.print(null)
            }

            txt.print(eaa01.eaa01esMov)

            def cfop = buscarCfopItemDocumento(eaa01.eaa01id)
            txt.print(cfop != null ? cfop.aaj15codigo : null)

            txt.print(eaa0102.eaa0102ieST == null ? null : StringUtils.extractNumbers(eaa0102.eaa0102ieST))
            txt.print(eaa0102.eaa0102im == null ? null : StringUtils.extractNumbers(eaa0102.eaa0102im))

            def endPrinc = buscarEnderecoPrincipalDocumento(eaa01.eaa01id)
            if (endPrinc == null) throw new ValidacaoException("Não foi informado o endereço principal para o documento: " + eaa01.eaa01central.abb01num)
            if (endPrinc.eaa0101municipio != null && endPrinc.eaa0101municipio.aag0201uf.aag02uf.equalsIgnoreCase("EX")) {
                txt.print(null)
            } else {
                txt.print(eaa0102.eaa0102ni == null ? null : StringUtils.ajustString(StringUtils.extractNumbers(eaa0102.eaa0102ni), eaa0102.eaa0102ti == 0 ? 14 : 11))
            }

            txt.print(eaa0102.eaa0102nome ?: " ")
            txt.print(endPrinc.eaa0101endereco ?: " ")
            txt.print(endPrinc.eaa0101numero)
            txt.print(endPrinc.eaa0101complem)
            txt.print(endPrinc.eaa0101bairro ?: " ")

            if (endPrinc.eaa0101municipio != null && endPrinc.eaa0101municipio.aag0201uf.aag02uf.equalsIgnoreCase("EX")) {
                txt.print("EXTERIOR")
                txt.print("EX")
            } else {
                txt.print(endPrinc.eaa0101municipio == null ? null : endPrinc.eaa0101municipio.aag0201nome)
                txt.print(endPrinc.eaa0101municipio == null ? null : endPrinc.eaa0101municipio.aag0201uf.aag02uf, 2)
            }

            txt.print(endPrinc.eaa0101cep, 8)
            txt.print(endPrinc.eaa0101pais == null ? null : endPrinc.eaa0101pais.aag01nome)
            txt.print(eaa0102.eaa0102fone == null ? null : eaa0102.eaa0102ddd == null ? eaa0102.eaa0102fone : eaa0102.eaa0102ddd + eaa0102.eaa0102fone)
            txt.print(eaa0102.eaa0102ie == null ? null : StringUtils.extractNumbers(eaa0102.eaa0102ie))

            txt.newLine()
            totalTipo20++

            if (eaa01.eaa01iNFP != 4) {
                /** TIpo 30 */

                def totalRetorno = BigDecimal.ZERO
                def eaa0103s = buscarItensDoDocumento(eaa01.eaa01id)
                for (Eaa0103 eaa0103 : eaa0103s) {
                    if (eaa0103.eaa0103retInd > 1) continue
                    if (eaa0103.eaa0103retInd == 1) totalRetorno totalRetorno.add(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "vProd")))

                    txt.print("30")
                    txt.print(eaa0103.eaa0103item.abm01codigo)

                    def descr = eaa0103.eaa0103descr == null ? null : eaa0103.eaa0103descr.length() > 115 ? eaa0103.eaa0103descr.substring(0, 115) : eaa0103.eaa0103descr
                    if (eaa0103.eaa0103cfop != null) descr = descr + "-" + eaa0103.eaa0103cfop.aaj15codigo
                    txt.print(descr, 120)

                    String ncm = eaa0103.eaa0103ncm == null ? null : eaa0103.eaa0103ncm.abg01codigo
                    ncm = ncm == null ? null : ncm.indexOf("/") == -1 ? ncm : ncm.substring(0, ncm.indexOf("/"))
                    txt.print(ncm == null ? null : StringUtils.ajustString(ncm, 8))

                    txt.print(eaa0103.eaa0103umComl == null ? null : eaa0103.eaa0103umComl.aam06codigo)
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "qCom")), 4))
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "vUnCom")), 4))
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "vProd")), 2))
                    txt.print(eaa0103.eaa0103cstIcms == null ? null : eaa0103.eaa0103cstIcms.aaj10codigo, 3)
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "pICMS")), 2))
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "pIPI")), 2))
                    txt.print(formatarValor(eaa0103.eaa0103json.getBigDecimal(getCampo("30", "vIPI")), 2))

                    txt.newLine()
                    totalTipo30++
                }

                /** Tipo 40 */
                txt.print("40")
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vBC")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vICMS")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vBCST")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vST")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vProd")).add(totalRetorno), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vFrete")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vSeg")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vDesc")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vIPI")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vOutro")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vNF")).add(totalRetorno), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vServ")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "pISS")), 2))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("40", "vISS")), 2))

                txt.newLine()
                totalTipo40++

                /** Tipo 50 +*/
                txt.print("50")
                txt.print(eaa0102.eaa0102frete)

                def despacho = eaa0102.eaa0102despacho != null ? getSession().get(Abe01.class, eaa0102.eaa0102despacho.abe01id) : null
                def ni = despacho == null ? null : despacho.abe01ni == null ? null : StringUtils.extractNumbers(despacho.abe01ni)
                txt.print(ni == null ? null : StringUtils.ajustString(ni, despacho.abe01ti == 0 ? 14 : 11, '0', true))
                txt.print(despacho == null ? null : despacho.abe01nome == null ? " " : despacho.abe01nome.length() > 60 ? despacho.abe01nome.substring(0, 60) : despacho.abe01nome)
                txt.print(despacho == null ? null : despacho.abe01ie == null ? null : StringUtils.extractNumbers(despacho.abe01ie))

                def endereco = despacho != null ? buscarEnderecoPrincipalEntidade(despacho.abe01id) : null
                txt.print(endereco == null ? null : endereco.abe0101endereco.length() > 60 ? endereco.abe0101endereco.substring(0, 60) : endereco.abe0101endereco)
                txt.print(endereco == null ? null : endereco.abe0101municipio == null ? null : endereco.abe0101municipio.aag0201nome)
                txt.print(endereco == null ? null : endereco.abe0101municipio == null ? null : endereco.abe0101municipio.aag0201uf.aag02uf)

                def veiculo = eaa0102.eaa0102veiculo != null ? getSession().get(Aah20.class, eaa0102.eaa0102veiculo.aah20id) : null
                txt.print(veiculo == null ? null : veiculo.aah20placa)
                txt.print(veiculo == null ? null : veiculo.aah20ufPlaca)
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("50", "qVol")), 0))
                txt.print(eaa0102.eaa0102especie)
                txt.print(eaa0102.eaa0102marca)
                txt.print(eaa0102.eaa0102numero)
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("50", "pesoL")), 3))
                txt.print(formatarValor(eaa01.eaa01json.getBigDecimal(getCampo("50", "pesoB")), 3))

                txt.newLine()
                totalTipo50++
            }
        }

        /** Tipo 90 **/
        txt.print("90")
        txt.print(totalTipo20, 5)
        txt.print(totalTipo30, 5)
        txt.print(totalTipo40, 5)
        txt.print(totalTipo50, 5)
        txt.print(0, 5)

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

    Aaj15 buscarCfopItemDocumento(Long eaa01id) {
        def sql = " SELECT * FROM Aaj15 " +
                " INNER JOIN Eaa0103 ON eaa0103cfop = aaj15id " +
                " INNER JOIN Eaa01 ON eaa01id = :eaa01id "

        return getAcessoAoBanco().buscarRegistroUnico(sql, criarParametroSql("eaa01id", eaa01id))
    }

    Eaa0101 buscarEnderecoPrincipalDocumento(Long eaa01id) {
        return getSession().createCriteria(Eaa0101.class)
                .addJoin(Joins.fetch("eaa0101.eaa0101municipio").left(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
                .addJoin(Joins.fetch("eaa0101.eaa0101pais").left(true).alias("aag01"))
                .addWhere(Criterions.eq("eaa0101.eaa0101doc", eaa01id))
                .addWhere(Criterions.eq("eaa0101.eaa0101principal", Eaa0101.SIM))
                .get(ColumnType.ENTITY)
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

    Abe0101 buscarEnderecoPrincipalEntidade(Long abe01id) {
        return getSession().createCriteria(Abe0101.class)
                .addJoin(Joins.fetch("abe0101.abe0101municipio").left(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
                .addJoin(Joins.fetch("abe0101.abe0101pais").left(true).alias("aag01"))
                .addWhere(Criterions.eq("abe0101.abe0101ent", abe01id))
                .get(ColumnType.ENTITY)
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==