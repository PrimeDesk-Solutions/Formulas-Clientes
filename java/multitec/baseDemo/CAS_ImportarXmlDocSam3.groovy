package multitec.baseDemo;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.validator.MultiValidationException
import br.com.multitec.utils.xml.ElementXml
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aae10
import sam.model.entities.aa.Aae11
import sam.model.entities.aa.Aae11
import sam.model.entities.aa.Aah01
import sam.model.entities.aa.Aaj03
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj14
import sam.model.entities.aa.Aaj15
import sam.model.entities.aa.Aaj30
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abg02
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.srf.service.SRFService

import java.time.LocalDate
import java.time.LocalTime

class CAS_ImportarXmlDocSam3 extends FormulaBase{
    private TableMap camposVlrFixosItem
    private TableMap camposVlrLivresItem
    private TableMap camposVlrFixosDoc
    private TableMap camposVlrLivresDoc
    private TableMap mapeamentoPCD;
    private TableMap mapeamentoEnt;
    private TableMap mapeamentoTipoDoc;
    private TableMap mapeamentoTabelaPreco;
    private TableMap mapeamentoCondicaoPagamento;
    private TableMap mapeamentoItem;
    private List<String> mensagens = new ArrayList<>()

    @Override
    FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    //METODOS PARA DE-PARA PARA OS CAMPOS DE VALORES DA NOTA
    //INFORMAR TODOS OS CAMPOS NECESSARIOS
    private void comporCamposVlrFixosItem(){
        camposVlrFixosItem = new TableMap()
        //camposVlrFixosItem.put("nomeCampoItemSAM4","nomeCampoItemSAM3")
        camposVlrFixosItem.put("eaa0103qtComl","ea011vlr0");
        camposVlrFixosItem.put("eaa0103qtUso","ea011vlr2");
        camposVlrFixosItem.put("eaa0103unit","ea011vlr6");
        camposVlrFixosItem.put("eaa0103total","ea011vlr9");
        camposVlrFixosItem.put("eaa0103totDoc","ea011vlr10");
        camposVlrFixosItem.put("eaa0103totFinanc","ea011vlr82");
        camposVlrFixosItem.put("eaa0103codEnqIpi ","ea014codenqlegal"); //nagyla

    }

    private void comporCamposVlrLivresItem(){
        camposVlrLivresItem = new TableMap()
        //camposVlrLivresItem.put("nomeCampoItemSAM4","nomeCampoItemSAM3")
        camposVlrLivresItem.put("vlr_desc_tx","ea011vlr7")
        camposVlrLivresItem.put("vlr_desc","ea011vlr8")
        camposVlrLivresItem.put("vlr_frete_dest","ea011vlr13")
        camposVlrLivresItem.put("vlr_seguro","ea011vlr14")
        camposVlrLivresItem.put("vlr_outras","ea011vlr15")
        camposVlrLivresItem.put("ipi_bc","ea011vlr16")
        camposVlrLivresItem.put("ipi_aliq","ea011vlr17")
        //camposVlrLivresItem.put("dificms","ea011vlr18")
        camposVlrLivresItem.put("ipi_unit","ea011vlr19")
        camposVlrLivresItem.put("ipi_ipi","ea011vlr20")
        camposVlrLivresItem.put("moeda_unit","ea011vlr21")
        camposVlrLivresItem.put("ipi_outras","ea011vlr22")
        camposVlrLivresItem.put("ipi_isento","ea011vlr23")
        camposVlrLivresItem.put("icm_bc","ea011vlr24")
        camposVlrLivresItem.put("icm_reduc_bc","ea011vlr25")
        camposVlrLivresItem.put("icm_aliq","ea011vlr26")
        camposVlrLivresItem.put("icm_icm","ea011vlr27")
        camposVlrLivresItem.put("icm_outras","ea011vlr28")
        camposVlrLivresItem.put("icm_isento","ea011vlr29")
        camposVlrLivresItem.put("st_bc","ea011vlr30")
        camposVlrLivresItem.put("st_aliq","ea011vlr31")
        camposVlrLivresItem.put("st_icm","ea011vlr32")
        //camposVlrLivresItem.put("icm_dif","ea011vlr35")
        camposVlrLivresItem.put("fcpufdest_aliq","ea011vlr37")
        camposVlrLivresItem.put("pis_bc","ea011vlr38")
        camposVlrLivresItem.put("pis_aliq","ea011vlr39")
        //camposVlrLivresItem.put("icm_dif","ea011vlr41")
        camposVlrLivresItem.put("pis_pis","ea011vlr42")
        camposVlrLivresItem.put("cofins_bc","ea011vlr44")
        camposVlrLivresItem.put("cofins_aliq","ea011vlr45")
        camposVlrLivresItem.put("cofins_cofins","ea011vlr48")
        camposVlrLivresItem.put("cofins_st","ea011vlr49")
        camposVlrLivresItem.put("iss_bc","ea011vlr52")
        camposVlrLivresItem.put("iss_aliq","ea011vlr53")
        camposVlrLivresItem.put("iss_iss","ea011vlr54")
        camposVlrLivresItem.put("ir_bc","ea011vlr55")
        camposVlrLivresItem.put("ir_ir","ea011vlr56")
        camposVlrLivresItem.put("inss_bc","ea011vlr57")
        camposVlrLivresItem.put("inss_vlr","ea011vlr58")
        camposVlrLivresItem.put("csll_bc","ea011vlr59")
        camposVlrLivresItem.put("csll_aliq","ea011vlr60")
        camposVlrLivresItem.put("csll_csll","ea011vlr61")
        camposVlrLivresItem.put("pis_cof_csll_bc","ea011vlr62")
        camposVlrLivresItem.put("pis_cof_csll_aliq","ea011vlr63")
        camposVlrLivresItem.put("pis_cofins_csll","ea011vlr64")
        camposVlrLivresItem.put("imp_bc","ea011vlr66")
        camposVlrLivresItem.put("ii_aliq","ea011vlr67")
        camposVlrLivresItem.put("ii_ii","ea011vlr68")
        camposVlrLivresItem.put("comp_import","ea011vlr69")
        camposVlrLivresItem.put("vlr_vlme","ea011vlr70")
        camposVlrLivresItem.put("vlr_pl","ea011vlr71")
        camposVlrLivresItem.put("vlr_pb","ea011vlr72")
        camposVlrLivresItem.put("vlr_frete_emit","ea011vlr73")
        camposVlrLivresItem.put("partilha_aliq","ea011vlr74")
        camposVlrLivresItem.put("icmsufdest_bc","ea011vlr75")
        camposVlrLivresItem.put("cif_imp","ea011vlr78")
        camposVlrLivresItem.put("icms_fcp","ea011vlr80")
        camposVlrLivresItem.put("qtde_trib","ea011vlr81")
        camposVlrLivresItem.put("siscomex","ea011vlr83")
        camposVlrLivresItem.put("ipi_obs","ea011vlr84")
        camposVlrLivresItem.put("internaufdest_aliq","ea011vlr85")
        //camposVlrLivresItem.put("dificms","ea011vlr86")
        camposVlrLivresItem.put("ir_aliq","ea011vlr88")
        camposVlrLivresItem.put("interesuf_aliq","ea011vlr91")
        camposVlrLivresItem.put("inss_aliq","ea011vlr93")
        camposVlrLivresItem.put("vlrfcisaida","ea011vlr94")
        camposVlrLivresItem.put("ufdest_icms","ea011vlr96")
        camposVlrLivresItem.put("uforig_icms","ea011vlr97")
        //camposVlrLivresItem.put("pis_bc","ea011vlr100")
        //camposVlrLivresItem.put("pis_aliq","ea011vlr101")
        //camposVlrLivresItem.put("pis_pis","ea011vlr102")
        //camposVlrLivresItem.put("icm_icm","ea011vlr103")
        //camposVlrLivresItem.put("icm_bc","ea011vlr104")
        //camposVlrLivresItem.put("icm_aliq","ea011vlr105")
        //camposVlrLivresItem.put("icm_outras","ea011vlr106")
        //camposVlrLivresItem.put("icm_isento","ea011vlr107")
        //camposVlrLivresItem.put("st_icm","ea011vlr108")
        //camposVlrLivresItem.put("st_bc","ea011vlr109")
        //camposVlrLivresItem.put("st_aliq","ea011vlr110")
        //camposVlrLivresItem.put("ipi_ipi","ea011vlr113")
        //camposVlrLivresItem.put("ipi_bc","ea011vlr114")
        //camposVlrLivresItem.put("ipi_aliq","ea011vlr115")
        //camposVlrLivresItem.put("ipi_outras","ea011vlr116")
        //camposVlrLivresItem.put("ipi_isento","ea011vlr117")
        //camposVlrLivresItem.put("icm_reduc_bc","ea011vlr118")
        //camposVlrLivresItem.put("ipi_obs","ea011vlr119")
        camposVlrLivresItem.put("credoutros","ea011vlr120")
        //camposVlrLivresItem.put("cofins_bc","ea011vlr121")
        //camposVlrLivresItem.put("cofins_aliq","ea011vlr122")
        //camposVlrLivresItem.put("cofins_cofins","ea011vlr123")
        camposVlrLivresItem.put("desc_taxa_rep","ea011vlr125")
        camposVlrLivresItem.put("desc_valor_rep","ea011vlr126")
        camposVlrLivresItem.put("desc_taxa_vendas","ea011vlr127")
        camposVlrLivresItem.put("desc_valor_venda","ea011vlr128")
    }

    private void comporCamposVlrFixosDoc(){
        camposVlrFixosDoc = new TableMap()
        //camposVlrFixosDoc.put("nomeCampoDocSAM4","nomeCampoDocSAM3")
        camposVlrFixosDoc.put("eaa01totItens","ea01vlr9")
        camposVlrFixosDoc.put("eaa01totDoc","ea01vlr10")
        camposVlrFixosDoc.put("eaa01totFinanc","ea01vlr82")
    }

    private void comporCamposVlrLivresDoc(){
        camposVlrLivresDoc = new TableMap()
        //camposVlrLivresDoc.put("nomeCampoDocSAM4","nomeCampoDocSAM3")
        camposVlrLivresDoc.put("vlr_desc_tx","ea01vlr7")
        camposVlrLivresDoc.put("vlr_desc","ea01vlr8")
        camposVlrLivresDoc.put("vlr_frete_dest","ea01vlr13")
        camposVlrLivresDoc.put("vlr_seguro","ea01vlr14")
        camposVlrLivresDoc.put("vlr_outras","ea01vlr15")
        camposVlrLivresDoc.put("ipi_bc","ea01vlr16")
        camposVlrLivresDoc.put("ipi_aliq","ea01vlr17")
//        camposVlrLivresDoc.put("dificms","ea01vlr18")
        camposVlrLivresDoc.put("ipi_unit","ea01vlr19")
        camposVlrLivresDoc.put("ipi_ipi","ea01vlr20")
        camposVlrLivresDoc.put("moeda_unit","ea01vlr21")
        camposVlrLivresDoc.put("ipi_outras","ea01vlr22")
        camposVlrLivresDoc.put("ipi_isento","ea01vlr23")
        camposVlrLivresDoc.put("icm_bc","ea01vlr24")
        camposVlrLivresDoc.put("icm_reduc_bc","ea01vlr25")
        camposVlrLivresDoc.put("icm_aliq","ea01vlr26")
        camposVlrLivresDoc.put("icm_icm","ea01vlr27")
        camposVlrLivresDoc.put("icm_outras","ea01vlr28")
        camposVlrLivresDoc.put("icm_isento","ea01vlr29")
        camposVlrLivresDoc.put("st_bc","ea01vlr30")
        camposVlrLivresDoc.put("st_aliq","ea01vlr31")
        camposVlrLivresDoc.put("st_icm","ea01vlr32")
//        camposVlrLivresDoc.put("icm_dif","ea01vlr35")
        camposVlrLivresDoc.put("fcpufdest_aliq","ea01vlr37")
        camposVlrLivresDoc.put("pis_bc","ea01vlr38")
        camposVlrLivresDoc.put("pis_aliq","ea01vlr39")
//        camposVlrLivresDoc.put("icm_dif","ea01vlr41")
        camposVlrLivresDoc.put("pis_pis","ea01vlr42")
        camposVlrLivresDoc.put("cofins_bc","ea01vlr44")
        camposVlrLivresDoc.put("cofins_aliq","ea01vlr45")
        camposVlrLivresDoc.put("cofins_cofins","ea01vlr48")
        camposVlrLivresDoc.put("cofins_st","ea01vlr49")
        camposVlrLivresDoc.put("iss_bc","ea01vlr52")
        camposVlrLivresDoc.put("iss_aliq","ea01vlr53")
        camposVlrLivresDoc.put("iss_iss","ea01vlr54")
        camposVlrLivresDoc.put("ir_bc","ea01vlr55")
        camposVlrLivresDoc.put("ir_ir","ea01vlr56")
        camposVlrLivresDoc.put("inss_bc","ea01vlr57")
        camposVlrLivresDoc.put("inss_vlr","ea01vlr58")
        camposVlrLivresDoc.put("csll_bc","ea01vlr59")
        camposVlrLivresDoc.put("csll_aliq","ea01vlr60")
        camposVlrLivresDoc.put("csll_csll","ea01vlr61")
        camposVlrLivresDoc.put("pis_cof_csll_bc","ea01vlr62")
        camposVlrLivresDoc.put("pis_cof_csll_aliq","ea01vlr63")
        camposVlrLivresDoc.put("pis_cofins_csll","ea01vlr64")
        camposVlrLivresDoc.put("imp_bc","ea01vlr66")
        camposVlrLivresDoc.put("ii_aliq","ea01vlr67")
        camposVlrLivresDoc.put("ii_ii","ea01vlr68")
        camposVlrLivresDoc.put("comp_import","ea01vlr69")
        camposVlrLivresDoc.put("vlr_vlme","ea01vlr70")
        camposVlrLivresDoc.put("vlr_pl","ea01vlr71")
        camposVlrLivresDoc.put("vlr_pb","ea01vlr72")
        camposVlrLivresDoc.put("vlr_frete_emit","ea01vlr73")
        camposVlrLivresDoc.put("partilha_aliq","ea01vlr74")
        camposVlrLivresDoc.put("icmsufdest_bc","ea01vlr75")
        camposVlrLivresDoc.put("cif_imp","ea01vlr78")
        camposVlrLivresDoc.put("icms_fcp","ea01vlr80")
        camposVlrLivresDoc.put("qtde_trib","ea01vlr81")
        camposVlrLivresDoc.put("siscomex","ea01vlr83")
        camposVlrLivresDoc.put("ipi_obs","ea01vlr84")
        camposVlrLivresDoc.put("internaufdest_aliq","ea01vlr85")
//        camposVlrLivresDoc.put("dificms","ea01vlr86")
        camposVlrLivresDoc.put("ir_aliq","ea01vlr88")
        camposVlrLivresDoc.put("interesuf_aliq","ea01vlr91")
        camposVlrLivresDoc.put("inss_aliq","ea01vlr93")
        camposVlrLivresDoc.put("vlrfcisaida","ea01vlr94")
        camposVlrLivresDoc.put("ufdest_icms","ea01vlr96")
        camposVlrLivresDoc.put("uforig_icms","ea01vlr97")
//        camposVlrLivresDoc.put("pis_bc","ea01vlr100")
//        camposVlrLivresDoc.put("pis_aliq","ea01vlr101")
//        camposVlrLivresDoc.put("pis_pis","ea01vlr102")
//        camposVlrLivresDoc.put("icm_icm","ea01vlr103")
//        camposVlrLivresDoc.put("icm_bc","ea01vlr104")
//        camposVlrLivresDoc.put("icm_aliq","ea01vlr105")
//        camposVlrLivresDoc.put("icm_outras","ea01vlr106")
//        camposVlrLivresDoc.put("icm_isento","ea01vlr107")
//        camposVlrLivresDoc.put("st_icm","ea01vlr108")
//        camposVlrLivresDoc.put("st_bc","ea01vlr109")
//        camposVlrLivresDoc.put("st_aliq","ea01vlr110")
//        camposVlrLivresDoc.put("ipi_ipi","ea01vlr113")
//        camposVlrLivresDoc.put("ipi_bc","ea01vlr114")
//        camposVlrLivresDoc.put("ipi_aliq","ea01vlr115")
//        camposVlrLivresDoc.put("ipi_outras","ea01vlr116")
//        camposVlrLivresDoc.put("ipi_isento","ea01vlr117")
//        camposVlrLivresDoc.put("icm_reduc_bc","ea01vlr118")
//        camposVlrLivresDoc.put("ipi_obs","ea01vlr119")
        camposVlrLivresDoc.put("credoutros","ea01vlr120")
//        camposVlrLivresDoc.put("cofins_bc","ea01vlr121")
//        camposVlrLivresDoc.put("cofins_aliq","ea01vlr122")
//        camposVlrLivresDoc.put("cofins_cofins","ea01vlr123")
        camposVlrLivresDoc.put("desc_taxa_rep","ea01vlr125")
        camposVlrLivresDoc.put("desc_valor_rep","ea01vlr126")
        camposVlrLivresDoc.put("desc_taxa_vendas","ea01vlr127")
        camposVlrLivresDoc.put("desc_valor_venda","ea01vlr128")
    }

    //METODOS PARA DE-PARA PARA OS CODIGOS DO PCD, ENTIDADE, TIPO DOC, TABELA DE PREÇO, CONDIÇÃO DE PAGAMENTO E ITEM
    //INFORMAR APENAS OS CODIGOS QUE MUDARAM, CODIGOS IGUAIS NÃO PRECISAM SER INFORMADOS
    private void comporMapPCD(){
        mapeamentoPCD = new TableMap()
        //mapeamentoPCD.put("codigoSAM3","codigoSAM4")
        mapeamentoPCD.put("70004","310_0")
        mapeamentoPCD.put("70010","311_0")
        mapeamentoPCD.put("70009","319_0")
        mapeamentoPCD.put("70000","400_0")
        mapeamentoPCD.put("70001","400_0")
        mapeamentoPCD.put("70007","400_0")
        mapeamentoPCD.put("70057","400_0")
        mapeamentoPCD.put("70033","401_0")
        mapeamentoPCD.put("70047","403_0")
        mapeamentoPCD.put("70029","409_0")
        mapeamentoPCD.put("70058","409_0")
        mapeamentoPCD.put("70002","417_0")
        mapeamentoPCD.put("70035","417_0")
        mapeamentoPCD.put("70019","418_0")
        mapeamentoPCD.put("70045","419_0")
        mapeamentoPCD.put("70064","420_0")
        mapeamentoPCD.put("80054","420_0")
        mapeamentoPCD.put("80044","421_0")
        mapeamentoPCD.put("70178","422_0")
        mapeamentoPCD.put("70102","423_0")
        mapeamentoPCD.put("70082","424_0")
        mapeamentoPCD.put("50022","502_0")
        mapeamentoPCD.put("70022","502_0")
        mapeamentoPCD.put("70012","505_0")
        mapeamentoPCD.put("70020","509_0")
        mapeamentoPCD.put("70220","509_0")
        mapeamentoPCD.put("70014","519_0")
        mapeamentoPCD.put("50032","520_0")
        mapeamentoPCD.put("70028","523_0")
        mapeamentoPCD.put("70073","523_0")
        mapeamentoPCD.put("70050","525_0")
        mapeamentoPCD.put("70065","525_0")
        mapeamentoPCD.put("70017","529_0")
        mapeamentoPCD.put("70006","531_0")
        mapeamentoPCD.put("70063","531_0")
        mapeamentoPCD.put("70043","532_0")
        mapeamentoPCD.put("50024","537_0")
        mapeamentoPCD.put("70003","538_0")
        mapeamentoPCD.put("70005","538_0")
        mapeamentoPCD.put("70018","539_0")
        mapeamentoPCD.put("70025","540_0")
        mapeamentoPCD.put("70030","541_0")
        mapeamentoPCD.put("70049","542_0")
        mapeamentoPCD.put("70071","543_0")
        mapeamentoPCD.put("70069","544_0")
        mapeamentoPCD.put("10000","300")
        mapeamentoPCD.put("10036","300")
        mapeamentoPCD.put("10008","301")
        mapeamentoPCD.put("10010","301")
        mapeamentoPCD.put("10026","301")
        mapeamentoPCD.put("10028","301")
        mapeamentoPCD.put("10048","301")
        mapeamentoPCD.put("10059","301")
        mapeamentoPCD.put("10061","301")
        mapeamentoPCD.put("10118","301")
        mapeamentoPCD.put("11040","301")
        mapeamentoPCD.put("10007","303")
        mapeamentoPCD.put("10009","303")
        mapeamentoPCD.put("10032","303")
        mapeamentoPCD.put("10045","303")
        mapeamentoPCD.put("10094","303")
        mapeamentoPCD.put("10106","303")
        mapeamentoPCD.put("11003","304")
        mapeamentoPCD.put("11038","304")
        mapeamentoPCD.put("10004","305")
        mapeamentoPCD.put("10044","305")
        mapeamentoPCD.put("10005","306")
        mapeamentoPCD.put("00020","307")
        mapeamentoPCD.put("00025","307")
        mapeamentoPCD.put("00028","307")
        mapeamentoPCD.put("00031","307")
        mapeamentoPCD.put("00038","307")
        mapeamentoPCD.put("10018","307")
        mapeamentoPCD.put("11032","307")
        mapeamentoPCD.put("00037","308")
        mapeamentoPCD.put("10011","309")
        mapeamentoPCD.put("10022","314")
        mapeamentoPCD.put("00010","320")
        mapeamentoPCD.put("00030","322")
        mapeamentoPCD.put("00032","323")
        mapeamentoPCD.put("00033","323")
        mapeamentoPCD.put("10012","324")
        mapeamentoPCD.put("11044","324")
        mapeamentoPCD.put("10083","325")
        mapeamentoPCD.put("10104","326")
        mapeamentoPCD.put("10124","327")
        mapeamentoPCD.put("11001","328")
        mapeamentoPCD.put("11004","329")
        mapeamentoPCD.put("11006","330")
        mapeamentoPCD.put("11007","330")
        mapeamentoPCD.put("11014","330")
        mapeamentoPCD.put("11015","330")
        mapeamentoPCD.put("1352A","331")
        mapeamentoPCD.put("10039","404")
        mapeamentoPCD.put("10073","404")
        mapeamentoPCD.put("10074","404")
        mapeamentoPCD.put("10107","404")
        mapeamentoPCD.put("10003","407")
        mapeamentoPCD.put("10096","407")
        mapeamentoPCD.put("10042","501")
        mapeamentoPCD.put("10057","501")
        mapeamentoPCD.put("10015","506")
        mapeamentoPCD.put("10055","511")
        mapeamentoPCD.put("10017","524")
        mapeamentoPCD.put("00013","545")
        mapeamentoPCD.put("00034","546")
        mapeamentoPCD.put("00035","547")
        mapeamentoPCD.put("10021","548")
        mapeamentoPCD.put("10027","549")
        mapeamentoPCD.put("10031","550")
        mapeamentoPCD.put("10037","551")
        mapeamentoPCD.put("10052","551")
        mapeamentoPCD.put("10053","551")
        mapeamentoPCD.put("10054","551")
        mapeamentoPCD.put("10041","552")
        mapeamentoPCD.put("10043","553")
        mapeamentoPCD.put("10049","554")
        mapeamentoPCD.put("10113","555")
        mapeamentoPCD.put("10119","556")
        mapeamentoPCD.put("10122","557")
        mapeamentoPCD.put("11021","558")
        mapeamentoPCD.put("10116","559")
        mapeamentoPCD.put("10019","512")
    }
    private void comporMapEnt(){
        mapeamentoEnt = new TableMap()
        //mapeamentoEnt.put("codigoSAM3","codigoSAM4")
    }
    private void comporMapTipoDoc(){
        mapeamentoTipoDoc = new TableMap()
        //mapeamentoTipoDoc.put("codigoSAM3","codigoSAM4")
        mapeamentoTipoDoc.put("26","M26") //Faturamento
        mapeamentoTipoDoc.put("20","M20") //Faturamento
        mapeamentoTipoDoc.put("21","M21") //Faturamento
        mapeamentoTipoDoc.put("01","M000001")
        mapeamentoTipoDoc.put("04","M000010")
        mapeamentoTipoDoc.put("08","M000011")
        mapeamentoTipoDoc.put("12","M000008")
        mapeamentoTipoDoc.put("13","M000012")
        mapeamentoTipoDoc.put("14","M000008")
        mapeamentoTipoDoc.put("141","M000008")
        mapeamentoTipoDoc.put("15","M000003")
        mapeamentoTipoDoc.put("19","M000013")
        mapeamentoTipoDoc.put("61","M000014")
        mapeamentoTipoDoc.put("63","M000015")
        mapeamentoTipoDoc.put("64","M000016")
        mapeamentoTipoDoc.put("641","M000017")

    }
    private void comporMapTabelaPreco(){
        mapeamentoTabelaPreco = new TableMap()
        //mapeamentoTabelaPreco.put("codigoSAM3","codigoSAM4")
    }
    private void comporMapCondicaoPagamento(){
        mapeamentoCondicaoPagamento = new TableMap()
        //mapeamentoCondicaoPagamento.put("codigoSAM3","codigoSAM4")
    }
    private void comporMapItem(){
        mapeamentoItem = new TableMap()
        //mapeamentoItem.put("codigoSAM3","codigoSAM4")
    }

    @Override
    void executar() {
        comporCamposVlrFixosItem()
        comporCamposVlrLivresItem()
        comporCamposVlrFixosDoc()
        comporCamposVlrLivresDoc()
        comporMapPCD()
        comporMapEnt()
        comporMapTipoDoc()
        comporMapTabelaPreco()
        comporMapCondicaoPagamento()
        comporMapItem()

        MultipartFile arquivo = get("arquivo");

        List<Eaa01> eaa01s = new ArrayList<>();

        SRFService srfService = instanciarService(SRFService.class);

        InputStream is = arquivo.getInputStream();
        ElementXml elementXml = new ElementXml(is);

        List<ElementXml> elementsEa01s = elementXml.getChildNodes("Ea01")

        for(ElementXml elementEa01 in elementsEa01s){
            LocalDate abb01data = DateUtils.parseDate(elementEa01.getChildValue("ea01dtemis"), "dd/MM/yyyy");
            LocalDate ea01dtes = elementEa01.getChildValue("ea01dtes") != null ? DateUtils.parseDate(elementEa01.getChildValue("ea01dtes"), "dd/MM/yyyy") : abb01data;
            Long abb01num = Long.parseLong(elementEa01.getChildValue("ea01num"))
            String abb01serie = elementEa01.getChildValue("ea01serie") != null ? elementEa01.getChildValue("ea01serie") : null

            Abd01 abd01 = null;
            Abe01 abe01 = null;
            Aah01 aah01 = null;
            Abe40 abe40 = null;
            Abe30 abe30 = null;
            String abe01codigo = "";
            String aah01codigo = "";
            String abd01codigo = "";

            ElementXml elementPcd = elementEa01.getChildNode("ea01pcd")
            if(elementPcd != null){
                abd01codigo = elementPcd.getChildValue("ab18codigo")
                abd01 = buscarPCDCodigo(abd01codigo)
            }

            ElementXml elementEnt = elementEa01.getChildNode("ea01ent")
            if(elementEnt != null){
                abe01codigo = elementEnt.getChildValue("ab80codigo")
                abe01 = buscarEntidadeCodigo(abe01codigo)
            }

            ElementXml elementTipo = elementEa01.getChildNode("ea01tipo")
            if(elementTipo != null){
                aah01codigo = elementTipo.getChildValue("ab15codigo")
                aah01 = buscarTipoDocumentoCodigo(aah01codigo)
            }
//
//            ElementXml elementTp = elementEa01.getChildNode("ea01tabpreco")
//            if(elementTp != null){
//                String abe40codigo= elementTp.getChildValue("ab60codigo")
//                abe40 = buscarTabelaPrecoCodigo(abe40codigo)
//            }
//
//            ElementXml elementCp = elementEa01.getChildNode("ea01condpgto")
//            if(elementCp != null){
//                String abe30codigo = elementCp.getChildValue("ab31codigo")
//                abe30 = buscarCondicaoPrecoCodigo(abe30codigo)
//            }

            Boolean temNull = false;
            if(abd01 == null){
                mensagens.add("Não foi encontrada o PCD com o codigo: " + abd01codigo + " para a nota num: " + abb01num)
                temNull = true;
            }

            if(abe01 == null){
                mensagens.add("Não foi encontrada a entidadade com o codigo: " + abe01codigo + " para a nota num: " + abb01num)
                temNull = true
            }

            if(aah01 == null){
                mensagens.add("Não foi encontrada o tipo de documento com o codigo: " + aah01codigo + " para a nota num: " + abb01num)
                temNull = true
            }

            if(temNull) continue;

            BigDecimal eaa01totItens = elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totItens")) != null ? new BigDecimal(elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totItens"))) : BigDecimal.ZERO;
            BigDecimal eaa01totDoc = elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totDoc")) != null ? new BigDecimal(elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totDoc"))) : BigDecimal.ZERO;
            BigDecimal eaa01totFinanc = elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totFinanc")) != null ? new BigDecimal(elementEa01.getChildValue(camposVlrFixosDoc.getString("eaa01totFinanc"))) : BigDecimal.ZERO;

            TableMap eaa01json = new TableMap()
            Set<String> camposLivresDoc = camposVlrLivresDoc.keySet()
            for(String nomeCampo in camposLivresDoc){
                String value = elementEa01.getChildValue(camposVlrLivresDoc.getString(nomeCampo))
                if(value != null){
                    BigDecimal valueBigDecimal = new BigDecimal(value);
                    eaa01json.put(nomeCampo, valueBigDecimal)
                }
            }

            try{
                Eaa01 eaa01 = srfService.comporDocumentoPadrao(abe01.abe01id,abd01.abd01id,null)
                eaa01.eaa01tp = abe40;
                eaa01.eaa01cp = abe30;
                eaa01.eaa01totItens = eaa01totItens
                eaa01.eaa01totDoc = eaa01totDoc
                eaa01.eaa01totFinanc = eaa01totFinanc
                eaa01.eaa01json = eaa01json
                eaa01.eaa01iData = eaa01.eaa01esMov == Eaa01.ESMOV_SAIDA ? abb01data : ea01dtes
                eaa01.eaa01rep0 = buscarRepresentante(elementEa01, 0)
                eaa01.eaa01rep1 = buscarRepresentante(elementEa01, 1)
                eaa01.eaa01rep2 = buscarRepresentante(elementEa01, 2)
                eaa01.eaa01rep3 = buscarRepresentante(elementEa01, 3)
                eaa01.eaa01rep4 = buscarRepresentante(elementEa01, 4)
                eaa01.eaa01txComis0 = buscarTaxaRepresentante(elementEa01, 0)
                eaa01.eaa01txComis1 = buscarTaxaRepresentante(elementEa01, 1)
                eaa01.eaa01txComis2 = buscarTaxaRepresentante(elementEa01, 2)
                eaa01.eaa01txComis3 = buscarTaxaRepresentante(elementEa01, 3)
                eaa01.eaa01txComis4 = buscarTaxaRepresentante(elementEa01, 4)
                eaa01.eaa01motivoDev = buscarMotivoDev(elementEa01)
                eaa01.eaa01sitDoc = buscarSitDoc(elementEa01)
                eaa01.eaa01cancData = buscarDataCancelamento(elementEa01)
                eaa01.eaa01cancObs = buscarCancObs(elementEa01)
                eaa01.eaa01cancMotivo = buscarMotivoCancelamento(elementEa01)
                eaa01.eaa01obsUsoInt = buscarObs(elementEa01, 1)
                eaa01.eaa01obsFisco = buscarObs(elementEa01, 2)
                eaa01.eaa01obsSAM = buscarObs(elementEa01, 3)
                eaa01.eaa01obsContrib = buscarObs(elementEa01, 4)
                eaa01.eaa01obsRetInd = buscarObs(elementEa01, 5)
                eaa01.eaa01nfeChave = buscarValorStringXml(elementEa01, "ea01chavenfe")

                eaa01.eaa01nfeStat =  buscarNfeStatus(elementEa01)

                eaa01.eaa01nfeCod = buscarValorIntegerXml(elementEa01, "ea01codvalidnfe")
                eaa01.eaa01nfeDescr = buscarValorStringXml(elementEa01, "ea01descrvalidnfe")
                eaa01.eaa01nfeProt = buscarValorStringXml(elementEa01, "ea01protnfe")
                eaa01.eaa01nfeData = buscarValorDateXml(elementEa01, "ea01dtrecnfe")
                eaa01.eaa01nfeHora = buscarValorTimeXml(elementEa01, "ea01horarecnfe")
                //eaa01.eaa01esData = abb01data
                eaa01.eaa01esData = buscarValorDateXml(elementEa01, "ea01dtes")
                eaa01.eaa01esHora = LocalTime.parse(elementEa01.getChildValue("ea01hremis"))

                eaa01.eaa01central.abb01tipo = aah01;
                eaa01.eaa01central.abb01num = abb01num
                eaa01.eaa01central.abb01data = abb01data;
                eaa01.eaa01central.abb01operHora = LocalTime.parse(elementEa01.getChildValue("ea01hremis"))
                eaa01.eaa01central.abb01serie = abb01serie
                eaa01.eaa01central.abb01valor = eaa01totDoc
                eaa01.eaa01central.abb01operAutor = "CAS4801";
                eaa01.eaa01central.abb01eg = obterEmpresaAtiva()

                if(eaa01.eaa0102s == null) eaa01.eaa0102s = new ArrayList();

                List<Eaa0102> eaa0102s = eaa01.eaa0102s.toList();

                Eaa0102 eaa0102 = eaa01.eaa0102s.size() > 0 ? eaa01.eaa0102s.getAt(0) : new Eaa0102()

                eaa0102.eaa0102despacho = buscarDespacho(elementEa01)
                eaa0102.eaa0102redespacho = buscarRedespacho(elementEa01)

                eaa0102s.add(0,eaa0102)
                eaa01.eaa0102s = eaa0102s;

                ElementXml elementEaa011s = elementEa01.getChildNode("ea011s")

                if (elementEaa011s != null) {
                    List<ElementXml> elementsEa011s = elementEaa011s.getChildNodes("Ea011")

                    for (ElementXml elementEa011 in elementsEa011s) {
                        ElementXml elementItem = elementEa011.getChildNode("ea011item")

                        String abm01codigo = elementItem.getChildValue("ab50codigo")
                        Integer eaa0103tipo = Integer.parseInt(elementItem.getChildValue("ab50mps"));



                        Abm01 abm01 = buscarItemCodigo(abm01codigo, eaa0103tipo)

                        if (abm01 == null) {
                            mensagens.add("Item da nota: " + abb01num + ", não encontrado com o codigo: " + abm01codigo)
                            continue
                        }
                        Eaa0103 eaa0103 = srfService.comporItemDoDocumentoPadrao(eaa01, abm01.abm01id);

                        Integer eaa0103seq = Integer.parseInt(elementEa011.getChildValue("ea011seq"));
                        BigDecimal eaa0103qtComl = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103qtComl")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103qtComl"))) : BigDecimal.ZERO;
                        BigDecimal eaa0103qtUso = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103qtUso")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103qtUso"))) : BigDecimal.ZERO;
                        BigDecimal eaa0103unit = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103unit")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103unit"))) : BigDecimal.ZERO;
                        BigDecimal eaa0103total = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103total")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103total"))) : BigDecimal.ZERO;
                        BigDecimal eaa0103totDoc = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103totDoc")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103totDoc"))) : BigDecimal.ZERO;
                        BigDecimal eaa0103totFinanc = elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103totFinanc")) != null ? new BigDecimal(elementEa011.getChildValue(camposVlrFixosItem.getString("eaa0103totFinanc"))) : BigDecimal.ZERO;

                        eaa0103.eaa0103seq = eaa0103seq;
                        eaa0103.eaa0103qtComl = eaa0103qtComl;
                        eaa0103.eaa0103qtUso = eaa0103qtUso;
                        eaa0103.eaa0103unit = eaa0103unit;
                        eaa0103.eaa0103total = eaa0103total;
                        eaa0103.eaa0103totDoc = eaa0103totDoc;
                        eaa0103.eaa0103totFinanc = eaa0103totFinanc

                        eaa0103.eaa0103ncm = buscarNCMItem(elementEa011)
                        eaa0103.eaa0103cstIcms = buscarCSTICMItem(elementEa011)
                        eaa0103.eaa0103cstIpi = buscarCSTIPIItem(elementEa011)
                        eaa0103.eaa0103cstPis = buscarCSTPISItem(elementEa011)
                        eaa0103.eaa0103cstCofins = buscarCSTCofinsItem(elementEa011)
                        eaa0103.eaa0103csosn = buscarCsosnItem(elementEa011)
                        eaa0103.eaa0103codBcCred = buscarCodBcCredItem(elementEa011)
                        eaa0103.eaa0103origemCred = buscarValorIntegerXml(elementEa011, "ea011origemcred")
                        eaa0103.eaa0103clasReceita = buscarValorIntegerXml(elementEa011, "ea011clasreceita")
                        eaa0103.eaa0103cfop = buscarCfopItem(elementEa011)
                        eaa0103.eaa0103txComis0 = buscarTaxaComissaoItem(elementEa011, 0)
                        eaa0103.eaa0103txComis1 = buscarTaxaComissaoItem(elementEa011, 1)
                        eaa0103.eaa0103txComis2 = buscarTaxaComissaoItem(elementEa011, 2)
                        eaa0103.eaa0103txComis3 = buscarTaxaComissaoItem(elementEa011, 3)
                        eaa0103.eaa0103txComis4 = buscarTaxaComissaoItem(elementEa011, 4)
                        eaa0103.eaa0103pcNum = buscarValorStringXml(elementEa011, "ea011pedcli")
                        eaa0103.eaa0103pcSeq = buscarValorIntegerXml(elementEa011, "ea011seqpedcli")
                        eaa0103.eaa0103issExig = buscarValorIntegerXml(elementEa011, "ea011indiciss")
                        eaa0103.eaa0103ativCP = buscarAtividadeCP(elementEa011)

                        if (eaa0103.eaa0103ncm == null) {
                            ElementXml NCM = elementEa011.getChildNode("ea011ncm")
                            if(NCM != null){
                                String abg01codigo = NCM.getChildValue("ab10codigo")

                                if (abg01codigo != "") {
                                    mensagens.add("Não foi encontrado o NCM: " + abg01codigo + "para a nota num: " + abb01num)
                                } else {
                                    mensagens.add("Não foi informado o NCM para a nota num: " + abb01num)
                                }
                            }
                        }


                        TableMap eaa0103json = new TableMap()
                        Set<String> camposLivres = camposVlrLivresItem.keySet()
                        for (String campoLivre in camposLivres) {
                            String value = elementEa011.getChildValue(camposVlrLivresItem.getString(campoLivre))
                            if (value != null) {
                                BigDecimal valueBigDecimal = new BigDecimal(value);
                                eaa0103json.put(campoLivre, valueBigDecimal)
                            }
                        }
                        eaa0103.eaa0103json = eaa0103json;

                        eaa01.addToEaa0103s(eaa0103);
                    }
                }

                eaa01s.add(eaa01)

            } catch (Exception err) {
                err.printStackTrace()
                mensagens.add(err.getMessage()+"código da entidade: "+abe01codigo)
            }

        }

        for(Eaa01 eaa01 in eaa01s){
            try{
                Abb01 abb01 = eaa01.eaa01central;
                getSession().persist(abb01)
                getSession().persistAll(eaa01)
            }catch (MultiValidationException ex){
                //interromper("parou aqui")
                interromper(ex.validations.get(0).message)

            }
        }

        if(mensagens.size() > 0){
            Aba20 aba20 = session.createCriteria(Aba20.class).addWhere(Criterions.eq("aba20codigo", "Avisos")).get(ColumnType.ENTITY);

            for(String mensagem : mensagens){
                Aba2001 aba2001 = new Aba2001();
                aba2001.setAba2001rd(aba20)
                aba2001.setAba2001lcto(mensagens.indexOf(mensagem))
                aba2001.setAba2001prop("Aviso")
                TableMap json = new TableMap()
                json.put("msg", mensagem)
                aba2001.setAba2001json(json);

                session.persist(aba2001)
            }

            interromper("Repositorio de dados preenchido!")
        }

    }

    private Integer buscarValorIntegerXml(ElementXml elementEa01, String nomeCampo){
        return elementEa01.getChildValue(nomeCampo) != null ? Integer.parseInt(elementEa01.getChildValue(nomeCampo)) : null;
    }

    private LocalDate buscarValorDateXml(ElementXml elementEa01, String nomeCampo){
        return elementEa01.getChildValue(nomeCampo) != null ? DateUtils.parseDate(elementEa01.getChildValue(nomeCampo), "dd/MM/yyyy") : null;
    }

    private LocalTime buscarValorTimeXml(ElementXml elementEa01, String nomeCampo){
        return elementEa01.getChildValue(nomeCampo) != null ? LocalTime.parse(elementEa01.getChildValue(nomeCampo)) : null;
    }

    private String buscarValorStringXml(ElementXml elementEa01, String nomeCampo){
        return elementEa01.getChildValue(nomeCampo);
    }

    private String buscarObs(ElementXml elementEa01, Integer obs){
        return elementEa01.getChildValue("ea01obs"+obs);
    }


    private Integer buscarNfeStatus(ElementXml elementEa01){
        //Integer value = buscarValorIntegerXml(elementEa01, "ea01validnfe")

        Integer operacao = buscarValorIntegerXml(elementEa01, "ea01tipooper")
        Integer emissao  = buscarValorIntegerXml(elementEa01, "ea01emissao")
        String chavenfe = buscarValorStringXml(elementEa01, "ea01chavenfe")
        Integer validnfe = buscarValorIntegerXml(elementEa01, "ea01validnfe")

        if((operacao == 0)&&(emissao == 1)&&(chavenfe != "")&&(validnfe == 0)){
            validnfe = 3
            return validnfe;

        }else{
            if(validnfe == null) return null;
            if(validnfe == 6) return 0
            if(validnfe == 7) return 6
            if(validnfe == 8) return 7
            return validnfe;
        }
    }

    private Abg01 buscarNCMItem(ElementXml elementEa01){

        ElementXml elementNcm = elementEa01.getChildNode("ea011ncm")
        if(elementNcm == null) return null

        String abg01codigo = elementNcm.getChildValue("ab10codigo")

        return getSession().createCriteria(Abg01.class)
                .addWhere(Criterions.eq("abg01codigo",abg01codigo))
                .addWhere(samWhere.getCritPadrao(Abg01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }



    private Aaj10 buscarCSTICMItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011csticms")
        if(elementNcm == null) return null
        String aaj10codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj10.class)
                .addWhere(Criterions.eq("aaj10codigo",aaj10codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj11 buscarCSTIPIItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011cstipi")
        if(elementNcm == null) return null
        String aaj11codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj11.class)
                .addWhere(Criterions.eq("aaj11codigo",aaj11codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj12 buscarCSTPISItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011cstpis")
        if(elementNcm == null) return null
        String aaj12codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj12.class)
                .addWhere(Criterions.eq("aaj12codigo",aaj12codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj13 buscarCSTCofinsItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011cstcofins")
        if(elementNcm == null) return null
        String aaj13codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj13.class)
                .addWhere(Criterions.eq("aaj13codigo",aaj13codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj14 buscarCsosnItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011csosn")
        if(elementNcm == null) return null
        String aaj14codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj14.class)
                .addWhere(Criterions.eq("aaj14codigo",aaj14codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj15 buscarCfopItem(ElementXml elementEa01){
        ElementXml elementNcm = elementEa01.getChildNode("ea011cfop")
        if(elementNcm == null) return null
        String aaj15codigo = elementNcm.getChildValue("ab13codigo")
        return getSession().createCriteria(Aaj15.class)
                .addWhere(Criterions.eq("aaj15codigo",aaj15codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aaj30 buscarCodBcCredItem(ElementXml elementEa011){
        ElementXml elementNcm = elementEa011.getChildNode("ea011codbccred")
        if(elementNcm == null) return null
        String aaj30codigo = elementNcm.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj30.class)
                .addWhere(Criterions.eq("aaj30codigo",aaj30codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aae11 buscarMotivoCancelamento(ElementXml elementEa01){
        ElementXml elementMot = elementEa01.getChildNode("ea01cancmotivo")
        if(elementMot == null) return null
        String aae11codigo = elementMot.getChildValue("ab19codigo")
        return getSession().createCriteria(Aae11.class)
                .addWhere(Criterions.eq("aae11codigo",aae11codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abg02 buscarAtividadeCP(ElementXml elementEa011){
        ElementXml elementCP = elementEa011.getChildNode("ea011contrprev")
        if(elementCP == null) return null
        String abg02codigo = elementCP.getChildValue("ab55codigo")
        return getSession().createCriteria(Abg02.class)
                .addWhere(Criterions.eq("abg02codigo",abg02codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private LocalDate buscarDataCancelamento(ElementXml elementEa01){
        return elementEa01.getChildValue("ea01cancdata") != null ? DateUtils.parseDate(elementEa01.getChildValue("ea01cancdata"), "dd/MM/yyyy") : null
    }

    private String buscarCancObs(ElementXml elementEa01){
        return elementEa01.getChildValue("ea01cancobs");
    }

    private Aaj03 buscarSitDoc(ElementXml elementEa01){
        ElementXml elementSit = elementEa01.getChildNode("ea01sitdoc")
        if(elementSit == null) return null
        String aaj03codigo = elementSit.getChildValue("ab19codigo")
        return getSession().createCriteria(Aaj03.class)
                .addWhere(Criterions.eq("aaj03codigo",aaj03codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Aae10 buscarMotivoDev(ElementXml elementEa01){
        ElementXml elementMot = elementEa01.getChildNode("ea01devmotivo")
        if(elementMot == null) return null
        String aae10codigo = elementMot.getChildValue("ab19codigo")
        return getSession().createCriteria(Aae10.class)
                .addWhere(Criterions.eq("aae10codigo",aae10codigo))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private BigDecimal buscarTaxaRepresentante(ElementXml elementEa01, Integer rep){
        String nomeTag = "ea01txcomis"+rep
        String value = elementEa01.getChildValue(nomeTag)
        if(value != null){
            return new BigDecimal(value)
        }else{
            return BigDecimal.ZERO
        }
    }
    private BigDecimal buscarTaxaComissaoItem(ElementXml elementEa01, Integer rep){
        String nomeTag = "ea011txcomis"+rep
        String value = elementEa01.getChildValue(nomeTag)
        if(value != null){
            return new BigDecimal(value)
        }else{
            return BigDecimal.ZERO
        }
    }
    private Abe01 buscarRepresentante(ElementXml elementEa01, Integer rep){
        String nomeTag = "ea01rep"+rep
        ElementXml elementRep = elementEa01.getChildNode(nomeTag)
        if(elementRep != null){
            String abe01codigo = elementRep.getChildValue("ab80codigo")
            return getSession().createCriteria(Abe01.class)
                    .addWhere(Criterions.eq("abe01codigo",abe01codigo))
                    .addWhere(getSamWhere().getCritPadrao(Abe01.class))
                    .setMaxResults(1)
                    .get(ColumnType.ENTITY)
        }else{
            return null;
        }
    }

    private Abd01 buscarPCDCodigo(String abd01codigo){
        if(mapeamentoPCD.getString(abd01codigo) != null && mapeamentoPCD.getString(abd01codigo) != ""){
            abd01codigo = mapeamentoPCD.getString(abd01codigo)
        }
        return getSession().createCriteria(Abd01.class)
                .addWhere(Criterions.eq("abd01codigo",abd01codigo))
                .addWhere(getSamWhere().getCritPadrao(Abd01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abe01 buscarEntidadeCodigo(String abe01codigo){
        if(mapeamentoEnt.getString(abe01codigo) != null && mapeamentoEnt.getString(abe01codigo) != ""){
            abe01codigo = mapeamentoEnt.getString(abe01codigo)
        }
        return getSession().createCriteria(Abe01.class)
                .addWhere(Criterions.eq("abe01codigo",abe01codigo))
                .addWhere(getSamWhere().getCritPadrao(Abe01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abe01 buscarDespacho(ElementXml elementEa01){
        ElementXml elementEa012 = elementEa01.getChildNode("ea012s")
        if(elementEa012 == null) return null;
        List<ElementXml> elementEa012s = elementEa012.getChildNodes("Ea012")
        if(elementEa012s == null || elementEa012s.size() < 1) return null
        ElementXml elementRep = elementEa012s.get(0).getChildNode("ea012despacho")
        if(elementRep != null){
            String abe01codigo = elementRep.getChildValue("ab80codigo")
            return getSession().createCriteria(Abe01.class)
                    .addWhere(Criterions.eq("abe01codigo",abe01codigo))
                    .addWhere(getSamWhere().getCritPadrao(Abe01.class))
                    .setMaxResults(1)
                    .get(ColumnType.ENTITY)
        }else{
            return null;
        }
    }

    private Abe01 buscarRedespacho(ElementXml elementEa01){
        ElementXml elementEa012 = elementEa01.getChildNode("ea012s")
        if(elementEa012 == null) return null;
        List<ElementXml> elementEa012s = elementEa012.getChildNodes("Ea012")
        if(elementEa012s == null || elementEa012s.size() < 1) return null
        ElementXml elementRep = elementEa012s.get(0).getChildNode("ea012redespacho")
        if(elementRep != null){
            String abe01codigo = elementRep.getChildValue("ab80codigo")
            return getSession().createCriteria(Abe01.class)
                    .addWhere(Criterions.eq("abe01codigo",abe01codigo))
                    .addWhere(getSamWhere().getCritPadrao(Abe01.class))
                    .setMaxResults(1)
                    .get(ColumnType.ENTITY)
        }else{
            return null;
        }
    }

    private Aah01 buscarTipoDocumentoCodigo(String aah01codigo){
        if(mapeamentoTipoDoc.getString(aah01codigo) != null && mapeamentoTipoDoc.getString(aah01codigo) != ""){
            aah01codigo = mapeamentoTipoDoc.getString(aah01codigo)
        }
        return getSession().createCriteria(Aah01.class)
                .addWhere(Criterions.eq("aah01codigo",aah01codigo))
                .addWhere(getSamWhere().getCritPadrao(Aah01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abe40 buscarTabelaPrecoCodigo(String abe40codigo){
        if(mapeamentoTabelaPreco.getString(abe40codigo) != null && mapeamentoTabelaPreco.getString(abe40codigo) != ""){
            abe40codigo = mapeamentoTabelaPreco.getString(abe40codigo)
        }
        return getSession().createCriteria(Abe40.class)
                .addWhere(Criterions.eq("abe40codigo",abe40codigo))
                .addWhere(getSamWhere().getCritPadrao(Abe40.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abe30 buscarCondicaoPrecoCodigo(String abe30codigo){
        if(mapeamentoCondicaoPagamento.getString(abe30codigo) != null && mapeamentoCondicaoPagamento.getString(abe30codigo) != ""){
            abe30codigo = mapeamentoCondicaoPagamento.getString(abe30codigo)
        }
        return getSession().createCriteria(Abe30.class)
                .addWhere(Criterions.eq("abe30codigo",abe30codigo))
                .addWhere(getSamWhere().getCritPadrao(Abe30.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private Abm01 buscarItemCodigo(String abm01codigo, Integer abm01tipo){

        if(abm01tipo  == 2){ // no sam4 o tipo 2 passou a ser tipo 3
            abm01tipo = 3
        }

        if(mapeamentoItem.getString(abm01codigo) != null && mapeamentoItem.getString(abm01codigo) != ""){
            abm01codigo = mapeamentoItem.getString(abm01codigo)
        }
        return getSession().createCriteria(Abm01.class)
                .addWhere(Criterions.eq("abm01codigo",abm01codigo))
                .addWhere(Criterions.eq("abm01tipo",abm01tipo))
                .addWhere(getSamWhere().getCritPadrao(Abm01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }




}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=