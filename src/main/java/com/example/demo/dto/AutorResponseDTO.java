package com.example.demo.dto;

/**
 * DTO DE RESPOSTA - AUTOR (DATA TRANSFER OBJECT)
 * 
 * Este DTO é usado para ENVIAR dados para o frontend quando:
 * - Retornando autor criado (POST /api/autores → 201 Created)
 * - Retornando autor encontrado (GET /api/autores/{id} → 200 OK)
 * - Retornando autor atualizado (PUT /api/autores/{id} → 200 OK)
 * - Listando autores (GET /api/autores → 200 OK)
 * 
 * RESPONSABILIDADES:
 * 1. SEGURANÇA - Expõe apenas dados seguros para o frontend
 * 2. PERFORMANCE - Contém apenas dados necessários (não carrega relacionamentos)
 * 3. ESTABILIDADE - Mantém contrato de API estável mesmo com mudanças internas
 * 4. SIMPLICIDADE - Estrutura simples e previsível para o frontend
 * 
 * CAMPOS INCLUÍDOS:
 * - idAutor: ID público para operações do frontend (editar, deletar)
 * - nome: Nome completo do autor
 * - email: Email do autor
 * - cep: CEP do autor
 * - telefone: Telefone do autor (pode ser null)
 * 
 * CAMPOS EXCLUÍDOS (por segurança/performance):
 * - uuid: Identificador interno (não necessário para frontend)
 * - disponibilidade: Campo interno de soft delete
 * - livrosSet: Relacionamentos (evita N+1 queries e dados desnecessários)
 * - timestamps de auditoria (se existissem)
 * - campos de controle interno
 * 
 * PADRÃO RECORD:
 * - Imutável (thread-safe)
 * - Serialização JSON automática
 * - Getters automáticos: idAutor(), nome(), email(), etc.
 * - Equals/HashCode automáticos
 * - ToString automático
 */
public record AutorResponseDTO(
    
    /**
     * ID DO AUTOR (CHAVE PRIMÁRIA)
     * 
     * Identificador único do autor no sistema.
     * Usado pelo frontend para:
     * - Operações de edição (PUT /api/autores/{id})
     * - Operações de exclusão (DELETE /api/autores/{id})
     * - Navegação e referências
     * - Chaves em listas/tabelas
     * 
     * TIPO: Long
     * - Permite IDs grandes (até 9.223.372.036.854.775.807)
     * - Compatível com auto-increment do banco
     * - Serializado como número no JSON
     * 
     * EXEMPLO NO JSON:
     * "idAutor": 1
     */
    Long idAutor,
    
    /**
     * NOME DO AUTOR
     * 
     * Nome completo do autor para exibição.
     * Usado pelo frontend para:
     * - Exibição em listas
     * - Cabeçalhos de páginas
     * - Formulários de edição (valor inicial)
     * - Busca e filtros
     * 
     * CARACTERÍSTICAS:
     * - Sempre presente (nunca null)
     * - Já validado no momento da criação
     * - Pode conter acentos e caracteres especiais
     * - Máximo 100 caracteres
     * 
     * EXEMPLO NO JSON:
     * "nome": "Machado de Assis"
     */
    String nome,
    
    /**
     * EMAIL DO AUTOR
     * 
     * Email único do autor no sistema.
     * Usado pelo frontend para:
     * - Exibição de contato
     * - Formulários de edição (valor inicial)
     * - Validação de unicidade (verificar se mudou)
     * - Links de contato (mailto:)
     * 
     * CARACTERÍSTICAS:
     * - Sempre presente (nunca null)
     * - Formato válido garantido
     * - Único no sistema
     * - Normalizado (lowercase, trimmed)
     * 
     * EXEMPLO NO JSON:
     * "email": "machado@literatura.com"
     */
    String email,
    
    /**
     * CEP DO AUTOR
     * 
     * Código de Endereçamento Postal do autor.
     * Usado pelo frontend para:
     * - Exibição de localização
     * - Formulários de edição (valor inicial)
     * - Integração com APIs de endereço (ViaCEP)
     * - Relatórios geográficos
     * 
     * CARACTERÍSTICAS:
     * - Sempre presente (nunca null)
     * - Formato XXXXX-XXX garantido
     * - Pode ser usado para buscar endereço completo
     * 
     * EXEMPLO NO JSON:
     * "cep": "20040-020"
     */
    String cep,
    
    /**
     * TELEFONE DO AUTOR
     * 
     * Número de telefone do autor (campo opcional).
     * Usado pelo frontend para:
     * - Exibição de contato (se disponível)
     * - Formulários de edição (valor inicial ou vazio)
     * - Links de contato (tel:)
     * - Validação condicional
     * 
     * CARACTERÍSTICAS:
     * - Pode ser null (campo opcional)
     * - Se presente, formato (XX) XXXXX-XXXX garantido
     * - Frontend deve tratar null adequadamente
     * 
     * EXEMPLOS NO JSON:
     * "telefone": "(21) 99999-9999"  // Quando preenchido
     * "telefone": null               // Quando não preenchido
     */
    String telefone
    
) {
    
    /**
     * MÉTODO DE CONVENIÊNCIA - VERIFICAR SE TEM TELEFONE
     * 
     * Facilita verificações no código que usa este DTO.
     * Evita verificações null repetitivas.
     * 
     * @return true se telefone está preenchido, false caso contrário
     */
    public boolean temTelefone() {
        return telefone != null && !telefone.trim().isEmpty();
    }
    
    /**
     * MÉTODO DE CONVENIÊNCIA - OBTER TELEFONE FORMATADO
     * 
     * Retorna telefone formatado ou mensagem padrão.
     * Útil para exibição direta no frontend.
     * 
     * @return Telefone formatado ou "Não informado"
     */
    public String telefoneFormatado() {
        return temTelefone() ? telefone : "Não informado";
    }
    
    /**
     * MÉTODO DE CONVENIÊNCIA - OBTER NOME PARA EXIBIÇÃO
     * 
     * Retorna nome formatado para exibição.
     * Pode incluir lógica de formatação específica.
     * 
     * @return Nome formatado para exibição
     */
    public String nomeExibicao() {
        return nome != null ? nome.trim() : "Nome não informado";
    }
    
    /**
     * MÉTODO DE CONVENIÊNCIA - GERAR LINK MAILTO
     * 
     * Gera link mailto para abrir cliente de email.
     * Útil para interfaces web.
     * 
     * @return Link mailto formatado
     */
    public String linkEmail() {
        return "mailto:" + email;
    }
    
    /**
     * MÉTODO DE CONVENIÊNCIA - GERAR LINK TEL
     * 
     * Gera link tel para discagem em dispositivos móveis.
     * Remove formatação para compatibilidade.
     * 
     * @return Link tel formatado ou null se não tem telefone
     */
    public String linkTelefone() {
        if (!temTelefone()) {
            return null;
        }
        // Remove formatação: (11) 99999-9999 → 11999999999
        String numeroLimpo = telefone.replaceAll("[^0-9]", "");
        return "tel:+55" + numeroLimpo;  // +55 = código do Brasil
    }
}

