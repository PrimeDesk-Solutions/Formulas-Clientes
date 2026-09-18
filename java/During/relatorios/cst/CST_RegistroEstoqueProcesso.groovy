package During.relatorios.cst

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

import java.time.LocalDate

class CST_RegistroEstoqueProcesso extends RelatorioBase {

    @Override
    String getNomeTarefa() {
        return "CST - Registro de Estoque em Processo - LCR"
    }

    @Override
    Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("emissao", DateUtils.getStartAndEndMonth(LocalDate.now()))
        return Utils.map("filtros", filtrosDefault)
    }

    @Override
    DadosParaDownload executar() {
        List<Long> numero = getListLong("numero")
        List<Long> tipoDoc = getListLong("tipoDoc")
        LocalDate[] emissao = getIntervaloDatas("emissao")

        List<TableMap> dados = buscarDados(numero, tipoDoc, emissao)

        for (dado in dados) {
            dado.put("abb01num", buscarDocumentoPlanoDeAcao(dado.getString("codItemProd")))
        }

        return gerarPDF("SCE_RegistroEstoqueProcesso", dados)
    }

    private List<TableMap> buscarDados(List<Long> numero, List<Long> tipoDoc, LocalDate[] emissao) {

        String whereNumero = numero != null && numero.size() > 0 ? "and bab01id in (:numero)\n" : ""
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "and abb01tipo in (:tipoDoc)\n" : ""
        String whereEmissao = emissao != null ? "and abb01data >= :dataIni and abb01data <= :dataFim\n" : ""

        String sql = "select abe01na, itemProd.abm01codigo as codItemProd, itemProd.abm01descr as descrItemProd,\n" +
                     "case\n" +
                     "\twhen item.abm01tipo = 0 then 'Mat'\n" +
                     "\twhen item.abm01tipo = 1 then 'Prod'\n" +
                     "\twhen item.abm01tipo = 2 then 'Merc'\n" +
                     "\twhen item.abm01tipo = 3 then 'Serv'\n" +
                     "end as abm01tipo, item.abm01codigo as codItem, item.abm01descr as descrItem, aam06codigo,\n" +
                     "case\n" +
                     "\twhen abm12cstA in (0, 3, 4, 5, 8) then abm12cstA || ' - Nacional'\n" +
                     "\twhen abm12cstA in (1, 2, 6, 7) then abm12cstA || ' - Estrangeira'\n" +
                     "end as abm12cstA,\n" +
                     "bab0101qtA, cast(abm0101json ->> 'precolivre' as numeric) as precolivre, bab0101qtA * cast(abm0101json ->> 'precolivre' as numeric) as valor\n" +
                     "from bab01\n" +
                     "inner join abb01 on abb01id = bab01central\n" +
                     "inner join abp20 on abp20id = bab01comp\n" +
                     "left join abe01 on abe01id = bab01ent\n" +
                     "inner join abm01 as itemProd on itemProd.abm01id = abp20item\n" +
					 "inner join bab0103 on bab01id = bab0103op\n" +
					 "inner join baa0101 on baa0101id = bab0103itemPP\n" +
					 "inner join baa01011 on baa0101id = baa01011pp\n" +
					 "inner join eaa0103 on eaa0103id = baa01011itemDoc\n" +
                     "inner join bab0101 on bab01id = bab0101op\n" +
                     "inner join abm01 as item on item.abm01id = bab0101item\n" +
                     "inner join aam06 on aam06id = item.abm01umu\n" +
                     "inner join abm0101 on item.abm01id = abm0101item\n" +
                     "left join abm12 on abm12id = abm0101fiscal\n" +
                     obterWherePadrao("bab01", "where") + "\n" +
                     whereNumero +
                     whereTipoDoc +
                     whereEmissao +
                     " and eaa0103pedAtend == 1 || and eaa0103pedAtend == 0 " +
                     "order by abb01num, item.abm01codigo"

        Parametro p1 = numero != null && numero.size() > 0 ? Parametro.criar("numero", numero) : null
        Parametro p2 = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null
        Parametro p3 = emissao != null ? Parametro.criar("dataIni", emissao[0]) : null
        Parametro p4 = emissao != null ? Parametro.criar("dataFim", emissao[1]) : null

        return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4)
    }

    private Integer buscarDocumentoPlanoDeAcao(String codItemProd) {

        String sql = "select centralEaa.abb01num\n" +
                     "from bab01\n" +
                     "inner join abb01 as centralOp on centralOp.abb01id = bab01central\n" +
                     "inner join abb0102 on centralOp.abb01id = abb0102doc\n" +
                     "inner join abb01 as centralOrigem on centralOrigem.abb01id = abb0102central\n" +
                     "inner join baa01 on centralOrigem.abb01id = baa01central\n" +
                     "inner join baa0101 on baa01id = baa0101plano\n" +
                     "inner join abp20 on abp20id = baa0101comp\n" +
                     "inner join abm01 on abm01id = abp20item\n" +
                     "inner join baa01011 on baa0101id = baa01011pp\n" +
                     "inner join eaa0103 on eaa0103id = baa01011itemDoc\n" +
                     "inner join eaa01 on eaa01id = eaa0103doc\n" +
                     "inner join abb01 as centralEaa on centralEaa.abb01id = eaa01central\n" +
                     obterWherePadrao("bab01", "where") + "\n" +
                     "and abm01codigo = '" + codItemProd + "'"
                     " and eaa0103pedAtend == 1 || and eaa0103pedAtend == 0 " 

        return getAcessoAoBanco().obterInteger(sql)
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlZ2lzdHJvIGRlIEVzdG9xdWUgZW0gUHJvY2Vzc28gLSBMQ1IiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlZ2lzdHJvIGRlIEVzdG9xdWUgZW0gUHJvY2Vzc28gLSBMQ1IiLCJ0aXBvIjoicmVsYXRvcmlvIn0=