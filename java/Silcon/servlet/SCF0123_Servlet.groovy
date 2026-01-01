package Silcon.servlets

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import com.fasterxml.jackson.core.type.TypeReference;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.ab.Abe01
import sam.model.entities.da.Daa01
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.criteria.client.ClientCriteria;
import sam.core.criteria.ClientCriteriaConvert
import br.com.multitec.utils.criteria.client.ClientCriterion;
import br.com.multitec.utils.criteria.client.ClientCriterions;
import sam.dto.scf.SCF0123MostrarDto
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate


public class SCF0123_Servlet extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return null;
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {

        Integer valueRecebidos = Integer.parseInt(httpServletRequest.getParameter("valueRecebidos"));
        Integer valuePagos = Integer.parseInt(httpServletRequest.getParameter("valuePagos"));
        Integer valueChkRecebimento = Integer.parseInt(httpServletRequest.getParameter("valueChkRecebimento"));
        LocalDate dtRecebimentoPgtoIni = LocalDate.parse(httpServletRequest.getParameter("dtRecebimentoPgtoIni"));
        LocalDate dtRecebimentoPgtoFin = LocalDate.parse(httpServletRequest.getParameter("dtRecebimentoPgtoFin"));
        Integer valueReceber = Integer.parseInt(httpServletRequest.getParameter("valueReceber"));
        Integer valuePagar = Integer.parseInt(httpServletRequest.getParameter("valuePagar"));
        Integer valueChkTipoDoc = Integer.parseInt(httpServletRequest.getParameter("valueChkTipoDoc"));
        String codTipoDocIni = httpServletRequest.getParameter("codTipoDocIni")
        String codTipoDocFin = httpServletRequest.getParameter("codTipoDocFin")
        Integer valueChkNumero = Integer.parseInt(httpServletRequest.getParameter("valueChkNumero"));
        Integer numeroInicial = Integer.parseInt(httpServletRequest.getParameter("numeroInicial"));
        Integer numeroFinal = Integer.parseInt(httpServletRequest.getParameter("numeroFinal"));
        Integer valueChkEmissao = Integer.parseInt(httpServletRequest.getParameter("valueChkEmissao"));
        LocalDate dataEmissaoIni = LocalDate.parse(httpServletRequest.getParameter("dataEmissaoIni"));
        LocalDate dataEmissaoFin = LocalDate.parse(httpServletRequest.getParameter("dataEmissaoFin"));
        Integer valueChkEntidade = Integer.parseInt(httpServletRequest.getParameter("valueChkEntidade"));
        String codEntidadeIni = httpServletRequest.getParameter("codEntidadeIni")
        String codEntidadeFin = httpServletRequest.getParameter("codEntidadeFin")
        Integer valueChkVencimento = Integer.parseInt(httpServletRequest.getParameter("valueChkVencimento"));
        Integer valueRdoVencimentoReal = Integer.parseInt(httpServletRequest.getParameter("valueRdoVencimentoReal"));
        LocalDate dataInicialVcto = LocalDate.parse(httpServletRequest.getParameter("dataInicialVcto"));
        LocalDate dataFinalVcto = LocalDate.parse(httpServletRequest.getParameter("dataFinalVcto"));
        Integer valueRdoReal = Integer.parseInt(httpServletRequest.getParameter("valueRdoReal"));
        Integer valueRdoPrevisao = Integer.parseInt(httpServletRequest.getParameter("valueRdoPrevisao"));
        String idsAbe01 = httpServletRequest.getParameter("idsAbe01");
        String idsAah01 = httpServletRequest.getParameter("idsAah01");
        ObjectMapper mapper = new ObjectMapper();


        Criterion critAah01;
        if (valueChkTipoDoc == 1) {
            if(idsAah01 != null && idsAah01.replace('"', "") != codTipoDocIni){
                critAah01 = Criterions.in("aah01id", mapper.readValue(idsAah01, new TypeReference<List<Long>>() {}));
            }else{
                if (codTipoDocIni != null && codTipoDocFin != null) {
                    critAah01 = Criterions.between("aah01codigo", codTipoDocIni, codTipoDocFin);
                } else if (codTipoDocIni != null) {
                    critAah01 = Criterions.ge("aah01codigo", codTipoDocIni);
                } else if (codTipoDocFin != null) {
                    critAah01 = Criterions.le("aah01codigo", codTipoDocFin);
                } else {
                    critAah01 = Criterions.isTrue();
                }
            }

        } else {
            critAah01 = Criterions.isTrue();
        }

        Criterion critAbb01num;
        if (valueChkNumero == 1) {
            if (numeroInicial != null && numeroFinal != null) {
                critAbb01num = Criterions.between("abb01num", numeroInicial, numeroFinal);
            } else if (numeroInicial != null) {
                critAbb01num = Criterions.ge("abb01num", numeroInicial);
            } else if (numeroFinal != null) {
                critAbb01num = Criterions.le("abb01num", numeroFinal);
            } else {
                critAbb01num = Criterions.isTrue();
            }
        } else {
            critAbb01num = Criterions.isTrue();
        }


        Criterion critEmissao
        if(valueChkEmissao == 1){
            if(dataEmissaoIni != null && dataEmissaoFin != null){
                critEmissao = Criterions.between("abb01data", dataEmissaoIni, dataEmissaoFin);
            }else if(dataEmissaoIni != null){
                critEmissao = Criterions.ge("abb01data", dataEmissaoIni);
            }else if(dataEmissaoFin != null){
                critEmissao = Criterions.le("abb01data", dataEmissaoFin);
            }else{
                critEmissao = Criterions.isTrue();
            }
        }else{
            critEmissao = Criterions.isTrue();
        }

        Criterion critAbe01;
        if(valueChkEntidade == 1){
            if(idsAbe01 != null && idsAbe01.replace('"', "") != codEntidadeIni ){
                critAbe01 = Criterions.in("abe01.abe01id", mapper.readValue(idsAbe01, new TypeReference<List<Long>>() {}))
            }else{
                if(codEntidadeIni != null && codEntidadeFin != null){
                    critAbe01 = Criterions.between("abe01.abe01codigo", codEntidadeIni, codEntidadeFin);
                }else if(codEntidadeIni != null){
                    critAbe01 = Criterions.ge("abe01.abe01codigo", codEntidadeIni);
                }else if(codEntidadeFin != null){
                    critAbe01 = Criterions.le("abe01.abe01codigo", codEntidadeFin);
                }else{
                    critAbe01 = Criterions.isTrue();
                }
            }
        }else{
            critAbe01 = Criterions.isTrue();
        }

        Criterion critVencimento;
        if(valueChkVencimento == 1){
            String dataVcto = valueRdoVencimentoReal == 1 ? "daa01dtVctoR" : "daa01dtVctoN";
            if(dataInicialVcto != null && dataFinalVcto != null){
                critVencimento = Criterions.between(dataVcto, dataInicialVcto, dataFinalVcto);
            }else if(dataInicialVcto != null){
                critVencimento = Criterions.ge(dataVcto, dataInicialVcto);
            }else if(dataFinalVcto != null){
                critVencimento = Criterions.le(dataVcto, dataFinalVcto);
            }else{
                critVencimento = Criterions.isTrue();
            }
        }else{
            critVencimento = Criterions.isTrue();
        }

        Criterion critPrevisao;
        if(valueRdoReal == 1){
            critPrevisao = Criterions.eq("daa01previsao", 0);
        }else if(valueRdoPrevisao == 1){
            critPrevisao = Criterions.eq("daa01previsao", 1);
        }else{
            critPrevisao = Criterions.isTrue();
        }

        Criterion critStatus;
        Criterion critRP;
        if(valueRecebidos == 1){
            critStatus = Criterions.isNotNull("daa01dtBaixa");
            critRP = Criterions.eq("daa01rp", 0);
        }else if(valuePagos == 1){
            critStatus = Criterions.isNotNull("daa01dtBaixa");
            critRP = Criterions.eq("daa01rp", 1);
        }else if(valueReceber == 1){
            critStatus = Criterions.isNull("daa01dtBaixa");
            critRP = Criterions.eq("daa01rp", 0);
        }else{
            critStatus = Criterions.isNull("daa01dtBaixa");
            critRP = Criterions.eq("daa01rp", 1);
        }

        Criterion critDataRecebimentoPgto;
        if(valueChkRecebimento == 1){
            if(dtRecebimentoPgtoIni != null && dtRecebimentoPgtoFin != null){
                critDataRecebimentoPgto = Criterions.between("daa01dtBaixa", dtRecebimentoPgtoIni, dtRecebimentoPgtoFin)
            }else if(dtRecebimentoPgtoIni != null){
                critDataRecebimentoPgto = Criterions.ge("daa01dtBaixa", dtRecebimentoPgtoIni);
            }else if(dtRecebimentoPgtoFin != null){
                critDataRecebimentoPgto = Criterions.le("daa01dtBaixa", dtRecebimentoPgtoFin);
            }else{
                critDataRecebimentoPgto = Criterions.isTrue();
            }
        }else{
            critDataRecebimentoPgto = Criterions.isTrue();
        }

        List<Daa01> daa01s = session.createCriteria(Daa01.class)
                .addJoin(Joins.fetch("daa01central").left(false).alias("abb01"))
                .addJoin(Joins.fetch("abb01.abb01tipo").left(false).alias("aah01"))
                .addJoin(Joins.fetch("abb01.abb01ent").left(false).alias("abe01"))
                .addJoin(Joins.fetch("daa01port").left(true).alias("abf15"))
                .addJoin(Joins.fetch("daa01oper").left(true).alias("abf16"))
                .addJoin(Joins.fetch("daa01rep0").left(true).alias("abe01rep0"))
                .addJoin(Joins.fetch("daa01rep1").left(true).alias("abe01rep1"))
                .addJoin(Joins.fetch("daa01rep2").left(true).alias("abe01rep2"))
                .addJoin(Joins.fetch("daa01rep3").left(true).alias("abe01rep3"))
                .addJoin(Joins.fetch("daa01rep4").left(true).alias("abe01rep4"))
                .addWhere(critAah01).addWhere(critPrevisao).addWhere(critAbe01)
                .addWhere(critEmissao).addWhere(critVencimento).addWhere(critAbb01num)
                .addWhere(critRP).addWhere(critStatus).addWhere(critDataRecebimentoPgto)
                .addWhere(samWhere.getCritPadrao(Daa01.class))
                .getList(ColumnType.ENTITY);

        return ResponseEntity.ok(daa01s)
    }

}
//meta-sis-eyJkZXNjciI6IlNDRjAxMDIzIC0gU2VydmxldCIsInRpcG8iOiJzZXJ2bGV0In0=