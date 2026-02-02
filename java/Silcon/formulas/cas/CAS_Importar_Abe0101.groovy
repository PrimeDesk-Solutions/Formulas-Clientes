package Silcon.formulas.cas;

import sam.model.entities.aa.Aag01;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe01
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase


public class CAS_Importar_Abe0101 extends FormulaBase {
    private XSSFSheet sheet
    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");
        leituraePersistBanco()
    }

    private void leituraePersistBanco() {
        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");
        int linha = 0;

        try{
            while (txt.nextLine()) {
                linha++
                Abe0101 abe0101 = new Abe0101();
                Abe01 abe01 = buscarEntidadePeloCodigo(txt.getCampo(1));
                if (abe01 == null) interromper("Entidade com o código " + txt.getCampo(1) + " não localizada no sistema. Linha: " + (linha + 1).toString());

                abe0101.setAbe0101ent(abe01);
                abe0101.setAbe0101local(txt.getCampo(2).isEmpty() ? null : txt.getCampo(2));
                abe0101.setAbe0101tpLog(null);
                abe0101.setAbe0101endereco(txt.getCampo(4).isEmpty() ? null : txt.getCampo(4));
                abe0101.setAbe0101numero(txt.getCampo(5).isEmpty() ? null : txt.getCampo(5));
                abe0101.setAbe0101bairro(txt.getCampo(6).isEmpty() ? null : txt.getCampo(6));
                abe0101.setAbe0101complem(txt.getCampo(7).isEmpty() ? null : txt.getCampo(7));
                abe0101.setAbe0101cep(txt.getCampo(8).isEmpty() ? null : txt.getCampo(8));
                abe0101.setAbe0101cp(Integer.parseInt(txt.getCampo(9)));
                abe0101.setAbe0101cepCp(txt.getCampo(10).isEmpty() ? null : txt.getCampo(10));

                if (!txt.getCampo(11).isEmpty()) {
                    Aag0201 aag0201 = buscarIDMunicipio(txt.getCampo(11));

                    abe0101.setAbe0101municipio(aag0201);
                }

                if(!txt.getCampo(12).isEmpty()){
                    Aag01 aag01 = buscarIDPais(txt.getCampo(12))

                    if(aag01 == null) interromper("País " + txt.getCampo(12) + " não encontrado no sistema.");

                    abe0101.setAbe0101pais(aag01);
                }
                abe0101.setAbe0101regiao(null);
                abe0101.setAbe0101ddd1(txt.getCampo(14).isEmpty() ? null : txt.getCampo(14));
                abe0101.setAbe0101fone1(txt.getCampo(15).isEmpty() ? null : txt.getCampo(15));
                abe0101.setAbe0101ddd2(txt.getCampo(16).isEmpty() ? null : txt.getCampo(16));
                abe0101.setAbe0101fone2(txt.getCampo(17).isEmpty() ? null : txt.getCampo(17));
                String email = txt.getCampo(18).isEmpty() ? null : txt.getCampo(18);
                email = email && email.length() > 60 ? email.substring(0, 60) : email;
                abe0101.setAbe0101eMail(email);
                abe0101.setAbe0101rs(txt.getCampo(19).isEmpty() ? null : txt.getCampo(19));
                abe0101.setAbe0101ti(Integer.parseInt(txt.getCampo(20)));
                abe0101.setAbe0101ni(txt.getCampo(21).isEmpty() ? null : txt.getCampo(21));
                abe0101.setAbe0101ie(txt.getCampo(22).isEmpty() ? null : txt.getCampo(22));
                abe0101.setAbe0101obs(txt.getCampo(23).isEmpty() ? null : txt.getCampo(23));
                abe0101.setAbe0101principal(Integer.parseInt(txt.getCampo(24)));
                abe0101.setAbe0101entrega(Integer.parseInt( txt.getCampo(25)));
                abe0101.setAbe0101cobranca(Integer.parseInt( txt.getCampo(26)));
                abe0101.setAbe0101outros(Integer.parseInt( txt.getCampo(27)));

                try {
                    getSession().persist(abe0101);
                } catch (Exception e) {
                    interromper("Falha ao importar registro da linha: " + linha)
                }
            }
        }catch (Exception e){
            interromper("Erro ao buscar registro. Linha " + linha + " " + e.getMessage())
        }
    }

    private Abe01 buscarEntidadePeloCodigo(String codEntidade) {

        return getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", codEntidade)).get(ColumnType.ENTITY);
    }

    private Aag0201 buscarIDMunicipio(String idMunicipio) {
        if(idMunicipio == "36014868") idMunicipio = "36238656";

        Aag0201 aag0201 = getSession().createCriteria(Aag0201.class).addWhere(Criterions.eq("aag0201id", Long.parseLong(idMunicipio))).get(ColumnType.ENTITY);

        if(aag0201 == null) interromper("Município " + idMunicipio + " não encontrado no sistema.")

        return aag0201;
    }
    private Aag01 buscarIDPais(String pais) {
        Aag01 aag01 = getSession().createCriteria(Aag01.class).addWhere(Criterions.eq("aag01id", Long.parseLong(pais))).get(ColumnType.ENTITY);

        return aag01;
    }

}