/**
 * EXEMPLO DE RESPOSTA JSON COMPLETA:
 * 
 * {
 *     "idAutor": 1,
 *     "nome": "Machado de Assis",
 *     "email": "machado@literatura.com",
 *     "cep": "20040-020",
 *     "telefone": "(21) 99999-9999"
 * }
 * 
 * EXEMPLO DE RESPOSTA JSON SEM TELEFONE:
 * 
 * {
 *     "idAutor": 2,
 *     "nome": "Clarice Lispector",
 *     "email": "clarice@literatura.com",
 *     "cep": "22071-900",
 *     "telefone": null
 * }
 * 
 * EXEMPLO DE USO NO FRONTEND (JavaScript):
 * 
 * // Receber dados da API
 * fetch('/api/autores/1')
 *     .then(response => response.json())
 *     .then(autor => {
 *         console.log('ID:', autor.idAutor);
 *         console.log('Nome:', autor.nome);
 *         console.log('Email:', autor.email);
 *         
 *         // Verificar telefone opcional
 *         if (autor.telefone) {
 *             console.log('Telefone:', autor.telefone);
 *         } else {
 *             console.log('Telefone não informado');
 *         }
 *         
 *         // Usar em formulário de edição
 *         document.getElementById('nome').value = autor.nome;
 *         document.getElementById('email').value = autor.email;
 *         document.getElementById('cep').value = autor.cep;
 *         document.getElementById('telefone').value = autor.telefone || '';
 *     });
 * 
 * EXEMPLO DE USO EM LISTA (React):
 * 
 * function AutorCard({ autor }) {
 *     return (
 *         <div className="autor-card">
 *             <h3>{autor.nome}</h3>
 *             <p>Email: <a href={`mailto:${autor.email}`}>{autor.email}</a></p>
 *             <p>CEP: {autor.cep}</p>
 *             {autor.telefone && (
 *                 <p>Telefone: <a href={`tel:${autor.telefone}`}>{autor.telefone}</a></p>
 *             )}
 *             <button onClick={() => editarAutor(autor.idAutor)}>
 *                 Editar
 *             </button>
 *         </div>
 *     );
 * }
 */