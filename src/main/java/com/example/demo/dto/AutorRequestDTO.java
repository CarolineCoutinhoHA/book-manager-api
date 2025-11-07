package com.example.demo.dto;

import jakarta.validation.constraints.*;

/**
 * DTO DE REQUISIÇÃO - AUTOR (DATA TRANSFER OBJECT)
 * 
 * Este DTO é usado para RECEBER dados do frontend quando:
 * - Criando um novo autor (POST /api/autores)
 * - Atualizando um autor existente (PUT /api/autores/{id})
 * 
 * RESPONSABILIDADES:
 * 1. VALIDAÇÃO DE ENTRADA - Garante que dados estão no formato correto
 * 2. SEGURANÇA - Não expõe campos internos (ID, UUID, etc.)
 * 3. CONTRATO DE API - Define exatamente quais dados são esperados
 * 4. DESACOPLAMENTO - Isola frontend da estrutura interna das entidades
 * 
 * PADRÃO RECORD:
 * - Imutável por natureza (thread-safe)
 * - Gera automaticamente: construtor, getters, equals, hashCode, toString
 * - Mais conciso que classes tradicionais
 * - Ideal para DTOs (apenas dados, sem comportamento)
 * 
 * VALIDAÇÕES BEAN VALIDATION:
 * - Executadas automaticamente quando @Valid é usado no Controller
 * - Falhas geram MethodArgumentNotValidException (400 Bad Request)
 * - Mensagens customizadas para melhor UX
 */
public record AutorRequestDTO(
    
    /**
     * NOME DO AUTOR
     * 
     * Campo obrigatório que identifica o autor.
     * 
     * VALIDAÇÕES:
     * - @NotBlank: Não pode ser null, vazio ou apenas espaços
     * - @Size(max = 100): Máximo 100 caracteres (alinhado com banco)
     * 
     * EXEMPLOS VÁLIDOS:
     * - "Machado de Assis"
     * - "Clarice Lispector"
     * - "José Saramago"
     * 
     * EXEMPLOS INVÁLIDOS:
     * - null → "O nome do autor é obrigatório!"
     * - "" → "O nome do autor é obrigatório!"
     * - "   " → "O nome do autor é obrigatório!"
     * - "Nome muito longo..." (>100 chars) → "O máximo de caracteres para o nome é 100."
     */
    @NotBlank(message = "O nome do autor é obrigatório!")
    @Size(max = 100, message = "O máximo de caracteres para o nome é 100.")
    String nome,
    
    /**
     * EMAIL DO AUTOR
     * 
     * Campo obrigatório e único no sistema.
     * Usado para identificação e comunicação.
     * 
     * VALIDAÇÕES:
     * - @NotBlank: Não pode ser null, vazio ou apenas espaços
     * - @Email: Deve ter formato válido de email
     * 
     * VALIDAÇÃO ADICIONAL:
     * - Unicidade é validada no Service (regra de negócio)
     * 
     * EXEMPLOS VÁLIDOS:
     * - "machado@literatura.com"
     * - "clarice.lispector@email.com.br"
     * - "autor123@gmail.com"
     * 
     * EXEMPLOS INVÁLIDOS:
     * - null → "O email é obrigatório!"
     * - "" → "O email é obrigatório!"
     * - "email-inválido" → "O email deve ter um formato válido!"
     * - "sem@dominio" → "O email deve ter um formato válido!"
     * - "machado@literatura.com" (se já existe) → "Email já está em uso!" (Service)
     */
    @NotBlank(message = "O email é obrigatório!")
    @Email(message = "O email deve ter um formato válido!")
    String email,
    
    /**
     * CEP DO AUTOR
     * 
     * Campo obrigatório para localização/endereço.
     * Deve seguir padrão brasileiro de CEP.
     * 
     * VALIDAÇÕES:
     * - @NotBlank: Não pode ser null, vazio ou apenas espaços
     * - @Pattern: Deve seguir formato XXXXX-XXX (5 dígitos + hífen + 3 dígitos)
     * 
     * REGEX EXPLICADO: \\d{5}-\\d{3}
     * - \\d{5}: Exatamente 5 dígitos
     * - -: Hífen literal
     * - \\d{3}: Exatamente 3 dígitos
     * 
     * EXEMPLOS VÁLIDOS:
     * - "01310-100" (Av. Paulista, SP)
     * - "20040-020" (Copacabana, RJ)
     * - "70040-010" (Brasília, DF)
     * 
     * EXEMPLOS INVÁLIDOS:
     * - null → "O CEP é obrigatório!"
     * - "" → "O CEP é obrigatório!"
     * - "12345678" → "O CEP deve estar no formato XXXXX-XXX"
     * - "1234-567" → "O CEP deve estar no formato XXXXX-XXX"
     * - "12345-67" → "O CEP deve estar no formato XXXXX-XXX"
     */
    @NotBlank(message = "O CEP é obrigatório!")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato XXXXX-XXX")
    String cep,
    
    /**
     * TELEFONE DO AUTOR
     * 
     * Campo OPCIONAL (pode ser null ou vazio).
     * Se preenchido, deve seguir padrão brasileiro.
     * 
     * VALIDAÇÕES:
     * - @Pattern: Se não for null/vazio, deve seguir formato (XX) XXXXX-XXXX
     * 
     * REGEX EXPLICADO: \\(\\d{2}\\) \\d{5}-\\d{4}
     * - \\(: Parêntese de abertura literal
     * - \\d{2}: Exatamente 2 dígitos (código de área)
     * - \\): Parêntese de fechamento literal
     * - (espaço): Espaço literal
     * - \\d{5}: Exatamente 5 dígitos (primeira parte do número)
     * - -: Hífen literal
     * - \\d{4}: Exatamente 4 dígitos (segunda parte do número)
     * 
     * EXEMPLOS VÁLIDOS:
     * - null (campo opcional)
     * - "" (campo opcional)
     * - "(11) 99999-9999" (celular SP)
     * - "(21) 98888-7777" (celular RJ)
     * - "(85) 91234-5678" (celular CE)
     * 
     * EXEMPLOS INVÁLIDOS:
     * - "11999999999" → "O telefone deve estar no formato (XX) XXXXX-XXXX"
     * - "(11) 9999-9999" → "O telefone deve estar no formato (XX) XXXXX-XXXX"
     * - "11 99999-9999" → "O telefone deve estar no formato (XX) XXXXX-XXXX"
     * - "(011) 99999-9999" → "O telefone deve estar no formato (XX) XXXXX-XXXX"
     * 
     * NOTA SOBRE VALIDAÇÃO CONDICIONAL:
     * A anotação @Pattern só é aplicada se o valor não for null.
     * Para campos opcionais, isso é o comportamento desejado.
     */
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", 
             message = "O telefone deve estar no formato (XX) XXXXX-XXXX")
    String telefone
    
) {
    
    /**
     * MÉTODO DE VALIDAÇÃO CUSTOMIZADA (OPCIONAL)
     * 
     * Records podem ter métodos, mas devem ser usados com parcimônia.
     * Este método demonstra como adicionar validações customizadas se necessário.
     * 
     * EM PRODUÇÃO:
     * - Prefira Bean Validation sempre que possível
     * - Use validações customizadas apenas para regras complexas
     * - Considere criar anotações customizadas para reutilização
     */
    public boolean isValid() {
        // Exemplo de validação customizada
        return nome != null && !nome.trim().isEmpty() &&
               email != null && email.contains("@") &&
               cep != null && cep.matches("\\d{5}-\\d{3}");
    }
    
    /**
     * MÉTODO DE NORMALIZAÇÃO (OPCIONAL)
     * 
     * Demonstra como normalizar dados antes do processamento.
     * Útil para padronizar formatos de entrada.
     */
    public AutorRequestDTO normalized() {
        return new AutorRequestDTO(
            nome != null ? nome.trim() : null,
            email != null ? email.trim().toLowerCase() : null,
            cep != null ? cep.trim() : null,
            telefone != null ? telefone.trim() : null
        );
    }
}

/**
 * EXEMPLO DE USO NO FRONTEND (JavaScript):
 * 
 * // Dados válidos
 * const autorData = {
 *     nome: "Machado de Assis",
 *     email: "machado@literatura.com",
 *     cep: "20040-020",
 *     telefone: "(21) 99999-9999"
 * };
 * 
 * // Requisição POST
 * fetch('/api/autores', {
 *     method: 'POST',
 *     headers: {
 *         'Content-Type': 'application/json',
 *         'Authorization': 'Bearer ' + token
 *     },
 *     body: JSON.stringify(autorData)
 * });
 * 
 * EXEMPLO DE RESPOSTA DE ERRO (400 Bad Request):
 * {
 *     "timestamp": "2024-01-15T10:30:00",
 *     "status": 400,
 *     "message": "Dados inválidos. Verifique os campos.",
 *     "fieldErrors": {
 *         "nome": "O nome do autor é obrigatório!",
 *         "email": "O email deve ter um formato válido!",
 *         "cep": "O CEP deve estar no formato XXXXX-XXX"
 *     }
 * }
 */