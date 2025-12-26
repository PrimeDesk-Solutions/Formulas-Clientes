package Atilatte.formulas.scf

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.DecimalUtils
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.dto.scf.SCF0221LctoExtratoDto
import sam.server.samdev.formula.FormulaBase

import java.time.LocalDate

class SCF_LeituraExtrato_ofx extends FormulaBase {
    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_EXTRATO_DE_CONTA_CORRENTE;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");

        String TRNTYPE = "";
        LocalDate DTPOSTED = null;
        BigDecimal TRNAMT = BigDecimal.ZERO;
        String FITID = null;
        String MEMO = "";
        String CHECKNUM = "";
        LocalDate dtAtual = LocalDate.now();

        List<SCF0221LctoExtratoDto> listExtratoDto = new ArrayList();

        File file = File.createTempFile(UUID.randomUUID().toString(), "ofx");
        arquivo.transferTo(file);

        File arquivoOFX = new File(file.getCanonicalPath());

        try {
            BufferedReader reader = new BufferedReader(new FileReader(arquivoOFX));
            String line;

            while ((line = reader.readLine()) != null) {
                SCF0221LctoExtratoDto extratoDto = new SCF0221LctoExtratoDto();
                if (line.contains("<STMTTRN>")) {
                    while ((line = reader.readLine().trim()) != "</STMTTRN>") {
                        if (line.contains("<TRNTYPE>")) {
                            TRNTYPE = line.substring(line.indexOf(">")+1, line.length());
                        }
                        if (line.contains("<DTPOSTED>")) {
                            String data = line.substring(line.indexOf(">")+1, line.indexOf(">")+9);
                            DTPOSTED = DateUtils.parseDate(data.trim(), "yyyyMMdd");
                        }
                        if (line.contains("<TRNAMT>")) {
                            String valor = line.substring(line.indexOf(">")+1, line.length()).replace(",",".");
                            TRNAMT = DecimalUtils.create(valor).get();
                        }
                        /*if (line.contains("<FITID>")){
                            String data = line.substring(line.indexOf(">")+1, line.indexOf(">")+9);
                            FITID = DateUtils.parseDate(data.trim(), "yyyyMMdd");
                        }*/
                        if (line.contains("<MEMO>")){
                            MEMO = line.substring(line.indexOf(">")+1, line.length());
                        }
                        if (line.contains("CHECKNUM")){
                            CHECKNUM = line.substring(line.indexOf(">")+1, line.length());
                        }
                        if (line.contains("FITID")){
                            FITID = line.substring(line.indexOf(">")+1, line.length());
                        }
                    }
                    extratoDto.data = DTPOSTED;
                    extratoDto.valor = TRNAMT < 0 ? TRNAMT * (-1) : TRNAMT;
                    extratoDto.dc = TRNTYPE == "DEBIT" ? "D" : "C";
                    extratoDto.historico = MEMO;
                    extratoDto.ni = null;
                    extratoDto.dados1 = CHECKNUM;
                    extratoDto.dados2 = FITID;

                    if(DTPOSTED == dtAtual.minusDays(2)) listExtratoDto.add(extratoDto);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        put("lista", listExtratoDto);
    }
}

/*
<?xml version="1.0" encoding="UTF-8"?>
<OFX>
  <SIGNONMSGSRSV1>
    <SONRS>
      <!-- Informações de autenticação -->
    </SONRS>
  </SIGNONMSGSRSV1>
  <BANKMSGSRSV1>
    <STMTTRNRS>
      <!-- Informações da transação bancária -->
      <STMTRS>
        <!-- Informações da conta bancária -->
        <BANKACCTFROM>
          <!-- Detalhes da conta bancária -->
        </BANKACCTFROM>
        <BANKTRANLIST>
          <!-- Lista de transações -->
          <STMTTRN>
            <!-- Detalhes de uma transação -->
          </STMTTRN>
          <!-- Outras transações -->
        </BANKTRANLIST>
        <LEDGERBAL>
          <!-- Saldo da conta -->
        </LEDGERBAL>
        <AVAILBAL>
          <!-- Saldo disponível -->
        </AVAILBAL>
      </STMTRS>
    </STMTTRNRS>
  </BANKMSGSRSV1>
</OFX>


Aqui estão detalhes mais específicos sobre cada seção e elementos dentro do arquivo OFX:

<OFX>: A tag raiz do arquivo OFX. Todo o conteúdo OFX está contido dentro desta tag.

<SIGNONMSGSRSV1>: Esta seção contém informações relacionadas à autenticação e à sessão. É onde você encontraria informações de login, como nome de usuário e senha.

<SONRS>: Esta tag contém a resposta do servidor após a autenticação ser realizada com sucesso. Ela pode incluir informações como o identificador de sessão.
<BANKMSGSRSV1>: Esta seção contém as mensagens relacionadas às transações bancárias. É onde você encontraria informações sobre contas bancárias e transações.

<STMTTRNRS>: Esta tag contém informações sobre uma transação específica.

<STMTRS>: Esta tag contém informações relacionadas à conta bancária e ao extrato.

<BANKACCTFROM>: Esta tag contém detalhes da conta bancária, como o número da conta e o tipo de conta.

<BANKTRANLIST>: Esta tag contém uma lista de transações.

<STMTTRN>: Cada uma dessas tags contém detalhes de uma transação específica, como data, valor e descrição.
<LEDGERBAL>: Esta tag contém informações sobre o saldo da conta, incluindo o saldo atual.

<AVAILBAL>: Esta tag contém informações sobre o saldo disponível na conta.

OFXHEADER:100: Indica a versão do cabeçalho OFX usada.
DATA:OFXSGML: Especifica o formato dos dados como OFXSGML.
VERSION:102: Indica a versão do arquivo OFX.
SECURITY:NONE: Define o nível de segurança (neste caso, nenhum).
ENCODING:USASCII: Especifica a codificação dos caracteres como USASCII.
CHARSET:1252: Define o conjunto de caracteres como o conjunto de caracteres Windows-1252.
COMPRESSION:NONE: Indica que não há compressão de dados.
OLDFILEUID:NONE e NEWFILEUID:NONE: São identificadores únicos para o arquivo OFX.

Elemento <OFX>:
Esta é a tag raiz que envolve todo o conteúdo OFX.

Elemento <SIGNONMSGSRSV1>:
Contém informações relacionadas à autenticação e à sessão.

Elemento <SONRS>:
Contém informações da resposta do servidor após a autenticação, incluindo status e informações de idioma.

Elemento <BANKMSGSRSV1>:
Contém mensagens relacionadas a transações bancárias.

Elemento <STMTTRNRS>:
Contém informações sobre uma transação específica.

Elemento <TRNUID>:
É um identificador único para a transação.

Elemento <STATUS>:
Fornece informações de status, incluindo código e severidade.

Elemento <STMTRS>:
Contém informações relacionadas à conta bancária e ao extrato.

Elemento <BANKACCTFROM>:
Contém detalhes da conta bancária, como o banco, número da conta e tipo de conta.

Elemento <BANKTRANLIST>:
Contém uma lista de transações bancárias.

Elemento <DTSTART> e <DTEND>:
Indicam o período de tempo das transações listadas.

Elemento <STMTTRN>:
Contém informações detalhadas sobre uma transação específica, incluindo o tipo de transação, data, valor, ID de referência e descrição.

Elemento <LEDGERBAL>:
Fornece informações sobre o saldo da conta, incluindo o saldo atual e a data em que foi calculado.

*/
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTEifQ==