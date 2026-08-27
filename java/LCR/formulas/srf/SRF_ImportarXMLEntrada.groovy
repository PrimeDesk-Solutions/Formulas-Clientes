package LCR.formulas.srf

import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13

import java.time.LocalDateTime
import java.time.OffsetDateTime

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aam06
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro
import sam.model.entities.aa.Aaj10;

import sam.model.entities.aa.Aaj15;
import br.com.multiorm.criteria.criterion.Criterions

class ImportaXmlNFe_Entrada extends FormulaBase {

    private Eaa01 eaa01;
    private ElementXml elementXmlNfe;

    @Override
    public void executar() {
        eaa01 = get("eaa01");
        elementXmlNfe = get("elementXmlNfe");

        selecionarAlinhamento("02");

        if(!elementXmlNfe.getNodeName().equals("nfeProc")) return;

        ElementXml elementNFe = elementXmlNfe.getChildNode("NFe");

        if(elementNFe == null) return;

        ElementXml elementinfNFe = elementNFe.getChildNode("infNFe");
        if(elementinfNFe == null) return;

        ElementXml elementide = elementinfNFe.getChildNode("ide");
        if(elementide == null) return;

        String serie = elementide.getChildValue("serie");
        eaa01.eaa01central.setAbb01serie(serie);

        TableMap eaa01json = eaa01.getEaa01json();

        //Percorrendo os itens do XML
        List<ElementXml> det = elementinfNFe.getChildNodes("det");
        if(det != null && det.size() > 0) {
            for(ElementXml elementnItem : det) {
                Integer nItem = Integer.parseInt(elementnItem.getAttValue("nItem"));

                Eaa0103 eaa0103 = obterItemDoDocumentoPelaSeqItemXml(eaa01, nItem);
                if(eaa0103 == null) continue;

                TableMap eaa0103json = eaa0103.getEaa0103json();

                ElementXml elementprod = elementnItem.getChildNode("prod");

                def uTrib = elementprod.getChildValue("uTrib");
                def uCom = elementprod.getChildValue("uCom");

                uCom = uCom.toUpperCase().contains("CX") ? "CX" : uCom.toUpperCase().contains("UN") ? "UN" : uCom.toUpperCase().contains("KG") ? "KG" : uCom.toUpperCase().contains("L") ? "L" : uCom;

                Aam06 aam06trib = getAcessoAoBanco().buscarRegistroUnico("SELECT aam06id, aam06codigo FROM Aam06 WHERE aam06codigo = :uTrib", Parametro.criar("uTrib", uTrib));
                if(aam06trib != null) eaa0103.setEaa0103umt(aam06trib);

                // Unidade de Medida de Venda
                eaa0103.eaa0103json.put("umv", uCom);

                // CFOP
                def cfop = elementprod.getChildValue("CFOP");

                Aaj15 aaj15 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM aaj15 WHERE aaj15codigo = :cfop", Parametro.criar("cfop", cfop));

                eaa0103.eaa0103cfop = aaj15;

                setarValorJson(eaa0103json, getCampo("113-I14","qTrib"), obterValorXml(elementprod, "qTrib", 4));

                setarValorJson(eaa0103json, getCampo("113a-I14a","vUnTrib"), obterValorXml(elementprod, "vUnTrib", 2));

                setarValorJson(eaa0103json, getCampo("114-I15","vFrete"), obterValorXml(elementprod, "vFrete", 2));

                setarValorJson(eaa0103json, getCampo("115-I16","vSeg"), obterValorXml(elementprod, "vSeg", 2));

                setarValorJson(eaa0103json, getCampo("116-I17","vDesc"), obterValorXml(elementprod, "vDesc", 2));

                setarValorJson(eaa0103json, getCampo("116a-I17a","vOutro"), obterValorXml(elementprod, "vOutro", 2));


                //M. Tributos incidentes no Produto ou Serviço
                ElementXml elementimposto = elementnItem.getChildNode("imposto");

                setarValorJson(eaa0103json, getCampo("163a-M02","vTotTrib"), obterValorXml(elementimposto, "vTotTrib", 2));

                //N. ICMS Normal e ST
                ElementXml elementICMS = elementimposto.getChildNode("ICMS");

                //Grupo Tributação do ICMS = 00
                ElementXml elementICMS00 = elementICMS.getChildNode("ICMS00");

                if(elementICMS00 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000" ))
                    setarValorJson(eaa0103json, getCampo("169-N15","vBC"), obterValorXml(elementICMS00, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("170-N16","pICMS"), obterValorXml(elementICMS00, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("171-N17","vICMS"), obterValorXml(elementICMS00, "vICMS", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS00, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS00, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 10
                ElementXml elementICMS10 = elementICMS.getChildNode("ICMS10");
                if(elementICMS10 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "010" ))
                    setarValorJson(eaa0103json, getCampo("176-N15","vBC"), obterValorXml(elementICMS10, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("177-N16","pICMS"), obterValorXml(elementICMS10, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("178-N17","vICMS"), obterValorXml(elementICMS10, "vICMS", 2));
                    setarValorJson(eaa0103json, getCampo("180-N19","pMVAST"), obterValorXml(elementICMS10, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("181-N20","pRedBCST"), obterValorXml(elementICMS10, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("182-N21","vBCST"), obterValorXml(elementICMS10, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("183-N22","pICMSST"), obterValorXml(elementICMS10, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("184-N23","vICMSST"), obterValorXml(elementICMS10, "vICMSST", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS10, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS10, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS10, "vFCPST", 2));

                }

                //Grupo Tributação do ICMS = 20
                ElementXml elementICMS20 = elementICMS.getChildNode("ICMS20");
                if(elementICMS20 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "020" ))
                    setarValorJson(eaa0103json, getCampo("189-N14","pRedBC"), obterValorXml(elementICMS20, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("190-N15","vBC"), obterValorXml(elementICMS20, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("191-N16","pICMS"), obterValorXml(elementICMS20, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("192-N17","vICMS"), obterValorXml(elementICMS20, "vICMS", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS20, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS20, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS20, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 30
                ElementXml elementICMS30 = elementICMS.getChildNode("ICMS30");
                if(elementICMS30 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "030" ))
                    setarValorJson(eaa0103json, getCampo("197-N19","pMVAST"), obterValorXml(elementICMS30, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("198-N20","pRedBCST"), obterValorXml(elementICMS30, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("199-N21","vBCST"), obterValorXml(elementICMS30, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("200-N22","pICMSST"), obterValorXml(elementICMS30, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("201-N23","vICMSST"), obterValorXml(elementICMS30, "vICMSST", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS30, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS30, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS30, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 40
                ElementXml elementICMS40 = elementICMS.getChildNode("ICMS40");
                if(elementICMS40 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "040" ))
                    setarValorJson(eaa0103json, getCampo("204.01-N17","vICMS"), obterValorXml(elementICMS40, "vICMS", 2));
                }

                //Grupo Tributação do ICMS = 51
                ElementXml elementICMS51 = elementICMS.getChildNode("ICMS51");
                if(elementICMS51 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "051" ))
                    setarValorJson(eaa0103json, getCampo("209-N14","pRedBC"), obterValorXml(elementICMS51, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("210-N15","vBC"), obterValorXml(elementICMS51, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("211-N16","pICMS"), obterValorXml(elementICMS51, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("212-N17","vICMS"), obterValorXml(elementICMS51, "vICMS", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS51, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS51, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS51, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 60
                ElementXml elementICMS60 = elementICMS.getChildNode("ICMS60");
                if(elementICMS60 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "060" ))
                    setarValorJson(eaa0103json, getCampo("216-N26","vBCSTRet"), obterValorXml(elementICMS60, "vBCSTRet", 2));
                    setarValorJson(eaa0103json, getCampo("217-N27","vICMSSTRet"), obterValorXml(elementICMS60, "vICMSSTRet", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS60, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS60, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS60, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 70
                ElementXml elementICMS70 = elementICMS.getChildNode("ICMS70");
                if(elementICMS70 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "070" ))
                    setarValorJson(eaa0103json, getCampo("222-N14","pRedBC"), obterValorXml(elementICMS70, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("223-N15","vBC"), obterValorXml(elementICMS70, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("224-N16","pICMS"), obterValorXml(elementICMS70, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("225-N17","vICMS"), obterValorXml(elementICMS70, "vICMS", 2));
                    setarValorJson(eaa0103json, getCampo("227-N19","pMVAST"), obterValorXml(elementICMS70, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("228-N20","pRedBCST"), obterValorXml(elementICMS70, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("229-N21","vBCST"), obterValorXml(elementICMS70, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("230-N22","pICMSST"), obterValorXml(elementICMS70, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("231-N23","vICMSST"), obterValorXml(elementICMS70, "vICMSST", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS70, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS70, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS70, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS = 90
                ElementXml elementICMS90 = elementICMS.getChildNode("ICMS90");
                if(elementICMS90 != null) {
                    eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "090" ))
                    setarValorJson(eaa0103json, getCampo("236-N15","vBC"), obterValorXml(elementICMS90, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("237-N14","pRedBC"), obterValorXml(elementICMS90, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("238-N16","pICMS"), obterValorXml(elementICMS90, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("239-N17","vICMS"), obterValorXml(elementICMS90, "vICMS", 2));
                    setarValorJson(eaa0103json, getCampo("241-N19","pMVAST"), obterValorXml(elementICMS90, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("242-N20","pRedBCST"), obterValorXml(elementICMS90, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("243-N21","vBCST"), obterValorXml(elementICMS90, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("244-N22","pICMSST"), obterValorXml(elementICMS90, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245-N23","vICMSST"), obterValorXml(elementICMS90, "vICMSST", 2));

                    // FCP
                    setarValorJson(eaa0103json, getCampo("184.1-N23a","vBCFCPST"), obterValorXml(elementICMS90, "vBCFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.2-N23b","pFCPST"), obterValorXml(elementICMS90, "pFCPST", 2));
                    setarValorJson(eaa0103json, getCampo("184.4-N23d","vFCPST"), obterValorXml(elementICMS90, "vFCPST", 2));
                }

                //Grupo Tributação do ICMS Part
                ElementXml elementICMSPart = elementICMS.getChildNode("ICMSPart");
                if(elementICMSPart != null) {
                    setarValorJson(eaa0103json, getCampo("245.05-N15","vBC"), obterValorXml(elementICMSPart, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("245.06-N14","pRedBC"), obterValorXml(elementICMSPart, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("245.07-N16","pICMS"), obterValorXml(elementICMSPart, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("245.08-N17","vICMS"), obterValorXml(elementICMSPart, "vICMS", 2));
                    setarValorJson(eaa0103json, getCampo("245.10-N19","pMVAST"), obterValorXml(elementICMSPart, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("245.11-N20","pRedBCST"), obterValorXml(elementICMSPart, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.12-N21","vBCST"), obterValorXml(elementICMSPart, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.13-N22","pICMSST"), obterValorXml(elementICMSPart, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.14-N23","vICMSST"), obterValorXml(elementICMSPart, "vICMSST", 2));
                }

                //Grupo Tributação do ICMSST
                ElementXml elementICMSST = elementICMS.getChildNode("ICMSST");
                if(elementICMSST != null) {
                    setarValorJson(eaa0103json, getCampo("245.20-N26","vBCSTRet"), obterValorXml(elementICMSST, "vBCSTRet", 2));
                    setarValorJson(eaa0103json, getCampo("245.21-N27","vICMSSTRet"), obterValorXml(elementICMSST, "vICMSSTRet", 2));
                    setarValorJson(eaa0103json, getCampo("245.22-N31","vBCSTDest"), obterValorXml(elementICMSST, "vBCSTDest", 2));
                    setarValorJson(eaa0103json, getCampo("245.23-N32","vICMSSTDes"), obterValorXml(elementICMSST, "vICMSSTDes", 2));
                }

                //Grupo Tributação do ICMSSN101
                ElementXml elementICMSSN101 = elementICMS.getChildNode("ICMSSN101");
                if(elementICMSSN101 != null) {
                    setarValorJson(eaa0103json, getCampo("245.27-N29","pCredSN"), obterValorXml(elementICMSSN101, "pCredSN", 2));
                    setarValorJson(eaa0103json, getCampo("245.28-N30","vCredICMSSN"), obterValorXml(elementICMSSN101, "vCredICMSSN", 2));
                }

                //Grupo Tributação do ICMSSN201
                ElementXml elementICMSSN201 = elementICMS.getChildNode("ICMSSN201");
                if(elementICMSSN201 != null) {
                    setarValorJson(eaa0103json, getCampo("245.31-N19","pMVAST"), obterValorXml(elementICMSSN201, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("245.32-N20","pRedBCST"), obterValorXml(elementICMSSN201, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.33-N21","vBCST"), obterValorXml(elementICMSSN201, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.34-N22","pICMSST"), obterValorXml(elementICMSSN201, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.35-N23","vICMSST"), obterValorXml(elementICMSSN201, "vICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.36-N29","pCredSN"), obterValorXml(elementICMSSN201, "pCredSN", 2));
                    setarValorJson(eaa0103json, getCampo("245.37-N30","vCredICMSSN"), obterValorXml(elementICMSSN201, "vCredICMSSN", 2));
                }

                //Grupo Tributação do ICMSSN202
                ElementXml elementICMSSN202 = elementICMS.getChildNode("ICMSSN202");
                if(elementICMSSN201 != null) {
                    setarValorJson(eaa0103json, getCampo("245.42-N19","pMVAST"), obterValorXml(elementICMSSN202, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("245.43-N20","pRedBCST"), obterValorXml(elementICMSSN202, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.44-N21","vBCST"), obterValorXml(elementICMSSN202, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.45-N22","pICMSST"), obterValorXml(elementICMSSN202, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.46-N23","vICMSST"), obterValorXml(elementICMSSN202, "vICMSST", 2));
                }

                //Grupo Tributação do ICMSSN500
                ElementXml elementICMSSN500 = elementICMS.getChildNode("ICMSSN500");
                if(elementICMSSN500 != null) {
                    setarValorJson(eaa0103json, getCampo("245.50-N26","vBCSTRet"), obterValorXml(elementICMSSN500, "vBCSTRet", 2));
                    setarValorJson(eaa0103json, getCampo("245.51-N27","vICMSSTRet"), obterValorXml(elementICMSSN500, "vICMSSTRet", 2));
                }

                //Grupo Tributação do ICMSSN900
                ElementXml elementICMSSN900 = elementICMS.getChildNode("ICMSSN900");
                if(elementICMSSN900 != null) {
                    setarValorJson(eaa0103json, getCampo("245.56-N15","vBC"), obterValorXml(elementICMSSN900, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("245.57-N14","pRedBC"), obterValorXml(elementICMSSN900, "pRedBC", 2));
                    setarValorJson(eaa0103json, getCampo("245.58-N16","pICMS"), obterValorXml(elementICMSSN900, "pICMS", 2));
                    setarValorJson(eaa0103json, getCampo("245.59-N17","vICMS"), obterValorXml(elementICMSSN900, "vICMS", 2));
                    setarValorJson(eaa0103json, getCampo("245.61-N19","pMVAST"), obterValorXml(elementICMSSN900, "pMVAST", 2));
                    setarValorJson(eaa0103json, getCampo("245.62-N20","pRedBCST"), obterValorXml(elementICMSSN900, "pRedBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.63-N21","vBCST"), obterValorXml(elementICMSSN900, "vBCST", 2));
                    setarValorJson(eaa0103json, getCampo("245.64-N22","pICMSST"), obterValorXml(elementICMSSN900, "pICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.65-N23","vICMSST"), obterValorXml(elementICMSSN900, "vICMSST", 2));
                    setarValorJson(eaa0103json, getCampo("245.52-N29","pCredSN"), obterValorXml(elementICMSSN900, "pCredSN", 2));
                    setarValorJson(eaa0103json, getCampo("245.53-N30","vCredICMSSN"), obterValorXml(elementICMSSN900, "vCredICMSSN", 2));
                }

                //O. Imposto sobre Produtos Industrializados - IPI
                ElementXml elementIPI = elementimposto.getChildNode("IPI");
                if(elementIPI != null) {

                    ElementXml elementIPITrib = elementIPI.getChildNode("IPITrib");
                    if(elementIPITrib != null) {
                        setarValorJson(eaa0103json, getCampo("254-O10","vBC"), obterValorXml(elementIPITrib, "vBC", 2));
                        setarValorJson(eaa0103json, getCampo("257-O13","pIPI"), obterValorXml(elementIPITrib, "pIPI", 2));

                        setarValorJson(eaa0103json, getCampo("255-O11","qUnid"), obterValorXml(elementIPITrib, "qUnid", 4));
                        setarValorJson(eaa0103json, getCampo("256-O12","vUnid"), obterValorXml(elementIPITrib, "vUnid", 4));

                        setarValorJson(eaa0103json, getCampo("259-O14","vIPI"), obterValorXml(elementIPITrib, "vIPI", 2));
                    }

                    // IPI Interno (IPIINT)
                    ElementXml elementIpiInt = elementIPI.getChildNode("IPINT");
                    if(elementIpiInt != null){

                        def cstIPI = elementIpiInt.getChildValue("CST");

                        // CST IPI
                        Aaj11 aaj11 = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", cstIPI));

                        if(aaj11 == null) interromper("Não foi encontrado CST de IPI com o código " + cstIPI)

                        eaa0103.eaa0103cstIpi = aaj11;
                    }
                }

                //P. Imposto de Importação - II
                ElementXml elementII = elementimposto.getChildNode("II");
                if(elementII != null) {
                    setarValorJson(eaa0103json, getCampo("263-P02","vIPI"), obterValorXml(elementII, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("264-P03","vDespAdu"), obterValorXml(elementII, "vDespAdu", 2));
                    setarValorJson(eaa0103json, getCampo("265-P04","vII"), obterValorXml(elementII, "vII", 2));
                    setarValorJson(eaa0103json, getCampo("266-P05","vIOF"), obterValorXml(elementII, "vIOF", 2));
                }

                //Q. PIS
                ElementXml elementPIS = elementimposto.getChildNode("PIS");
                if(elementPIS != null) {

                    ElementXml elementPISAliq = elementPIS.getChildNode("PISAliq");
                    if(elementPISAliq != null) {
                        setarValorJson(eaa0103json, getCampo("270-Q07","vBC"), obterValorXml(elementPISAliq, "vBC", 2));
                        setarValorJson(eaa0103json, getCampo("271-Q08","pPIS"), obterValorXml(elementPISAliq, "pPIS", 2));
                        setarValorJson(eaa0103json, getCampo("272-Q09","vPIS"), obterValorXml(elementPISAliq, "vPIS", 2));
                    }

                    ElementXml elementPISQtde = elementPIS.getChildNode("PISQtde");
                    if(elementPISQtde != null) {
                        setarValorJson(eaa0103json, getCampo("275-Q10","qBCProd"), obterValorXml(elementPISQtde, "qBCProd", 4));
                        setarValorJson(eaa0103json, getCampo("276-Q11","vAliqProd"), obterValorXml(elementPISQtde, "vAliqProd", 4));
                        setarValorJson(eaa0103json, getCampo("277-Q09","vPIS"), obterValorXml(elementPISQtde, "vPIS", 2));
                    }

                    ElementXml elementPISOutr = elementPIS.getChildNode("PISOutr");
                    if(elementPISOutr != null) {
                        def cstPIS = elementPISOutr.getChildValue("CST");

                        // CST PIS
                        Aaj12 aaj12 = getSession().get(Aaj12.class, Criterions.eq("aaj12codigo", cstPIS));

                        if(aaj12 == null) interromper("Não foi encontrado CST de PIS com o código " + cstPIS);

                        setarValorJson(eaa0103json, getCampo("282-Q07","vBC"), obterValorXml(elementPISOutr, "vBC", 2));
                        setarValorJson(eaa0103json, getCampo("283-Q08","pPIS"), obterValorXml(elementPISOutr, "pPIS", 2));
                        setarValorJson(eaa0103json, getCampo("284-Q10","qBCProd"), obterValorXml(elementPISOutr, "qBCProd", 4));
                        setarValorJson(eaa0103json, getCampo("285-Q11","vAliqProd"), obterValorXml(elementPISOutr, "vAliqProd", 4));
                        setarValorJson(eaa0103json, getCampo("286-Q09","vPIS"), obterValorXml(elementPISOutr, "vPIS", 2));

                        eaa0103.eaa0103cstPis = aaj12;
                    }
                }

                //R. PIS ST
                ElementXml elementPISST = elementimposto.getChildNode("PISST");
                if(elementPISST != null) {
                    setarValorJson(eaa0103json, getCampo("288-R02","vBC"), obterValorXml(elementPISST, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("289-R03","pPIS"), obterValorXml(elementPISST, "pPIS", 2));
                    setarValorJson(eaa0103json, getCampo("290-R04","qBCProd"), obterValorXml(elementPISST, "qBCProd", 4));
                    setarValorJson(eaa0103json, getCampo("291-R05","vAliqProd"), obterValorXml(elementPISST, "vAliqProd", 4));
                    setarValorJson(eaa0103json, getCampo("292-R06","vPIS"), obterValorXml(elementPISST, "vPIS", 2));
                }

                //S. COFINS
                ElementXml elementCOFINS = elementimposto.getChildNode("COFINS");
                if(elementCOFINS != null) {

                    ElementXml elementCOFINSAliq = elementCOFINS.getChildNode("COFINSAliq");
                    if(elementCOFINSAliq != null) {
                        setarValorJson(eaa0103json, getCampo("296-S07","vBC"), obterValorXml(elementCOFINSAliq, "vBC", 2));
                        setarValorJson(eaa0103json, getCampo("297-S08","pCOFINS"), obterValorXml(elementCOFINSAliq, "pCOFINS", 4));
                        setarValorJson(eaa0103json, getCampo("298-S11","vCOFINS"), obterValorXml(elementCOFINSAliq, "vCOFINS", 2));
                    }

                    ElementXml elementCOFINSQtde = elementCOFINS.getChildNode("COFINSQtde");
                    if(elementCOFINSQtde != null) {
                        setarValorJson(eaa0103json, getCampo("301-S09","qBCProd"), obterValorXml(elementCOFINSQtde, "qBCProd", 4));
                        setarValorJson(eaa0103json, getCampo("302-S10","vAliqProd"), obterValorXml(elementCOFINSQtde, "vAliqProd", 4));
                        setarValorJson(eaa0103json, getCampo("303-S11","vCOFINS"), obterValorXml(elementCOFINSQtde, "vCOFINS", 2));
                    }

                    ElementXml elementCOFINSOutr = elementCOFINS.getChildNode("COFINSOutr");
                    if(elementCOFINSOutr != null) {
                        def cstCOFINS = elementCOFINSOutr.getChildValue("CST");

                        // CST COFINS
                        Aaj13 aaj13 = getSession().get(Aaj13.class, Criterions.eq("aaj13codigo", cstCOFINS));

                        if(aaj13 == null) interromper("Não foi encontrado CST de PIS com o código " + cstCOFINS);

                        setarValorJson(eaa0103json, getCampo("308-S07","vBC"), obterValorXml(elementCOFINSOutr, "vBC", 2));
                        setarValorJson(eaa0103json, getCampo("309-S08","pCOFINS"), obterValorXml(elementCOFINSOutr, "pCOFINS", 2));
                        setarValorJson(eaa0103json, getCampo("310-S09","qBCProd"), obterValorXml(elementCOFINSOutr, "qBCProd", 4));
                        setarValorJson(eaa0103json, getCampo("311-S10","vAliqProd"), obterValorXml(elementCOFINSOutr, "vAliqProd", 4));
                        setarValorJson(eaa0103json, getCampo("312-S11","vCOFINS"), obterValorXml(elementCOFINSOutr, "vCOFINS", 2));
                        eaa0103.eaa0103cstCofins = aaj13;
                    }
                }

                //T. COFINS ST
                ElementXml elementCOFINSST = elementimposto.getChildNode("COFINSST");
                if(elementCOFINSST != null) {
                    setarValorJson(eaa0103json, getCampo("314-T02","vBC"), obterValorXml(elementCOFINSST, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("315-T03","pCOFINS"), obterValorXml(elementCOFINSST, "pCOFINS", 2));
                    setarValorJson(eaa0103json, getCampo("316-T04","qBCProd"), obterValorXml(elementCOFINSST, "qBCProd", 4));
                    setarValorJson(eaa0103json, getCampo("317-T05","vAliqProd"), obterValorXml(elementCOFINSST, "vAliqProd", 4));
                    setarValorJson(eaa0103json, getCampo("318-T06","vCOFINS"), obterValorXml(elementCOFINSST, "vCOFINS", 2));
                }

                //U. ISSQN
                ElementXml elementISSQN = elementimposto.getChildNode("ISSQN");
                if(elementISSQN != null) {
                    setarValorJson(eaa0103json, getCampo("320-U02","vBC"), obterValorXml(elementISSQN, "vBC", 2));
                    setarValorJson(eaa0103json, getCampo("321-U03","vAliq"), obterValorXml(elementISSQN, "vAliq", 2));
                    setarValorJson(eaa0103json, getCampo("322-U04","vISSQN"), obterValorXml(elementISSQN, "vISSQN", 2));
                }
            }
        }

        //W. Total da NF-e
        ElementXml elementtotal = elementinfNFe.getChildNode("total");

        ElementXml elementICMSTot = elementtotal.getChildNode("ICMSTot");

        setarValorJson(eaa01json, getCampo("328-W03","vBC"), obterValorXml(elementICMSTot, "vBC", 2));
        setarValorJson(eaa01json, getCampo("329-W04","vICMS"), obterValorXml(elementICMSTot, "vICMS", 2));
        setarValorJson(eaa01json, getCampo("330-W05","vBCST"), obterValorXml(elementICMSTot, "vBCST", 2));
        setarValorJson(eaa01json, getCampo("331-W06","vST"), obterValorXml(elementICMSTot, "vST", 2));
        setarValorJson(eaa01json, getCampo("332-W07","vProd"), obterValorXml(elementICMSTot, "vProd", 2));
        setarValorJson(eaa01json, getCampo("333-W08","vFrete"), obterValorXml(elementICMSTot, "vFrete", 2));
        setarValorJson(eaa01json, getCampo("334-W09","vSeg"), obterValorXml(elementICMSTot, "vSeg", 2));
        setarValorJson(eaa01json, getCampo("335-W10","vDesc"), obterValorXml(elementICMSTot, "vDesc", 2));
        setarValorJson(eaa01json, getCampo("336-W11","vII"), obterValorXml(elementICMSTot, "vII", 2));
        setarValorJson(eaa01json, getCampo("337-W12","vIPI"), obterValorXml(elementICMSTot, "vIPI", 2));
        setarValorJson(eaa01json, getCampo("338-W13","vPIS"), obterValorXml(elementICMSTot, "vPIS", 2));
        setarValorJson(eaa01json, getCampo("339-W14","vCOFINS"), obterValorXml(elementICMSTot, "vCOFINS", 2));
        setarValorJson(eaa01json, getCampo("340-W15","vOutro"), obterValorXml(elementICMSTot, "vOutro", 2));
        setarValorJson(eaa01json, getCampo("341-W16","vNF"), obterValorXml(elementICMSTot, "vNF", 2));
        setarValorJson(eaa01json, getCampo("341a-W16a","vTotTrib"), obterValorXml(elementICMSTot, "vTotTrib", 2));

        ElementXml elementISSQNtot = elementtotal.getChildNode("ISSQNtot");

        if(elementISSQNtot != null) {
            setarValorJson(eaa01json, getCampo("343-W18","vServ"), obterValorXml(elementISSQNtot, "vServ", 2));
            setarValorJson(eaa01json, getCampo("344-W19","vBC"), obterValorXml(elementISSQNtot, "vBC", 2));
            setarValorJson(eaa01json, getCampo("345-W20","vISS"), obterValorXml(elementISSQNtot, "vISS", 2));
            setarValorJson(eaa01json, getCampo("346-W21","vPIS"), obterValorXml(elementISSQNtot, "vPIS", 2));
            setarValorJson(eaa01json, getCampo("347-W22","vCOFINS"), obterValorXml(elementISSQNtot, "vCOFINS", 2));
        }

        ElementXml elementretTrib = elementtotal.getChildNode("retTrib");

        if(elementretTrib != null) {
            setarValorJson(eaa01json, getCampo("349-W24","vRetPIS"), obterValorXml(elementretTrib, "vRetPIS", 2));
            setarValorJson(eaa01json, getCampo("350-W25","vRetCOFINS"), obterValorXml(elementretTrib, "vRetCOFINS", 2));
            setarValorJson(eaa01json, getCampo("351-W26","vRetCSLL"), obterValorXml(elementretTrib, "vRetCSLL", 2));
            setarValorJson(eaa01json, getCampo("352-W27","vBCIRRF"), obterValorXml(elementretTrib, "vBCIRRF", 2));
            setarValorJson(eaa01json, getCampo("353-W28","vIRRF"), obterValorXml(elementretTrib, "vIRRF", 2));
            setarValorJson(eaa01json, getCampo("354-W29","vBCRetPrev"), obterValorXml(elementretTrib, "vBCRetPrev", 2));
            setarValorJson(eaa01json, getCampo("355-W30","vRetPrev"), obterValorXml(elementretTrib, "vRetPrev", 2));
        }

        //X. Informações do Transporte da NF-e
        ElementXml elementtransp = elementinfNFe.getChildNode("transp");

        List<ElementXml> vol = elementtransp.getChildNodes("vol");
        if(vol != null && vol.size() > 0) {
            for(ElementXml elementnvol : vol) {
                setarValorJson(eaa01json, getCampo("382-X27","qVol"), obterValorXml(elementnvol, "qVol", 0));
                setarValorJson(eaa01json, getCampo("386-X31","pesoL"), obterValorXml(elementnvol, "pesoL", 3));
                setarValorJson(eaa01json, getCampo("387-X32","pesoB"), obterValorXml(elementnvol, "pesoB", 3));

                Eaa0103 eaa0103 = obterItemDoDocumentoPelaSeqItemXml(eaa01, 1);
                if(eaa0103 != null) {
                    TableMap eaa0103json = eaa0103.getEaa0103json();

                    setarValorJson(eaa0103json, getCampo("382-X27","qVol"), obterValorXml(elementnvol, "qVol", 2));
                    setarValorJson(eaa0103json, getCampo("386-X31","pesoL"), obterValorXml(elementnvol, "pesoL", 2));
                    setarValorJson(eaa0103json, getCampo("387-X32","pesoB"), obterValorXml(elementnvol, "pesoB", 2));
                }
            }
        }

        //Z. Informações Adicionais da NF-e
        ElementXml elementinfAdic = elementinfNFe.getChildNode("infAdic");
        if(elementinfAdic != null) {
            String infAdFisco = elementinfAdic.getChildValue("infAdFisco");
            eaa01.setEaa01obsFisco(infAdFisco);

            String infCpl = elementinfAdic.getChildValue("infCpl");
            eaa01.setEaa01obsContrib(infCpl);
        }

        //Protocolo da NF-e
        ElementXml elementprotNFe = elementXmlNfe.getChildNode("protNFe");
        if(elementprotNFe != null) {
            ElementXml elementinfProt = elementprotNFe.getChildNode("infProt");
            if(elementinfProt != null) {
                String strDhRecbto = elementinfProt.getChildValue("dhRecbto");
                OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(strDhRecbto);
                LocalDateTime dhRecbto = odtInstanceAtOffset.toLocalDateTime();
                eaa01.setEaa01nfeData(dhRecbto.toLocalDate());
                eaa01.setEaa01nfeHora(dhRecbto.toLocalTime());

                String nProt = elementinfProt.getChildValue("nProt");
                eaa01.setEaa01nfeProt(nProt);

                Integer cStat = Integer.parseInt(elementinfProt.getChildValue("cStat"));
                eaa01.setEaa01nfeCod(cStat);

                String xMotivo = elementinfProt.getChildValue("xMotivo");
                eaa01.setEaa01nfeDescr(xMotivo);
            }
        }

    }

    private BigDecimal obterValorXml(ElementXml element, String nomeTagXml, int casasDecimais) {
        String conteudo = element.getChildValue(nomeTagXml);
        BigDecimal valor = conteudo == null ? BigDecimal.ZERO : new BigDecimal(conteudo);
        return round(valor, casasDecimais);
    }

    private void setarValorJson(TableMap json, String nomeCampo, BigDecimal valor) {
        if(json == null) return;
        if(nomeCampo == null) return;
        if(!json.containsKey(nomeCampo)) return;
        json.put(nomeCampo, valor);
    }

    private Eaa0103 obterItemDoDocumentoPelaSeqItemXml(Eaa01 eaa01, Integer nItem) {
        if(Utils.isEmpty(eaa01.getEaa0103s())) return null;

        Eaa0103 eaa0103Encontrado = null;
        for(Eaa0103 eaa0103 : eaa01.getEaa0103s()) {
            if(!eaa0103.getEaa0103seq().equals(nItem)) continue;
            eaa0103Encontrado = eaa0103;
            break;
        }

        return eaa0103Encontrado;
    }

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.IMPORTAR_ARQUIVOS_XML_DE_NFE;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjcifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjcifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjcifQ==